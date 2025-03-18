package top.catnies.firOnlineTime.database

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import top.catnies.firOnlineTime.managers.DatabaseManager
import top.catnies.firOnlineTime.utils.TimeUtil

open class PlayerData private constructor(
    val player: OfflinePlayer,
    var isOnline: Boolean,

    // 仅在线玩家存在
    var lastSavedTime: Long?,

    var savedTodayOnlineTime: Long,
    var savedWeekOnlineTime: Long,
    var savedMonthOnlineTime: Long,
    var savedTotalOnlineTime: Long,

    var dataRefreshTime: Long
) {

    companion object {
        val database = DatabaseManager.instance.database
        
        // 创建一个新的在线玩家缓存数据
        fun createOnlineData(player: Player, loginTime: Long): PlayerData {
            val data = PlayerData(
                player,
                true,

                loginTime,

                database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.TODAY),
                database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.WEEK),
                database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.MONTH),
                database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.TOTAL),

                System.currentTimeMillis()
            )
            return data
        }

        // 创建一个离线玩家缓存数据
        fun createOfflineData(player: OfflinePlayer): PlayerData {
            val data = PlayerData(
                player,
                false,

                null,

                database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.TODAY),
                database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.WEEK),
                database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.MONTH),
                database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.TOTAL),

                System.currentTimeMillis()
            )
            return data
        }
    }


    // 判断缓存是否因为跨日期而过期
    fun isExpired(): Boolean {
        // 在线缓存, 如果 lastSavedTime 的日期和今天的日期不一致, 说明是昨天的, 过期了.
        if (isOnline && TimeUtil.isExpire(lastSavedTime!!)) return true
        // 离线缓存, 如果 dataRefreshTime 的日期和今天的日期不一致, 说明是昨天的, 过期了.
        if (!isOnline && TimeUtil.isExpire(dataRefreshTime)) return true
        return false
    }


    // 保存数据, 然后刷新缓存.
    fun saveAndRefreshCache() {
        // 离线缓存刷新, 只需要重新从数据库获取最新的数据即可.
        if (!isOnline) {
            savedTodayOnlineTime = database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.TODAY)
            savedWeekOnlineTime = database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.WEEK)
            savedMonthOnlineTime = database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.MONTH)
            savedTotalOnlineTime = database.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.TOTAL)
            dataRefreshTime = System.currentTimeMillis()
        }
        // 在线缓存刷新, 需要计算出昨日的在线时间和今日在线时间, 然后保存到数据库中.
        else {
            database.saveAndRefreshOnlineCache(this.player)
        }
    }


    // 获取当日在线时长
    fun getTodayOnlineTime(): Long {
        if (isOnline) {
            return System.currentTimeMillis() - lastSavedTime!! + savedTodayOnlineTime
        }
        return savedTodayOnlineTime
    }

    // 获取本周在线时长
    fun getWeekOnlineTime(): Long {
        if (isOnline) {
            return System.currentTimeMillis() - lastSavedTime!! + savedWeekOnlineTime
        }
        return savedWeekOnlineTime
    }

    // 获取本月在线时长
    fun getMonthOnlineTime(): Long {
        if (isOnline) {
            return System.currentTimeMillis() - lastSavedTime!! + savedMonthOnlineTime
        }
        return savedMonthOnlineTime
    }

    // 获取总在线时长
    fun getTotalOnlineTime(): Long {
        if (isOnline) {
            return System.currentTimeMillis() - lastSavedTime!! + savedTotalOnlineTime
        }
        return savedTotalOnlineTime
    }
}