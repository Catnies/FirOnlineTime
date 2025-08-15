package top.catnies.firOnlineTime.database

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.managers.DataCacheManager
import top.catnies.firOnlineTime.managers.SettingsManager
import top.catnies.firOnlineTime.utils.TaskUtils
import top.catnies.firOnlineTime.utils.TimeUtil
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class SqliteDatabase private constructor() : Database {

    private var url: String = "jdbc:sqlite:plugins/FirOnlineTime/database.db"
    private val tableName: String = SettingsManager.instance.TABLE_NAME
    private lateinit var connection: Connection
    private val connectionLock = Any()

    companion object {
        val instance: SqliteDatabase by lazy {
            SqliteDatabase().apply {
                // 初始化连接使用任务队列确保线程安全
                DatabaseTaskQueue.submitTask("初始化SQLite连接") {
                    synchronized(connectionLock) {
                        if (!::connection.isInitialized || connection.isClosed) {
                            reconnect()
                        }
                    }
                    createTable()
                }.get(10, TimeUnit.SECONDS)
            }
        }
    }

    // 重新连接数据库
    private fun reconnect() {
        synchronized(connectionLock) {
            try {
                if (::connection.isInitialized && !connection.isClosed) {
                    connection.close()
                }
                connection = DriverManager.getConnection(url)
                FirOnlineTime.instance.logger.info("SQLite 数据库已重新连接")
            } catch (e: SQLException) {
                FirOnlineTime.instance.logger.severe("重新连接SQLite数据库失败: ${e.message}")
                throw e
            }
        }
    }

    // 确保连接有效（带自动重连）
    private fun ensureConnection(): Connection {
        return synchronized(connectionLock) {
            if (!::connection.isInitialized || connection.isClosed || !connection.isValid(1)) {
                FirOnlineTime.instance.logger.warning("数据库连接无效，尝试重新连接...")
                reconnect()
            }
            connection
        }
    }

    // 建表函数
    fun createTable() {
        DatabaseTaskQueue.submitTask("创建表") {
            try {
                val conn = ensureConnection()
                conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS $tableName (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, 
                    uuid VARCHAR(255) NOT NULL, 
                    date Date NOT NULL, 
                    onlineTime BIGINT NOT NULL,
                    UNIQUE(uuid, date))
                    """.trimIndent()
                ).use { statement ->
                    statement.execute()
                }
                FirOnlineTime.instance.logger.info("SQLite 表已创建/验证")
            } catch (e: SQLException) {
                FirOnlineTime.instance.logger.severe("建表时发生错误: $e")
                throw e
            }
        }.exceptionally { e ->
            FirOnlineTime.instance.logger.severe("创建表任务失败: ${e.message}")
            null
        }
    }

    // 插入 onlineTime 数据或使 onlineTime 增加
    override fun upsertOnlineTime(player: OfflinePlayer, date: Date, addTime: Long) {
        DatabaseTaskQueue.submitTask("更新在线时间") {
            val uuid = player.uniqueId.toString()
            try {
                val conn = ensureConnection()
                val sql = """
                INSERT INTO $tableName (uuid, date, onlineTime)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid, date) DO UPDATE SET
                onlineTime = onlineTime + excluded.onlineTime
                """.trimIndent()
                conn.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid)
                    statement.setDate(2, java.sql.Date(date.time))
                    statement.setLong(3, addTime)
                    statement.execute()
                }
            } catch (e: SQLException) {
                FirOnlineTime.instance.logger.severe("在线时间-插入/更新数据时发生错误: $e")
                throw e
            }
        }.exceptionally { e ->
            FirOnlineTime.instance.logger.severe("更新在线时间任务失败: ${e.message}")
            null
        }
    }

    // 查询玩家数据
    override fun queryPlayerData(player: OfflinePlayer, baseDate: Date, queryType: QueryType): Long {
        return DatabaseTaskQueue.submitTask("查询玩家数据") {
            val uuid = player.uniqueId.toString()
            try {
                val conn = ensureConnection()
                val sql = if (queryType == QueryType.TOTAL) {
                    "SELECT onlineTime FROM $tableName WHERE uuid = ?"
                } else {
                    "SELECT onlineTime FROM $tableName WHERE uuid = ? AND date BETWEEN ? AND ?"
                }

                conn.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid)
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
                    val resultSet = statement.executeQuery()

                    var sum = 0L
                    while (resultSet.next()) sum += resultSet.getLong(1)
                    sum
                }
            } catch (e: SQLException) {
                FirOnlineTime.instance.logger.severe("在线时间-查询玩家数据时发生错误: $e")
                throw e
            }
        }.get(5, TimeUnit.SECONDS) ?: 0L
    }

    // 根据传入的日期查询在线日期数
    override fun queryOnlineDays(player: OfflinePlayer, baseDate: Date, queryType: QueryType): Int {
        return DatabaseTaskQueue.submitTask("查询在线天数") {
            val uuid = player.uniqueId.toString()
            try {
                val conn = ensureConnection()
                val sqlTemplate = when (queryType) {
                    QueryType.TOTAL -> "SELECT COUNT(DISTINCT date) FROM $tableName WHERE uuid = ?"
                    else -> "SELECT COUNT(DISTINCT date) FROM $tableName WHERE uuid = ? AND date BETWEEN ? AND ?"
                }

                conn.prepareStatement(sqlTemplate).use { statement ->
                    statement.setString(1, uuid)

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

                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) resultSet.getInt(1) else 0
                }
            } catch (e: SQLException) {
                FirOnlineTime.instance.logger.severe("在线日期数查询失败: ${e.message}")
                throw e
            } catch (e: IllegalArgumentException) {
                FirOnlineTime.instance.logger.warning("无效的查询类型: $queryType")
                throw e
            }
        }.get(5, TimeUnit.SECONDS) ?: 0
    }

    // 根据传入的日期查询期间在线日期数
    override fun queryOnlineDays(player: OfflinePlayer, startDate: Date, endDate: Date): Int {
        return DatabaseTaskQueue.submitTask("查询日期范围在线天数") {
            val uuid = player.uniqueId.toString()
            try {
                val conn = ensureConnection()
                val sql = "SELECT COUNT(DISTINCT date) FROM $tableName WHERE uuid = ? AND date BETWEEN ? AND ?"

                conn.prepareStatement(sql).use { statement ->
                    statement.setString(1, uuid)
                    statement.setDate(2, startDate)
                    statement.setDate(3, endDate)

                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) resultSet.getInt(1) else 0
                }
            } catch (e: SQLException) {
                FirOnlineTime.instance.logger.severe("在线日期数查询失败: ${e.message}")
                throw e
            }
        }.get(5, TimeUnit.SECONDS) ?: 0
    }

    // 更新单个玩家的数据库在线时间数据
    override fun saveAndRefreshOnlineCache(player: OfflinePlayer, systemNow: Long) {
        DatabaseTaskQueue.submitTask("保存并刷新缓存") {
            val data = DataCacheManager.instance.onlineCache[player.uniqueId] ?: return@submitTask
            val lastSaveTime = data.lastSavedTime!!
            val expire = TimeUtil.isExpire(lastSaveTime)

            if (expire) {
                val boundary: Long = TimeUtil.getTomorrowStartByTimeMillisStamp(lastSaveTime)
                val yesterdayPart = boundary - lastSaveTime
                val yesterdayDate = Date(lastSaveTime)
                val todayPart = systemNow - boundary
                val todayDate = TimeUtil.getNowSQLDate()

                if (yesterdayPart > 0) upsertOnlineTime(player, yesterdayDate, yesterdayPart)
                if (todayPart > 0) upsertOnlineTime(player, todayDate, todayPart)

                data.savedTodayOnlineTime = todayPart
                data.lastSavedTime = systemNow
                data.dataRefreshTime = systemNow

                // 异步刷新其他数据
                TaskUtils.runAsyncTask {
                    data.saveAndRefreshCache()
                }
            } else {
                val sessionTime = systemNow - lastSaveTime
                if (sessionTime <= 0) return@submitTask

                val todayDate = TimeUtil.getNowSQLDate()
                upsertOnlineTime(player, todayDate, sessionTime)

                data.savedTodayOnlineTime += sessionTime
                data.lastSavedTime = systemNow
                data.dataRefreshTime = systemNow
            }
        }.exceptionally { e ->
            FirOnlineTime.instance.logger.severe("保存并刷新缓存任务失败: ${e.message}")
            null
        }
    }

    // 关闭数据库连接
    fun close() {
        synchronized(connectionLock) {
            try {
                if (::connection.isInitialized && !connection.isClosed) {
                    connection.close()
                    FirOnlineTime.instance.logger.info("SQLite 数据库连接已关闭")
                }
            } catch (e: SQLException) {
                FirOnlineTime.instance.logger.warning("关闭SQLite连接时出错: ${e.message}")
            }
        }
    }
}
