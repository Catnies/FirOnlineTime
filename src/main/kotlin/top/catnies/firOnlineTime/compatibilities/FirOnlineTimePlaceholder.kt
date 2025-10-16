package top.catnies.firOnlineTime.compatibilities

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import top.catnies.firOnlineTime.api.FirOnlineTimeAPI
import top.catnies.firOnlineTime.database.QueryType
import top.catnies.firOnlineTime.utils.TimeUtil
import java.util.regex.Pattern

class FirOnlineTimePlaceholder private constructor() : PlaceholderExpansion() {

    companion object {
        val instance: FirOnlineTimePlaceholder by lazy { FirOnlineTimePlaceholder().apply { register() } }

        // 正则模式用于匹配 week_format_XXX 格式
        private val TIME_FORMAT_PATTERN = Pattern.compile("^(today|week|month|total)_format_(.+)$")
    }

    override fun getIdentifier() = "fotime"
    override fun getAuthor() = "Catnies"
    override fun getVersion()= "1.0.0"
    override fun persist() = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) { return null }

        return when (params) {
            // 当日
            "today" -> FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.TODAY).let { TimeUtil.formatTimeByMillis(it) }
            "today_value" -> FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.TODAY).toString()
            "today_days" -> FirOnlineTimeAPI.getPlayerOnlineDays(player, QueryType.TODAY).toString()

            // 当周
            "week" -> FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.WEEK).let { TimeUtil.formatTimeByMillis(it) }
            "week_value" -> FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.WEEK).toString()
            "week_days" -> FirOnlineTimeAPI.getPlayerOnlineDays(player, QueryType.WEEK).toString()

            // 当月
            "month" -> FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.MONTH).let { TimeUtil.formatTimeByMillis(it) }
            "month_value" -> FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.MONTH).toString()
            "month_days" -> FirOnlineTimeAPI.getPlayerOnlineDays(player, QueryType.MONTH).toString()

            // 总
            "total" -> FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.TOTAL).let { TimeUtil.formatTimeByMillis(it) }
            "total_value" -> FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.TOTAL).toString()
            "total_days" -> FirOnlineTimeAPI.getPlayerOnlineDays(player, QueryType.TOTAL).toString()

            else -> handleCustomFormat(player, params)
        }

    }


    /**
     * 处理自定义格式的占位符
     */
    private fun handleCustomFormat(player: OfflinePlayer, params: String): String? {
        val matcher = TIME_FORMAT_PATTERN.matcher(params)
        if (matcher.matches()) {
            val timeType = matcher.group(1) // today, week, month, total
            val formatPattern = matcher.group(2)

            val queryType = when (timeType) {
                "today" -> QueryType.TODAY
                "week" -> QueryType.WEEK
                "month" -> QueryType.MONTH
                "total" -> QueryType.TOTAL
                else -> return null
            }

            val timeMillis = FirOnlineTimeAPI.getPlayerOnlineTime(player, queryType)
            return timeMillis?.let { formatCustomTime(it, formatPattern) }
        }

        return null
    }

    /**
     * 根据自定义格式格式化时间 - 使用 { } 符号避免 % 冲突
     * @param millis 毫秒数
     * @param format 格式化字符串
     */
    private fun formatCustomTime(millis: Long, format: String): String {
        val totalSeconds = millis / 1000
        val totalMinutes = totalSeconds / 60
        val totalHours = totalSeconds / 3600
        val totalDays = totalHours / 24

        val days = totalDays
        val remainingHours = totalHours % 24
        val hours = totalHours
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val milliseconds = millis % 1000
        // 支持小数格式
        val preciseDays = millis / (1000.0 * 60 * 60 * 24)
        val preciseHours = millis / (1000.0 * 60 * 60)
        val preciseMinutes = millis / (1000.0 * 60)
        val preciseSeconds = millis / 1000.0

        return format
            // 基本时间单位 - 使用 {d} {h} {m} {s} 格式
            .replace("\\{d\\}".toRegex(), days.toString())
            .replace("\\{h\\}".toRegex(), remainingHours.toString())
            .replace("\\{m\\}".toRegex(), minutes.toString())
            .replace("\\{s\\}".toRegex(), seconds.toString())
            .replace("\\{ms\\}".toRegex(), milliseconds.toString())

            // 总时间单位
            .replace("\\{D\\}".toRegex(), totalDays.toString())
            .replace("\\{H\\}".toRegex(), totalHours.toString())
            .replace("\\{M\\}".toRegex(), totalMinutes.toString())
            .replace("\\{S\\}".toRegex(), totalSeconds.toString())
            .replace("\\{T\\}".toRegex(), millis.toString())

            // 精确时间单位（带小数）
            .replace("\\{\\.1d\\}".toRegex(), "%.1f".format(preciseDays))
            .replace("\\{\\.2d\\}".toRegex(), "%.2f".format(preciseDays))
            .replace("\\{\\.1h\\}".toRegex(), "%.1f".format(preciseHours))
            .replace("\\{\\.2h\\}".toRegex(), "%.2f".format(preciseHours))
            .replace("\\{\\.1m\\}".toRegex(), "%.1f".format(preciseMinutes))
            .replace("\\{\\.2m\\}".toRegex(), "%.2f".format(preciseMinutes))
            .replace("\\{\\.1s\\}".toRegex(), "%.1f".format(preciseSeconds))
            .replace("\\{\\.2s\\}".toRegex(), "%.2f".format(preciseSeconds))

            // 格式化时间字符串
            .replace("\\{HH\\}".toRegex(), "%02d".format(hours))
            .replace("\\{mm\\}".toRegex(), "%02d".format(minutes))
            .replace("\\{ss\\}".toRegex(), "%02d".format(seconds))

            // 转义大括号（如果需要显示 literal { }）
            .replace("\\\\\\{".toRegex(), "{")
            .replace("\\\\\\}".toRegex(), "}")
    }

}