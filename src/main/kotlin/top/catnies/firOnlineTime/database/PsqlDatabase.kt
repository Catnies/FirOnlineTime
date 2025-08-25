package top.catnies.firOnlineTime.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.managers.DataCacheManager
import top.catnies.firOnlineTime.managers.SettingsManager
import top.catnies.firOnlineTime.utils.TaskUtils
import top.catnies.firOnlineTime.utils.TimeUtil
import java.sql.Date
import java.sql.SQLException

//Writing by HiBer2007

class PsqlDatabase private constructor() : Database {

    private val tableName: String = SettingsManager.instance.TABLE_NAME
    private lateinit var dataSource: HikariDataSource

    companion object {
        val instance: PsqlDatabase by lazy { PsqlDatabase().apply {
            reload()
            createTable()
        } }
    }

    // 重新加载配置文件
    fun reload() {
        try {
            val hikariConfig = HikariConfig()
            hikariConfig.jdbcUrl = SettingsManager.instance.JDBC_URL
            hikariConfig.driverClassName = SettingsManager.instance.JDBC_DRIVER
            hikariConfig.username = SettingsManager.instance.USERNAME
            hikariConfig.password = SettingsManager.instance.PASSWORD
            dataSource = HikariDataSource(hikariConfig)

            FirOnlineTime.instance.logger.info("PostgreSQL 数据库已连接成功！")
        } catch (e: Exception) {
            FirOnlineTime.instance.logger.severe("连接 PostgreSQL 数据库时发生错误, 插件将自动关闭:")
            e.printStackTrace()
            FirOnlineTime.instance.server.pluginManager.disablePlugin(FirOnlineTime.instance)
        }
    }

