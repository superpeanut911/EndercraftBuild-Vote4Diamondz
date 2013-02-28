package net.endercraftbuild.bungee.commands;

import net.endercraftbuild.bungee.Vote4Diamondz;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReloadCommand extends Command {
	
	private final Vote4Diamondz plugin;
	
	public ReloadCommand(Vote4Diamondz plugin) {
		super("gvreload", "ecb.gvreload", "gvr");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer))
			return;
		
		plugin.reload();
		sender.sendMessage(ChatColor.GREEN + "ECB Bukkit Vote4Diamondz configuration reloaded.");
	}

}
