package top.catnies.firOnlineTime.managers

import org.bukkit.configuration.file.YamlConfiguration
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.utils.ConfigUtil
import kotlin.properties.Delegates

class SettingsManager private constructor() {

    var settings by Delegates.notNull<YamlConfiguration>()
    var configVersion by Delegates.notNull<Double>()
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
        
        // 检查配置版本，默认为1.0
        configVersion = settings.getDouble("config-version", 1.0)
        
        // 如果版本为1.0，发出警告
        if (configVersion < 2.0) {
            FirOnlineTime.instance.logger.warning("使用旧版配置 (v$configVersion)，某些新功能可能不可用")
            FirOnlineTime.instance.logger.warning("建议备份当前配置并删除配置文件以生成新版配置")
        }
        
        CacheUpdateInterval = settings.getInt("CacheUpdateInterval", 600)
        OfflinePlayerUpdateInterval = settings.getInt("OfflinePlayerUpdateInterval", 600)

        // Database - 根据配置版本选择不同的配置方式
        DatabaseType = settings.getString("DatabaseType", "SQLite")!!
        
        when {
            // 版本2.0+的配置方式
            configVersion >= 2.0 -> {
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
            // 版本1.0的配置方式 (兼容旧版)
            else -> {
                // 如果尝试使用PostgreSQL但配置版本是1.0，则回退到SQLite并发出警告
                if (DatabaseType.equals("postgresql", true) || DatabaseType.equals("psql", true)) {
                    FirOnlineTime.instance.logger.warning("旧版配置不支持PostgreSQL，已自动回退到SQLite")
                    DatabaseType = "SQLite"
                }
                
                // 1.0版本只支持MySQL和SQLite
                when (DatabaseType.lowercase()) {
                    "mysql" -> {
                        JDBC_URL = settings.getString("MySQL.jdbc-url")!!
                        JDBC_DRIVER = settings.getString("MySQL.jdbc-class")!!
                        USERNAME = settings.getString("MySQL.properties.user")!!
                        PASSWORD = settings.getString("MySQL.properties.password")!!
                        TABLE_NAME = settings.getString("MySQL.properties.table-name", "fir_online_time")!!
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
    }
}
