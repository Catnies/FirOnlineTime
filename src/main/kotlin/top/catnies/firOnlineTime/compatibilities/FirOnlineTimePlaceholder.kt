package top.catnies.firOnlineTime.compatibilities

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.api.FirOnlineTimeAPI
import top.catnies.firOnlineTime.database.QueryType
import top.catnies.firOnlineTime.utils.TimeUtil

class FirOnlineTimePlaceholder private constructor() : PlaceholderExpansion() {

    companion object {
        val instance: FirOnlineTimePlaceholder by lazy { FirOnlineTimePlaceholder().apply { register() } }
    }

    override fun getIdentifier() = "fotime"
    override fun getAuthor() = "Catnies"
    override fun getVersion()= "1.0.0"
    override fun persist() = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) { return null }

        when (params) {
            // 当日
            "today" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.TODAY)
                return TimeUtil.formatTimeByMillis(onlineTime)
            }
            "today_value" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.TODAY)
                return onlineTime.toString()
            }
            // 当周
            "week" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.WEEK)
                return TimeUtil.formatTimeByMillis(onlineTime)
            }
            "week_value" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.WEEK)
                return onlineTime.toString()
            }
            // 当月
            "month" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.MONTH)
                return TimeUtil.formatTimeByMillis(onlineTime)
            }
            "month_value" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.MONTH)
                return onlineTime.toString()
            }
            // 总
            "total" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.TOTAL)
                return TimeUtil.formatTimeByMillis(onlineTime)
            }
            "total_value" -> {
                val onlineTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.TOTAL)
                return onlineTime.toString()
            }
        }
        return null
    }

}