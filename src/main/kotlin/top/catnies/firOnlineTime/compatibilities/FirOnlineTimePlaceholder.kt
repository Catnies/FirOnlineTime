package top.catnies.firOnlineTime.compatibilities

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.api.FirOnlineTimeAPI
import top.catnies.firOnlineTime.api.OnlineTimeType
import top.catnies.firOnlineTime.utils.TimeUtil

class FirOnlineTimePlaceholder private constructor() : PlaceholderExpansion() {

    companion object {
        val instance: FirOnlineTimePlaceholder by lazy { FirOnlineTimePlaceholder().apply { register() } }
    }

    override fun getIdentifier() = "fotime"
    override fun getAuthor() = "Catnies"
    override fun getVersion()= "1.0.0"

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) { return null }

        when (params) {
            // 当日
            "daily" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, OnlineTimeType.TODAY)
                return TimeUtil.formatTimeByMillis(onlineTime)
            }
            "daily_value" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, OnlineTimeType.TODAY)
                return onlineTime.toString()
            }
            // 当周
            "weekly" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, OnlineTimeType.WEEK)
                return TimeUtil.formatTimeByMillis(onlineTime)
            }
            "weekly_value" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, OnlineTimeType.WEEK)
                return onlineTime.toString()
            }
            // 当月
            "monthly" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, OnlineTimeType.MONTH)
                return TimeUtil.formatTimeByMillis(onlineTime)
            }
            "monthly_value" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, OnlineTimeType.MONTH)
                return onlineTime.toString()
            }
            // 总
            "total" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, OnlineTimeType.TOTAL)
                return TimeUtil.formatTimeByMillis(onlineTime)
            }
            "total_value" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, OnlineTimeType.TOTAL)
                return onlineTime.toString()
            }
        }
        return null
    }

}