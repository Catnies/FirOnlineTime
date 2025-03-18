package top.catnies.firOnlineTime.api

import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.database.PlayerData
import top.catnies.firOnlineTime.database.QueryType
import top.catnies.firOnlineTime.managers.DataCacheManager


object FirOnlineTimeAPI {

    /**
     * 获取玩家的在线时长
     */
    fun getPlayerOnlineTime(player: OfflinePlayer, type: QueryType): Long {
        // 获取玩家缓存
        val data = if (player.isOnline) {
            DataCacheManager.instance.onlineCache[player.uniqueId]!!
        } else {
            DataCacheManager.instance.offlineCache[player.uniqueId] ?: PlayerData.createOfflineData(player) // 没有离线缓存就新建离线缓存
        }

        // 检查缓存是否过期
        if (data.isExpired()) data.saveAndRefreshCache()

        // 根据类型返回不同的在线时长
        return when (type) {
            QueryType.TODAY -> data.getTodayOnlineTime()
            QueryType.WEEK -> data.getWeekOnlineTime()
            QueryType.MONTH -> data.getMonthOnlineTime()
            QueryType.TOTAL -> data.getTotalOnlineTime()
        }
    }

}