package top.catnies.firOnlineTime.api

import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.database.PlayerData
import top.catnies.firOnlineTime.managers.DataCacheManager

enum class OnlineTimeType { TODAY, WEEK, MONTH, TOTAL }

object FirOnlineTimeAPI {

    /**
     * 获取玩家的在线时长
     */
    fun getPlayerOnlineTime(player: OfflinePlayer, type: OnlineTimeType): Long {
        // 离线
        if (!player.isOnline) {
            // 获取缓存
            val data = DataCacheManager.instance.offlineCache[player.uniqueId] ?: PlayerData.createOfflineData(player)

            // 检查缓存是否过期
            if (data.isExpired()) {
                // 过期, 更新缓存
                data.refreshCache()
            }

            // 返回
            return when (type) {
                OnlineTimeType.TODAY -> data.getTodayOnlineTime()
                OnlineTimeType.WEEK -> data.getWeekOnlineTime()
                OnlineTimeType.MONTH -> data.getMonthOnlineTime()
                OnlineTimeType.TOTAL -> data.getTotalOnlineTime()
            }
        }

        // 在线
        else {
            // 获取缓存
            val data = DataCacheManager.instance.onlineCache[player.uniqueId]!!

            // 检查缓存是否过期
            if (data.isExpired()) {
                // 过期, 更新缓存
                data.refreshCache()
            }

            // 返回
            return when (type) {
                OnlineTimeType.TODAY -> data.getTodayOnlineTime()
                OnlineTimeType.WEEK -> data.getWeekOnlineTime()
                OnlineTimeType.MONTH -> data.getMonthOnlineTime()
                OnlineTimeType.TOTAL -> data.getTotalOnlineTime()
            }
        }
    }

}