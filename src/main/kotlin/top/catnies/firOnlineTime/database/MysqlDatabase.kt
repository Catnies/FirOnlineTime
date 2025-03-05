package top.catnies.firOnlineTime.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.configuration.file.YamlConfiguration
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.utils.ConfigUtil
import java.sql.SQLException

class MysqlDatabase private constructor(){

    private val TABLENAME: String = "firOnlineTime"

    private lateinit var config: YamlConfiguration
    private lateinit var mysql: HikariDataSource

    private lateinit var JDBC_URL: String
    private lateinit var JDBC_DRIVER: String
    private lateinit var USERNAME: String
    private lateinit var PASSWORD: String

    companion object {
        val instance: MysqlDatabase by lazy { MysqlDatabase().apply {
            reload()
            createTable()
        } }
    }

    fun reload() {
        config = ConfigUtil.registerConfig("settings.yml")

        JDBC_URL = config.getString("MySQL.jdbc-url")!!
        JDBC_DRIVER = config.getString("MySQL.jdbc-class")!!
        USERNAME = config.getString("MySQL.properties.user")!!
        PASSWORD = config.getString("MySQL.properties.password")!!

        try {
            val hikariConfig = HikariConfig()
            hikariConfig.jdbcUrl = JDBC_URL
            hikariConfig.driverClassName = JDBC_DRIVER
            hikariConfig.username = USERNAME
            hikariConfig.password = PASSWORD
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
                    CREATE TABLE IF NOT EXISTS $TABLENAME (id INT AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(255) NOT NULL, date Date NOT NULL, onlineTime BIGINT NOT NULL)
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


}