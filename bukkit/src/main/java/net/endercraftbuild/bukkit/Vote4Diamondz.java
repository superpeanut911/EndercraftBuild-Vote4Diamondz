package net.endercraftbuild.bukkit;

import java.io.File;

import net.endercraftbuild.bukkit.listeners.VoteMessageListener;
import net.endercraftbuild.bukkit.tasks.VoterNagTask;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Vote4Diamondz extends JavaPlugin {
	
	public static final String CHANNEL = "ecb-v4d";
	
	private VoteMessageListener voteMessageListener;
	private VoterNagTask voterNagTask;
	
	@Override
	public void onEnable() {
		if (!new File(this.getDataFolder().getPath() + File.separatorChar + "config.yml").exists())
			saveDefaultConfig();
		
		voterNagTask = new VoterNagTask(this);
		voteMessageListener = new VoteMessageListener(this);
		Bukkit.getMessenger().registerIncomingPluginChannel(this, CHANNEL, voteMessageListener);
	}
	
	@Override
	public void onDisable() {
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this, CHANNEL, voteMessageListener);
		voteMessageListener = null;
		voterNagTask.stopTask();
		voterNagTask = null;
	}
	
}
