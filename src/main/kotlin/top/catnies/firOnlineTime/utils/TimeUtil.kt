package top.catnies.firOnlineTime.utils

import java.sql.Date
import java.time.*
import java.time.temporal.TemporalAdjusters

object TimeUtil {

    /**
     * 获取某个时间戳代表的日期的明天00点的时间戳,单位毫秒.
     * @param timestamp 时间戳
     * @return 明天00点的时间戳
     */
    fun getTomorrowStartByTimeMillisStamp(timestamp: Long): Long {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()
        )
        val tomorrowMidnight = dateTime.plusDays(1).toLocalDate().atStartOfDay()
        return tomorrowMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * 获取当天00点的时间戳
     * @return 当天00点的时间戳
     */
    fun getTodayStartTimestamp(): Long {
        val today = LocalDate.now()
        val midnight = LocalDateTime.of(today, LocalTime.MIDNIGHT)
        val timestamp = midnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return timestamp
    }

    /**
     * 通过一个毫秒值获取时间长度, 并格式化为 xx天 xx小时xx分钟xx秒
     * @param millis 毫秒值
     * @return 格式化后的时间长度
     */
    fun formatTimeByMillis(millis: Long): String {
        var seconds = millis / 1000
        var minutes = seconds / 60
        var hours = minutes / 60
        val days = hours / 24
        hours %= 24
        minutes %= 60
        seconds %= 60
        return String.format("%d天 %02d小时%02d分钟%02d秒", days, hours, minutes, seconds)
    }

    /**
     * 获取当前时间的 SQL 格式的日期
     * @return SQL 格式的日期
     */
    fun getNowSQLDate(): Date {
        val nowDate = LocalDate.now(ZoneId.of("GMT+8"))
        return Date.valueOf(nowDate)
    }


    /**
     * 获取所在周的起始日期（周一）
     * @param date 当前日期
     * @return 所在周的起始日期
     */
    fun getWeekStart(date: Date): Date {
        val startOfWeek = date.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return Date.valueOf(startOfWeek)
    }

    /**
     * 获取所在周的结束日期（周日）
     * @param date 当前日期
     * @return 所在周的结束日期
     */
    fun getWeekEnd(date: Date): Date {
        val endOfWeek = date.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return Date.valueOf(endOfWeek)
    }

    /**
     * 获取所在月的起始日期（第一天）
     * @param date 当前日期
     * @return 所在月的起始日期
     */
    fun getMonthStart(date: Date): Date {
        val startOfMonth = date.toLocalDate().withDayOfMonth(1)
        return Date.valueOf(startOfMonth)
    }

    /**
     * 获取所在月的结束日期（最后一天）
     * @param date 当前日期
     * @return 所在月的结束日期
     */
    fun getMonthEnd(date: Date): Date {
        val endOfMonth = date.toLocalDate().withDayOfMonth(date.toLocalDate().lengthOfMonth())
        return Date.valueOf(endOfMonth)
    }
}