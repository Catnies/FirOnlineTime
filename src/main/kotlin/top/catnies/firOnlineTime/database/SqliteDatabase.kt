package top.catnies.firOnlineTime.database

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.managers.DataCacheManager
import top.catnies.firOnlineTime.managers.SettingsManager
import top.catnies.firOnlineTime.utils.TaskUtils
import top.catnies.firOnlineTime.utils.TimeUtil
import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException


class SqliteDatabase private constructor() : Database {

    private var url: String = "jdbc:sqlite:plugins/FirOnlineTime/database.db"
    private val tableName: String = SettingsManager.instance.TABLE_NAME

    companion object {
        val instance: SqliteDatabase by lazy { SqliteDatabase().apply {
            createTable()
        } }
    }

    // 建表函数
    fun createTable() {
        try {
            DriverManager.getConnection(url).use { connection ->
                connection.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS $tableName (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, 
                    uuid VARCHAR(255) NOT NULL, 
                    date Date NOT NULL, 
                    onlineTime BIGINT NOT NULL,
                    UNIQUE(uuid, date))
                    """
                        .trimIndent()
                ).use { statement ->
                    statement.execute()
                }
            }
        } catch (e: SQLException) {
            FirOnlineTime.instance!!.logger.severe("建表时发生错误: $e")
        }
    }

    // 插入 onlineTime 数据或使 onlineTime 增加
    override fun upsertOnlineTime(player: OfflinePlayer, date: Date, addTime: Long) {
        val uuid = player.uniqueId.toString()
        try {
            DriverManager.getConnection(url).use { connection ->
                // 与Mysql不同, Sqlite不支持ON DUPLICATE KEY UPDATE
                val sql = """
                INSERT INTO $tableName (uuid, date, onlineTime)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid, date) DO UPDATE SET
                onlineTime = onlineTime + excluded.onlineTime
                """.trimIndent()
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid)
                    statement.setDate(2, java.sql.Date(date.time))  // 修正 Date 类型转换
                    statement.setLong(3, addTime)
                    statement.execute()
                }
            }
        } catch (e: SQLException) {
            FirOnlineTime.instance!!.logger.severe("在线时间-插入/更新数据时发生错误: $e")
        }
    }

    // 查询玩家数据
    override fun queryPlayerData(player: OfflinePlayer, baseDate: Date, queryType: QueryType): Long {
        val uuid = player.uniqueId.toString()
        try {
            DriverManager.getConnection(url).use { connection ->
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
                        // FIX: TOTAL 查询不再需要Date范围，把Filter去掉
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
            FirOnlineTime.instance!!.logger.severe("在线时间-查询玩家数据时发生错误: $e")
        }
        return 0L
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
        }
    }

}