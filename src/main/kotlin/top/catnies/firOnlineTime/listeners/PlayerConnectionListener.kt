package top.catnies.firOnlineTime.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.database.PlayerData
import top.catnies.firOnlineTime.managers.DataCacheManager
import top.catnies.firOnlineTime.managers.DatabaseManager
import top.catnies.firOnlineTime.utils.TaskUtils

class PlayerConnectionListener private constructor(): Listener {

    companion object {
        val database = DatabaseManager.instance.database
        val instance: PlayerConnectionListener by lazy {
            PlayerConnectionListener().apply {
                Bukkit.getServer().pluginManager.registerEvents(this, FirOnlineTime.instance)
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        TaskUtils.runAsyncTask {
            DataCacheManager.instance.offlineCache.remove(event.player.uniqueId)
            DataCacheManager.instance.onlineCache[event.player.uniqueId] =
                PlayerData.createOnlineData(event.player, System.currentTimeMillis())
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        TaskUtils.runAsyncTask {
            // 通过数据库任务队列保存数据
            database.saveAndRefreshOnlineCache(player = event.player, systemNow = System.currentTimeMillis())
            DataCacheManager.instance.onlineCache.remove(event.player.uniqueId)
        }
    }
}
