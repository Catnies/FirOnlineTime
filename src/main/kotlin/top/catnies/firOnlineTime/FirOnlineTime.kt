package top.catnies.firOnlineTime

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import top.catnies.firOnlineTime.commands.ReloadCommand
import top.catnies.firOnlineTime.compatibilities.FirOnlineTimePlaceholder
import top.catnies.firOnlineTime.database.MysqlDatabase
import top.catnies.firOnlineTime.listeners.PlayerConnectionListener
import top.catnies.firOnlineTime.managers.DataCacheManager
import top.catnies.firOnlineTime.managers.SettingsManager

class FirOnlineTime : JavaPlugin() {

    companion object {
        @JvmField
        var instance: FirOnlineTime? = null
    }


    override fun onEnable() {
        instance = this
        // Config
        SettingsManager.instance
        // Data
        MysqlDatabase.instance
        DataCacheManager.instance
        // Listener
        PlayerConnectionListener.instance
        // COMMANDS
        getCommand("fironlinetime")?.setExecutor(ReloadCommand())
        getCommand("fironlinetime")?.tabCompleter = ReloadCommand()
        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) { FirOnlineTimePlaceholder.instance }
    }

    override fun onDisable() {


    }

}
