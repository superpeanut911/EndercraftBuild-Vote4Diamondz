package net.endercraftbuild.bukkit;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

public class CommandRunner {
	
	public static void run(String command, Player player, Map<String, String> substitutions) {
		String cmd = Utils.formatMessage(command.substring(1));
		
		for (Entry<String, String> substitution : substitutions.entrySet())
			cmd = cmd.replaceAll(substitution.getKey(), substitution.getValue());
		
		switch (command.charAt(0)) {
		case '%':
			player.chat(cmd);
			break;
			
		case '@':
			player.performCommand(cmd);
			break;
		
		case '^':
			player.getServer().dispatchCommand(player.getServer().getConsoleSender(), cmd);
			break;
		
		case '$':
			player.sendMessage(cmd);
			break;
		
		case '!':
			player.getServer().broadcastMessage(cmd);
			break;
		}
	}
	
}
