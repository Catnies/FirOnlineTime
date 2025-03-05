package top.catnies.firOnlineTime.utils

import org.bukkit.configuration.file.YamlConfiguration
import top.catnies.firOnlineTime.FirOnlineTime
import java.io.File

object ConfigUtil {

    // 读取配置文件
    fun registerConfig(filePath: String): YamlConfiguration {
        val file: File = File(FirOnlineTime.instance!!.dataFolder, filePath)
        if (!file.exists()) FirOnlineTime.instance!!.saveResource(filePath, false)
        return YamlConfiguration.loadConfiguration(file)
    }

}