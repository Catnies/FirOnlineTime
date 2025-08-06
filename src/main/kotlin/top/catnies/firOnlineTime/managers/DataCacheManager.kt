package top.catnies.firOnlineTime.managers

import cn.chengzhiya.mhdfscheduler.task.MHDFTask
import top.catnies.firOnlineTime.database.PlayerData
import top.catnies.firOnlineTime.utils.TaskUtils
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


class DataCacheManager private constructor() {

    val onlineCache = ConcurrentHashMap<UUID, PlayerData>()
    val offlineCache = ConcurrentHashMap<UUID, PlayerData>()

    lateinit var onlineTask: MHDFTask
    lateinit var offlineTask: MHDFTask

    companion object {
        val instance: DataCacheManager by lazy { DataCacheManager().apply {
            // 把在线缓存定期更新到数据库中, 同时刷新缓存的 lastSavedTime 字段;
            onlineTask = TaskUtils.runTimerAsyncTask({
                onlineCache.values.forEach { it.saveAndRefreshCache() }
            }, 0L, SettingsManager.instance.CacheUpdateInterval.toLong() * 20L)

            // 定期从数据库获取数据更新服务器中的离线缓存, 以防止玩家在其他服务器, 但是旧缓存一直没有更新的情况;
            offlineTask = TaskUtils.runTimerAsyncTask({
                offlineCache.values.forEach { it.saveAndRefreshCache() }
            }, 0L, SettingsManager.instance.CacheUpdateInterval.toLong() * 20L)
        } }
    }

}