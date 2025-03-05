package top.catnies.firOnlineTime.database

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import top.catnies.firOnlineTime.utils.TimeUtil

open class PlayerData private constructor(
    val player: OfflinePlayer,
    var isOnline: Boolean,

    var loginTime: Long?,

    var savedTodayOnlineTime: Long,
    var savedWeekOnlineTime: Long,
    var savedMonthOnlineTime: Long,
    var savedTotalOnlineTime: Long,

    var dataRefreshTime: Long
) {

    companion object {
        // 创建一个新的在线玩家缓存数据
        fun createOnlineData(player: Player, loginTime: Long): PlayerData {
            val data = PlayerData(
                player,
                true,

                loginTime,

                MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.DAY),
                MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.WEEK),
                MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.MONTH),
                MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.TOTAL),

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

                MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.DAY),
                MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.WEEK),
                MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.MONTH),
                MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.TOTAL),

                System.currentTimeMillis()
            )
            return data
        }
    }


    // 判断缓存是否因为跨日期而过期
    fun isExpired(): Boolean {
        // 刷新时间小于今天的0点, 说明数据是昨天的, 过期了
        if (dataRefreshTime <= TimeUtil.getTodayStartTimestamp()) { return true }
        // 在线, 但是记录的登录时间小于今天的0点, 说明登录时间是昨天的, 查询时间也是昨天的, 过期了
        if (isOnline) {
            if (loginTime!! < TimeUtil.getTodayStartTimestamp()) { return true }
        }
        return false
    }


    // 刷新缓存
    fun refreshCache() {
        savedTodayOnlineTime = MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.DAY)
        savedWeekOnlineTime = MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.WEEK)
        savedMonthOnlineTime = MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.MONTH)
        savedTotalOnlineTime = MysqlDatabase.instance.queryPlayerData(player, TimeUtil.getNowSQLDate(), QueryType.TOTAL)
        dataRefreshTime = System.currentTimeMillis()
    }


    // 保存数据
    fun saveData() {
        MysqlDatabase.instance.updateOnlineTime(this.player)
    }


    // 获取当日在线时长
    fun getTodayOnlineTime(): Long {
        if (isOnline) {
            return System.currentTimeMillis() - loginTime!! + savedTodayOnlineTime
        }
        return savedTodayOnlineTime
    }

    // 获取本周在线时长
    fun getWeekOnlineTime(): Long {
        if (isOnline) {
            return System.currentTimeMillis() - loginTime!! + savedWeekOnlineTime
        }
        return savedWeekOnlineTime
    }

    // 获取本月在线时长
    fun getMonthOnlineTime(): Long {
        if (isOnline) {
            return System.currentTimeMillis() - loginTime!! + savedMonthOnlineTime
        }
        return savedMonthOnlineTime
    }

    // 获取总在线时长
    fun getTotalOnlineTime(): Long {
        if (isOnline) {
            return System.currentTimeMillis() - loginTime!! + savedTotalOnlineTime
        }
        return savedTotalOnlineTime
    }
}