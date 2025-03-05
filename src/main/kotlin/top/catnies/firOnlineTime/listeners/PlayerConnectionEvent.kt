package top.catnies.firOnlineTime.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.catnies.firOnlineTime.FirOnlineTime

class PlayerConnectionEvent private constructor(): Listener {

    companion object {
        val instance: PlayerConnectionEvent by lazy { PlayerConnectionEvent().apply {
            Bukkit.getServer().pluginManager.registerEvents(this, FirOnlineTime.instance!!)
        } }
    }


    @EventHandler
    fun OnPlayerJoin(event: PlayerJoinEvent) {

    }

    @EventHandler
    fun OnPlayerQuit(event: PlayerQuitEvent) {

    }

}