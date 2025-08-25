package top.catnies.firOnlineTime.managers

import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.database.Database
import top.catnies.firOnlineTime.database.MysqlDatabase
import top.catnies.firOnlineTime.database.PsqlDatabase
import top.catnies.firOnlineTime.database.SqliteDatabase
import kotlin.properties.Delegates

class DatabaseManager private constructor() {

    var database: Database by Delegates.notNull()

    companion object {
        val instance: DatabaseManager by lazy { DatabaseManager().apply { reload() } }
    }

    // 创建数据库
    fun reload() {
        val dbType = SettingsManager.instance.DatabaseType.lowercase()
        val configVersion = SettingsManager.instance.configVersion
        
        // 如果配置版本是1.0，不允许使用PostgreSQL
        if (configVersion < 2.0 && (dbType == "postgresql" || dbType == "psql")) {
            FirOnlineTime.instance.logger.warning("配置版本1.0不支持PostgreSQL，已自动回退到SQLite")
            database = SqliteDatabase.instance
            return
        }
        
        when (dbType) {
            "sqlite" -> database = SqliteDatabase.instance
            "mysql" -> database = MysqlDatabase.instance
            "postgresql", "psql" -> database = PsqlDatabase.instance
            else -> {
                database = SqliteDatabase.instance
                FirOnlineTime.instance.logger.warning("数据库类型配置错误！已自动回退到SQLite")
            } // 默认使用 SQLite
        }
    }
}
