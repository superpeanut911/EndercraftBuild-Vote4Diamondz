package net.endercraftbuild.bukkit.listeners;

import java.util.HashMap;
import java.util.Map;

import net.endercraftbuild.bukkit.CommandRunner;
import net.endercraftbuild.bukkit.Vote4Diamondz;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class VoteMessageListener implements PluginMessageListener {
	
	private final Vote4Diamondz plugin;
	
	public VoteMessageListener(Vote4Diamondz plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		String votingPlayerName = new String(message);
		
		Player votingPlayer = plugin.getServer().getPlayer(votingPlayerName);
		
		if (votingPlayer == null) {
			plugin.getServer().getLogger().severe(String.format("%s channel message received via %s's stream indicated %s was online but they are not.", Vote4Diamondz.CHANNEL, player.getName(), votingPlayerName));
			return;
		}
		
		Map<String, String> substitutions = new HashMap<String, String>();
		substitutions.put("PLAYER", votingPlayer.getName());
		
		for (String command : plugin.getConfig().getStringList("voting-reward-actions"))
			CommandRunner.run(command, votingPlayer, substitutions);
	}

}
