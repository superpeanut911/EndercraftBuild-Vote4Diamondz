package net.endercraftbuild.bukkit.commands;

import net.endercraftbuild.bukkit.Utils;
import net.endercraftbuild.bukkit.Vote4Diamondz;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommandExecutor implements CommandExecutor {
	
	private final Vote4Diamondz plugin;
	
	public ReloadCommandExecutor(Vote4Diamondz plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player && !((Player) sender).hasPermission("ecb.v4b.reload")) {
			sender.sendMessage(command.getPermissionMessage());
			return true;
		}
		
		plugin.reload();
		sender.sendMessage(Utils.formatMessage("&aECB Bukkit Vote4Diamondz configuration reloaded."));
		
		return true;
	}

}
