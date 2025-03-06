package top.catnies.firOnlineTime.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.managers.DataCacheManager
import top.catnies.firOnlineTime.managers.SettingsManager
import top.catnies.firOnlineTime.utils.TimeUtil
import java.sql.Date
import java.sql.SQLException

enum class QueryType {
    DAY, WEEK, MONTH, TOTAL
}


class MysqlDatabase private constructor(){

    private val tableName: String = SettingsManager.instance.TABLE_NAME
    private lateinit var mysql: HikariDataSource

    companion object {
        val instance: MysqlDatabase by lazy { MysqlDatabase().apply {
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
            mysql = HikariDataSource(hikariConfig)

            FirOnlineTime.instance!!.logger.info("MySQL 数据库已连接成功！")
        } catch (e: Exception) {
            FirOnlineTime.instance!!.logger.severe("连接 MySQL 数据库时发生错误, 插件将自动关闭:")
            e.printStackTrace()
            FirOnlineTime.instance!!.server.pluginManager.disablePlugin(FirOnlineTime.instance!!)
        }
    }


    // 建表函数
    fun createTable() {
        try {
            mysql.connection.use { connection ->
                connection.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS $tableName (
                    id INT AUTO_INCREMENT PRIMARY KEY, 
                    uuid VARCHAR(255) NOT NULL, 
                    date Date NOT NULL, 
                    onlineTime BIGINT NOT NULL)
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


    // Insert操作,一般是玩家本日首次登陆
    fun insertOnlineTime(player: OfflinePlayer, date: Date, time: Long) {
        val uuid = player.uniqueId.toString()
        try {
            mysql.connection.use { connection ->
                val sql = "INSERT INTO $tableName (uuid, date, onlineTime) VALUES(?, ?, ?)"
                connection.prepareStatement(sql).use { statement ->
                    // 替换占位符
                    statement.setString(1, uuid)
                    statement.setDate(2, date)
                    statement.setLong(3, time)
                    // 执行插入
                    statement.execute()
                }
            }
        } catch (e: SQLException) {
            FirOnlineTime.instance!!.logger.severe("在线时间-插入数据时发生错误:$e")
        }
    }

    // UPDATE操作,直接使onlineTime增加
    fun addOnlineTime(player: OfflinePlayer, date: Date, addTime: Long) {
        val uuid = player.uniqueId.toString()
        try {
            mysql.connection.use { connection ->
                val sql = "UPDATE $tableName SET onlineTime = onlineTime + ? WHERE uuid = ? and date = ?"
                connection.prepareStatement(sql).use { statement ->
                    statement.setLong(1, addTime) // 增加的时间,单位毫秒
                    statement.setString(2, uuid)
                    statement.setDate(3, date)
                    statement.execute()
                }
            }
        } catch (e: SQLException) {
            FirOnlineTime.instance!!.logger.severe("在线时间-更新数据时发生错误:$e")
        }
    }


    // 查询玩家数据
    fun queryPlayerData(player: OfflinePlayer, baseDate: Date, queryType: QueryType): Long {
        val uuid = player.uniqueId.toString()
        try {
            mysql.connection.use { connection ->
                val sql = "SELECT onlineTime FROM $tableName WHERE uuid = ? and date BETWEEN ? AND ?"
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid)
                    // 指定date范围
                    when (queryType) {
                        QueryType.DAY -> {
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

        return -1
    }


    // 更新单个玩家的数据库在线时间数据
    fun updateOnlineTime(player: OfflinePlayer, systemNow: Long = System.currentTimeMillis()) {
        val data = DataCacheManager.instance.onlineCache[player.uniqueId] ?: return
        val lastLoginTime: Long = data.loginTime!!

        // 日期分界线
        val tomorrow: Long = TimeUtil.getTomorrowStartByTimeMillisStamp(lastLoginTime)

        // 处理日期变更, 如果时间戳超过了明天的0点, 则需要将时间戳分为两个部分
        if (systemNow >= tomorrow) {
            // 两个时间点不在同一天的情况
            // 0点前
            val past1 = tomorrow - lastLoginTime
            val date1 = Date(lastLoginTime)

            // 检测是否是本日首次记录,因为有可能玩家 23:59 登陆,然后 00:09 监听器才记录时间
            val time: Long = queryPlayerData(player, date1, QueryType.DAY)
            if (time == -1L) {
                // 本日首次记录
                insertOnlineTime(player, date1, past1)
            } else {
                addOnlineTime(player, date1, past1)
            }

            // 0点后
            val past2 = systemNow - tomorrow
            val date2: Date = TimeUtil.getNowSQLDate()
            insertOnlineTime(player, date2, past2)

            // 更新缓存中的日周月累计时间
            Bukkit.getScheduler().runTaskAsynchronously(FirOnlineTime.instance!!, Runnable { data.refreshCache() })
        } else {
            // 两个时间点都在同一天的情况
            val past = systemNow - lastLoginTime
            val date = Date(lastLoginTime)

            // 检测是否是本日首次记录
            val time: Long = queryPlayerData(player, date, QueryType.DAY)
            if (time == -1L) {
                // 本日首次记录
                insertOnlineTime(player, date, past)
            } else {
                addOnlineTime(player, date, past)
            }
        }

        // 更新缓存中的最后上线时间戳
        data.loginTime = systemNow
    }

}