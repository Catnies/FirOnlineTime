package top.catnies.firOnlineTime.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.api.PlayerData
import top.catnies.firOnlineTime.database.MysqlDatabase
import top.catnies.firOnlineTime.managers.DataCacheManager

class PlayerConnectionListener private constructor(): Listener {

    companion object {
        val instance: PlayerConnectionListener by lazy { PlayerConnectionListener().apply {
            Bukkit.getServer().pluginManager.registerEvents(this, FirOnlineTime.instance!!)
        } }
    }


    /**
     * 玩家登录时, 移除服务器内的离线缓存; 创建新的在线缓存.
     */
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        Bukkit.getScheduler().runTaskAsynchronously(FirOnlineTime.instance!!, Runnable {
            DataCacheManager.instance.offlineCache.remove(event.player.uniqueId)
            DataCacheManager.instance.onlineCache[event.player.uniqueId] = PlayerData.createOnlineData(event.player, System.currentTimeMillis())
        })
    }


    /**
     * 玩家退出时, 保存数据, 然后移除服务器内的在线缓存;
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        Bukkit.getScheduler().runTaskAsynchronously(FirOnlineTime.instance!!, Runnable {
            MysqlDatabase.instance.updateOnlineTime(player = event.player, systemNow = System.currentTimeMillis())
            DataCacheManager.instance.onlineCache.remove(event.player.uniqueId)
        })
    }

}