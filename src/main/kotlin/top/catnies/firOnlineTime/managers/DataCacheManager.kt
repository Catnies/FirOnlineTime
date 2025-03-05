package top.catnies.firOnlineTime.managers

import org.bukkit.Bukkit
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.api.PlayerData
import top.catnies.firOnlineTime.database.MysqlDatabase
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


class DataCacheManager private constructor() {

    val onlineCache = ConcurrentHashMap<UUID, PlayerData>()
    val offlineCache = ConcurrentHashMap<UUID, PlayerData>()

    companion object {
        val instance: DataCacheManager by lazy { DataCacheManager().apply {

            // 把缓存更新到数据库中.
            Bukkit.getScheduler().runTaskTimerAsynchronously(FirOnlineTime.instance!!, Runnable {
                onlineCache.values.forEach { MysqlDatabase.instance.updateOnlineTime(it.player) }
            }, 0L, 12000L)

            // 从数据库获取数据更新离线缓存,以防止玩家在其他服务器,但是旧缓存一直没有更新的情况.
            Bukkit.getScheduler().runTaskTimerAsynchronously(FirOnlineTime.instance!!, Runnable {
                offlineCache.values.forEach { it.refreshCache() }
            }, 0L, 12000L)

        } }
    }

}