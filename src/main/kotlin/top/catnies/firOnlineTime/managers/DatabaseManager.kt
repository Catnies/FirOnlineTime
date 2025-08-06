package top.catnies.firOnlineTime.managers

import top.catnies.firOnlineTime.database.Database
import top.catnies.firOnlineTime.database.MysqlDatabase
import top.catnies.firOnlineTime.database.SqliteDatabase
import kotlin.properties.Delegates

class DatabaseManager private constructor(){

    var database: Database by Delegates.notNull()

    companion object {
        val instance: DatabaseManager by lazy { DatabaseManager().apply { reload() } }
    }

    // 创建数据库
    fun reload() {
        when (SettingsManager.instance.DatabaseType.lowercase()) {
            "sqlite" -> database = SqliteDatabase.instance
            "mysql" -> database = MysqlDatabase.instance
        }
    }

}