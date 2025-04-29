package top.catnies.firOnlineTime.api

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.database.MysqlDatabase
import top.catnies.firOnlineTime.database.PlayerData
import top.catnies.firOnlineTime.database.QueryType
import top.catnies.firOnlineTime.managers.DataCacheManager
import java.sql.Date
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


object FirOnlineTimeAPI {

    /**
     * 获取玩家的在线时长, 同步缓存获取
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


    /**
     * 获取玩家的在线时长, 同步阻塞获取
     */
    fun getPlayerOnlineTime(player: OfflinePlayer, type: QueryType, baseDate: Date): Long {
        return MysqlDatabase.instance.queryPlayerData(player, baseDate, type)
    }


    /**
     * 获取玩家的在线时长, 异步获取
     */
    fun getPlayerOnlineTimeAsync(player: OfflinePlayer, type: QueryType, baseDate: Date): CompletableFuture<Long> {
        val future = CompletableFuture<Long>()
        Bukkit.getScheduler().runTaskAsynchronously (FirOnlineTime.instance!!, Runnable {
            val playerData = MysqlDatabase.instance.queryPlayerData(player, baseDate, type)
            future.complete(playerData)
        } )
        return future.orTimeout(5, TimeUnit.SECONDS)
    }



}