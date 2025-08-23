package top.catnies.firOnlineTime.managers

import org.bukkit.configuration.file.YamlConfiguration
import top.catnies.firOnlineTime.utils.ConfigUtil
import kotlin.properties.Delegates

class SettingsManager private constructor(){

    var settings by Delegates.notNull<YamlConfiguration>()
    var CacheUpdateInterval by Delegates.notNull<Int>()
    var OfflinePlayerUpdateInterval by Delegates.notNull<Int>()

    // Database
    var DatabaseType by Delegates.notNull<String>()
    var JDBC_URL by Delegates.notNull<String>()
    var JDBC_DRIVER by Delegates.notNull<String>()
    var USERNAME by Delegates.notNull<String>()
    var PASSWORD by Delegates.notNull<String>()
    var TABLE_NAME by Delegates.notNull<String>()

    companion object {
        val instance: SettingsManager by lazy { SettingsManager().apply { reload() } }
    }

    fun reload() {
        settings = ConfigUtil.registerConfig("settings.yml")
        CacheUpdateInterval = settings.getInt("CacheUpdateInterval", 600)
        OfflinePlayerUpdateInterval = settings.getInt("OfflinePlayerUpdateInterval", 600)

        // Database
        DatabaseType = settings.getString("DatabaseType", "SQLite")!!
        
        // 根据数据库类型获取相应的配置
        when (DatabaseType.lowercase()) {
            "mysql" -> {
                JDBC_URL = settings.getString("MySQL.jdbc-url")!!
                JDBC_DRIVER = settings.getString("MySQL.jdbc-class")!!
                USERNAME = settings.getString("MySQL.properties.user")!!
                PASSWORD = settings.getString("MySQL.properties.password")!!
                TABLE_NAME = settings.getString("MySQL.properties.table-name", "fir_online_time")!!
            }
            "postgresql", "psql" -> {
                JDBC_URL = settings.getString("PostgreSQL.jdbc-url")!!
                JDBC_DRIVER = settings.getString("PostgreSQL.jdbc-class")!!
                USERNAME = settings.getString("PostgreSQL.properties.user")!!
                PASSWORD = settings.getString("PostgreSQL.properties.password")!!
                TABLE_NAME = settings.getString("PostgreSQL.properties.table-name", "fir_online_time")!!
            }
            else -> { // SQLite 或其他
                JDBC_URL = "jdbc:sqlite:${FirOnlineTime.instance.dataFolder}/data.db"
                JDBC_DRIVER = "org.sqlite.JDBC"
                USERNAME = ""
                PASSWORD = ""
                TABLE_NAME = settings.getString("SQLite.table-name", "fir_online_time")!!
            }
        }
    }
}
