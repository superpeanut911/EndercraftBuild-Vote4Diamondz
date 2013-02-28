package net.endercraftbuild.bukkit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {
	
	public static String formatMessage(String message, Object... args) {
		return ChatColor.translateAlternateColorCodes('&', String.format(message, args));
	}

	public static void sendMessage(Player player, String message, Object... args) {
		player.sendMessage(formatMessage(message, args));
	}

}
