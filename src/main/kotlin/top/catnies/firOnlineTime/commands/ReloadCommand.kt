package top.catnies.firOnlineTime.commands
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import top.catnies.firOnlineTime.FirOnlineTime
import top.catnies.firOnlineTime.managers.SettingsManager

class ReloadCommand: CommandExecutor, TabExecutor {
	override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
		val completionList: MutableList<String> = mutableListOf()

		if (args.size == 1) {
			completionList.add("reload")
			return completionList
		}

		return null
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if (args.isEmpty()) {
			Bukkit.dispatchCommand(sender, "version FirOnlineTime")
		}

		if (args.size == 1) {
			if (args[0].equals("reload", ignoreCase = true)) {
				SettingsManager.instance.reload()
				if (sender is Player) {
					sender.sendMessage("[FirOnlineTime] Config reloaded!")
				}

				FirOnlineTime.instance!!.logger.info("Config reloaded!")
			}
		}

		return true
	}
}