    // 建表函数
    fun createTable() {
        try {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS $tableName (
                    id SERIAL PRIMARY KEY, 
                    uuid VARCHAR(255) NOT NULL, 
                    date DATE NOT NULL, 
                    onlineTime BIGINT NOT NULL,
                    CONSTRAINT unique_player_date UNIQUE (uuid, date))
                    """
                    .trimIndent()
                ).use { statement ->
                    statement.execute()
                }
            }
        } catch (e: SQLException) {
            FirOnlineTime.instance.logger.severe("建表时发生错误: $e")
        }
    }

    // 插入 onlineTime 数据或使 onlineTime 增加
    override fun upsertOnlineTime(player: OfflinePlayer, date: Date, addTime: Long) {
        val uuid = player.uniqueId.toString()
        try {
            dataSource.connection.use { connection -> 
                val sql = """
                    INSERT INTO $tableName (uuid, date, onlineTime)
                    VALUES (?, ?, ?)
                    ON CONFLICT (uuid, date) 
                    DO UPDATE SET onlineTime = $tableName.onlineTime + EXCLUDED.onlineTime
                """.trimIndent()
                
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid)
                    statement.setDate(2, java.sql.Date(date.time))
                    statement.setLong(3, addTime)
                    statement.execute()
                }
            }
        } catch (e: SQLException) {
            FirOnlineTime.instance.logger.severe("在线时间-插入/更新数据时发生错误: $e")
        }
    }

    // 查询玩家数据
    override fun queryPlayerData(player: OfflinePlayer, baseDate: Date, queryType: QueryType): Long {
        val uuid = player.uniqueId.toString()
        try {
            dataSource.connection.use { connection ->
                // 根据查询类型动态构建SQL
                val sql = if (queryType == QueryType.TOTAL) {
                    "SELECT onlineTime FROM $tableName WHERE uuid = ?"
                } else {
                    "SELECT onlineTime FROM $tableName WHERE uuid = ? AND date BETWEEN ? AND ?"
                }

                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid)
                    // 指定date范围
                    when (queryType) {
                        QueryType.TODAY -> {
                            statement.setDate(2, TimeUtil.getNowSQLDate())
                            statement.setDate(3, TimeUtil.getNowSQLDate())
                        }
                        QueryType.WEEK -> {
                            statement.setDate(2, TimeUtil.getWeekStart(baseDate))
                            statement.setDate(3, TimeUtil.getWeekEnd(baseDate))
                        }
                        QueryType.MONTH -> {
                            statement.setDate(2, TimeUtil.getMonthStart(baseDate))
                            statement.setDate(3, TimeUtil.getMonthEnd(baseDate))
                        }
                        QueryType.TOTAL -> {}
                    }
                    // 查询
                    val resultSet = statement.executeQuery()

                    var sum = 0L
                    while (resultSet.next()) sum += resultSet.getLong(1)
                    return sum
                }
            }
        } catch (e: SQLException) {
            FirOnlineTime.instance.logger.severe("在线时间-查询玩家数据时发生错误: $e")
        }
        return 0L
    }

    // 根据传入的日期查询期间在线日期数
    override fun queryOnlineDays(player: OfflinePlayer, baseDate: Date, queryType: QueryType): Int {
        val uuid = player.uniqueId.toString()
        return try {
            dataSource.connection.use { connection ->
                // 基础查询模板
                val sqlTemplate = when (queryType) {
                    QueryType.TOTAL -> "SELECT COUNT(DISTINCT date) FROM $tableName WHERE uuid = ?"
                    else -> "SELECT COUNT(DISTINCT date) FROM $tableName WHERE uuid = ? AND date BETWEEN ? AND ?"
                }

                connection.prepareStatement(sqlTemplate).use { statement ->
                    statement.setString(1, uuid)

                    // 设置日期范围参数
                    if (queryType != QueryType.TOTAL) {
                        val (start, end) = when (queryType) {
                            QueryType.TODAY -> TimeUtil.getNowSQLDate() to TimeUtil.getNowSQLDate()
                            QueryType.WEEK -> TimeUtil.getWeekStart(baseDate) to TimeUtil.getWeekEnd(baseDate)
                            QueryType.MONTH -> TimeUtil.getMonthStart(baseDate) to TimeUtil.getMonthEnd(baseDate)
                            else -> throw IllegalArgumentException("无效的查询类型")
                        }
                        statement.setDate(2, start)
                        statement.setDate(3, end)
                    }

                    // 执行查询并返回结果
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) resultSet.getInt(1) else 0
                }
            }
        } catch (e: SQLException) {
            FirOnlineTime.instance.logger.severe("在线日期数查询失败: ${e.message}")
            0
        } catch (e: IllegalArgumentException) {
            FirOnlineTime.instance.logger.warning("无效的查询类型: $queryType")
            0
        }
    }

    // 根据传入的日期查询期间在线日期数
    override fun queryOnlineDays(player: OfflinePlayer, startDate: Date, endDate: Date): Int {
        val uuid = player.uniqueId.toString()
        return try {
            dataSource.connection.use { connection ->
                // 固定语法：查询指定日期范围内的不同日期计数
                val sql = "SELECT COUNT(DISTINCT date) FROM $tableName WHERE uuid = ? AND date BETWEEN ? AND ?"

                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid)
                    statement.setDate(2, startDate)  // 起点日期
                    statement.setDate(3, endDate)    // 终点日期

                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) resultSet.getInt(1) else 0
                }
            }
        } catch (e: SQLException) {
            FirOnlineTime.instance.logger.severe("在线日期数查询失败: ${e.message}")
            0
        }
    }

    // 更新单个玩家的数据库在线时间数据
    override fun saveAndRefreshOnlineCache(player: OfflinePlayer, systemNow: Long) {
        val data = DataCacheManager.instance.onlineCache[player.uniqueId] ?: return
        // lastSavedTime 不可能为 null，如果玩家在线，它在加入时就会被初始化
        val lastSaveTime = data.lastSavedTime!!
        val expire = TimeUtil.isExpire(lastSaveTime)

        // 如果缓存过期了, 代表已经跨日期了, 需要分割
        if (expire) {
            val boundary: Long = TimeUtil.getTomorrowStartByTimeMillisStamp(lastSaveTime)
            val yesterdayPart = boundary - lastSaveTime
            val yesterdayDate = Date(lastSaveTime)
            val todayPart = systemNow - boundary
            val todayDate = TimeUtil.getNowSQLDate()

            // 把最新的数据保存到数据库中
            // 只有当这些部分大于0时才保存，避免无效的数据库写入
            if (yesterdayPart > 0) upsertOnlineTime(player, yesterdayDate, yesterdayPart)
            if (todayPart > 0) upsertOnlineTime(player, todayDate, todayPart)

            // FIX 1: Synchronized重置今日在线时间缓存为新一天的部分
            data.savedTodayOnlineTime = todayPart
            // FIX 2: Synchronized更新最后保存时间戳，防止下次保存时重复进入此逻辑块
            data.lastSavedTime = systemNow
            data.dataRefreshTime = systemNow

            // FIX 3: Async刷新其他数据{周、月、总}
            TaskUtils.runAsyncTask {
                // 这里的 saveAndRefreshCache 应该被设计为刷新{周、月、总榜}数据
                data.saveAndRefreshCache()
            }
        }
        // 如果缓存没有过期, 则直接Update
        else {
            val sessionTime = systemNow - lastSaveTime
            // 如果时间差小于等于0，说明系统时间可能被回调，或者这是一个无效的保存周期，直接滚蛋
            if (sessionTime <= 0) return

            val todayDate = TimeUtil.getNowSQLDate()

            // 把最新的数据保存到数据库中
            upsertOnlineTime(player, todayDate, sessionTime)

            // 更新缓存中的今日在线时间和最后保存时间戳
            data.savedTodayOnlineTime += sessionTime
            data.lastSavedTime = systemNow
            data.dataRefreshTime = systemNow

            data.savedTodayOnlineTime = queryPlayerData(player, todayDate, QueryType.TODAY)
            data.savedWeekOnlineDays = queryOnlineDays(player, todayDate, QueryType.WEEK)
            data.savedMonthOnlineDays = queryOnlineDays(player, todayDate, QueryType.MONTH)
            data.savedTotalOnlineDays = queryOnlineDays(player, todayDate, QueryType.TOTAL)
        }
    }
}
