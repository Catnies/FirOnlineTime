package top.catnies.firOnlineTime.database

import org.bukkit.OfflinePlayer
import java.sql.Date

interface Database {

    fun upsertOnlineTime(player: OfflinePlayer, date: Date, addTime: Long)

    fun queryPlayerData(player: OfflinePlayer, baseDate: Date, queryType: QueryType): Long

    fun queryOnlineDays(player: OfflinePlayer, baseDate: Date, queryType: QueryType): Int

    fun queryOnlineDays(player: OfflinePlayer, startDate: Date, endDate: Date): Int

    fun saveAndRefreshOnlineCache(player: OfflinePlayer, systemNow: Long = System.currentTimeMillis())

}