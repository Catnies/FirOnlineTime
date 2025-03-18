package top.catnies.firOnlineTime.database

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.managers.DataCacheManager
import top.catnies.firOnlineTime.managers.SettingsManager
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
                val sql = "SELECT onlineTime FROM $tableName WHERE uuid = ? and date BETWEEN ? AND ?"
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
                        QueryType.TOTAL -> {
                            statement.setDate(2, TimeUtil.getMonthStart(baseDate))
                            statement.setDate(3, TimeUtil.getMonthEnd(baseDate))
                        }
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
        val expire = TimeUtil.isExpire(data.lastSavedTime!!)

        // 如果缓存过期了, 代表已经跨日期了, 需要分割
        if (expire) {
            val boundary: Long = TimeUtil.getTomorrowStartByTimeMillisStamp(data.lastSavedTime!!)
            val yesterdayPart = boundary - data.lastSavedTime!!
            val yesterdayDate = Date(data.lastSavedTime!!)
            val todayPart = systemNow - boundary
            val todayDate = TimeUtil.getNowSQLDate()

            // 把最新的数据保存到数据库中
            upsertOnlineTime(player, yesterdayDate, yesterdayPart)
            upsertOnlineTime(player, todayDate, todayPart)

            // 重新获取从数据库中获取数据, 刷新当前缓存
            Bukkit.getScheduler().runTaskAsynchronously(FirOnlineTime.instance!!, Runnable {
                data.saveAndRefreshCache()
                data.lastSavedTime = systemNow
                data.dataRefreshTime = systemNow
            })
        }

        // 如果缓存没有过期, 则直接更新
        else {
            val todayPart = systemNow - data.lastSavedTime!!
            val todayDate = TimeUtil.getNowSQLDate()

            // 把最新的数据保存到数据库中
            upsertOnlineTime(player, todayDate, todayPart)

            // 更新缓存中的最后上线时间戳, 今日在线时间
            data.savedTodayOnlineTime += todayPart
            data.lastSavedTime = systemNow
            data.dataRefreshTime = systemNow
        }
    }

}