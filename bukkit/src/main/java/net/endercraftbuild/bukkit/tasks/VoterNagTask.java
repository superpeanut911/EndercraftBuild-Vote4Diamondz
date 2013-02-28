package net.endercraftbuild.bukkit.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.endercraftbuild.bukkit.CommandRunner;
import net.endercraftbuild.bukkit.Vote4Diamondz;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VoterNagTask extends BukkitRunnable {
	
	private final Vote4Diamondz plugin;
	private int id = -1;

	public VoterNagTask(Vote4Diamondz plugin) {
		this.plugin = plugin;

		Long interval = 20L * plugin.getConfig().getLong("nag-interval", 600L);
		id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, interval, interval);
		if (id == -1)
			plugin.getLogger().warning("Failed to start Vote4Diamondz.VoterNagTask -- players will not be nagged to vote.");
}

	@Override
	public void run() {
		String voterPermission = plugin.getConfig().getString("voter-permission");
		List<String> commands = plugin.getConfig().getStringList("nag-actions");
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (!player.hasPermission(voterPermission)) {
				Map<String, String> substitutions = new HashMap<String, String>();
				substitutions.put("PLAYER", player.getName());
				
				for (String command : commands)
					CommandRunner.run(command, player, substitutions);
			}
		}
	}
	
	public boolean stopTask() {
		if (id == -1)
			return false;
		
		plugin.getServer().getScheduler().cancelTask(id);
		return true;
	}

}
