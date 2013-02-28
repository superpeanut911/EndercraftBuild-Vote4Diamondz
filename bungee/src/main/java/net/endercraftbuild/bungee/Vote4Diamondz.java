package net.endercraftbuild.bungee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.eclipse.jetty.server.Server;

import net.endercraftbuild.bungee.commands.ReloadCommand;
import net.endercraftbuild.bungee.database.*;
import net.endercraftbuild.bungee.handlers.VoteHandler;

import com.alta189.simplesave.Database;
import com.alta189.simplesave.DatabaseFactory;
import com.alta189.simplesave.exceptions.ConnectionException;
import com.alta189.simplesave.exceptions.TableRegistrationException;
import com.alta189.simplesave.mysql.MySQLConfiguration;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class Vote4Diamondz extends Plugin {
	public final static String CHANNEL = "ecb-v4d";
	public final static String CONFIG_FILE = "config.properties"; 
	public final static String VOTER_HTML_FILE = "vote.html";
	public final static String SITES_FILE = "sites.txt";
	
    public static int getTime() {
        return (int) (System.currentTimeMillis() / 1000L);
    }
    
    private ReloadCommand reloadCommand; 
    
	private Properties properties;
	private Server webServer;
    private Database database;
    
    private byte[] voterHtml;
    private Map<String, String> sites;
    	
	@Override
	public void onEnable() {
		copyResource(CONFIG_FILE);
		copyResource(VOTER_HTML_FILE);
		copyResource(SITES_FILE);
		
		ProxyServer.getInstance().getPluginManager().registerCommand(reloadCommand = new ReloadCommand(this));
		ProxyServer.getInstance().registerChannel(CHANNEL);
		startWebServer();
	}
	
	@Override
	public void onDisable() {
		stopWebServer();
		ProxyServer.getInstance().unregisterChannel(CHANNEL);
		ProxyServer.getInstance().getPluginManager().unregisterCommand(reloadCommand);
	}
	
	public void copyResource(String name) {
		ProxyServer.getInstance().getLogger().info("Seeing if we need to copy: " + name);
		File resourceDestinationFile = new File(getResource(name));
		
		if (!resourceDestinationFile.getParentFile().exists())
			resourceDestinationFile.getParentFile().mkdirs();
		
		if (resourceDestinationFile.exists())
			return;

		try {
			ProxyServer.getInstance().getLogger().info(this.getClass().getClassLoader().getResource(name).toString());
			InputStream resourceInputStream = this.getClass().getClassLoader().getResourceAsStream(name);
			OutputStream resourceOutputStream = new FileOutputStream(resourceDestinationFile);
			int readBytes;
			byte[] buffer = new byte[4096];
			while ((readBytes = resourceInputStream.read(buffer)) > 0)
				resourceOutputStream.write(buffer, 0, readBytes);
			resourceOutputStream.close();
			resourceInputStream.close();
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().warning("Failed to copy default resource: " + e.getLocalizedMessage());
		}
	}
	
	public String getResource(String name) {
		return "plugins/" + getDescription().getName() + "/" + name;
	}
	
	public Properties getProperties() {
		if (properties != null)
			return properties;
		
		properties = new Properties();
		try {
			properties.load(new FileInputStream(getResource(CONFIG_FILE)));
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().warning("Failed to load configuration: " + e.getLocalizedMessage());
			properties = null;
		}
		
		return properties;
	}
	
	public void reload() {
		stopWebServer();
		
		properties = null;
		voterHtml = null;
		sites = null;
		
		startWebServer();
	}
	
	public Database getDatabase() {
		if (database != null)
			return database;
		
		try {
			database = DatabaseFactory.createNewDatabase(getDatabaseConfiguration());
	        database.registerTable(VoteEntry.class);
	        database.registerTable(VoteHistory.class);
	        database.connect();
		} catch (TableRegistrationException | ConnectionException e) {
			ProxyServer.getInstance().getLogger().warning("Failed to setup database: " + e.getLocalizedMessage());
			e.printStackTrace();
			properties = null;
		}
		
		return database;
	}
	
	public void startWebServer() {
		try {
			final InetSocketAddress bind = new InetSocketAddress(getHost(), getPort());
			webServer = new Server(bind);
			webServer.setHandler(new VoteHandler(this));
			webServer.start();
		} catch (Exception e) {
            ProxyServer.getInstance().getLogger().severe("Could not start vote server: " + e.getLocalizedMessage());
            e.printStackTrace();
            webServer = null;
		}
	}
	
	public void stopWebServer() {
        if (webServer != null) {
            try {
                webServer.stop();
            } catch (Exception e) {
                ProxyServer.getInstance().getLogger().severe("Could not stop vote server: " + e.getLocalizedMessage());
                webServer = null;
            }
        }
	}
	
	public MySQLConfiguration getDatabaseConfiguration() {
		MySQLConfiguration config = new MySQLConfiguration();
		
		config.setDatabase(getProperties().getProperty("voterdb.database"));
		config.setHost(getProperties().getProperty("voterdb.host"));
		config.setPort(Integer.parseInt(getProperties().getProperty("voterdb.port")));
		config.setUser(getProperties().getProperty("voterdb.user"));
		config.setPassword(getProperties().getProperty("voterdb.password"));
		
		return config;
	}
	
	public String getHost() {
		return getProperties().getProperty("host");
	}
	
	public Integer getPort() {
		return Integer.parseInt(getProperties().getProperty("port"));
	}
	
	public Integer getVoteInterval() {
		return Integer.parseInt(getProperties().getProperty("voteInterval"));
	}
	
    public boolean shouldCheckIP() {
    	return Boolean.parseBoolean(getProperties().getProperty("checkIP"));
    }
    
    public byte[] getNotOnlineMessage() {
    	return getProperties().getProperty("notOnline").getBytes();
    }
    
    public byte[] getWrongIPMessage() {
    	return getProperties().getProperty("wrongIP").getBytes();
    }
    
    public byte[] getTooSoonMessage() {
    	return getProperties().getProperty("tooSoon").getBytes();
    }
    
    public byte[] getThanksMessage() {
    	return getProperties().getProperty("thanks").getBytes();
    }
    
    public byte[] getVoterHtml() {
    	if (voterHtml != null)
    		return voterHtml;
    	
    	try {
	        // load page, into ram it goes, minified too
    		BufferedReader sr = new BufferedReader(new FileReader(getResource(VOTER_HTML_FILE)));
	        StringBuilder out = new StringBuilder();
    		String line;
    		while ((line = sr.readLine()) != null) {
    			line = line.trim();
	            out.append(line);
	            // add servers
	            if (line.equals("<!-- Begin sites -->")) {
	                for (Map.Entry<String, String> entry : getSites().entrySet()) {
	                    out.append("<div><img src=\"");
	                    // image
	                    out.append(entry.getValue());
	                    out.append("\" data-site=\"");
	                    // site
	                    out.append(entry.getKey());
	                    out.append("\"></div>");
	                }
	            }
	        }
	        sr.close();
	        voterHtml = out.toString().getBytes();
    	} catch (IOException e) {
			ProxyServer.getInstance().getLogger().warning("Failed to load vote.html: " + e.getLocalizedMessage());
			voterHtml = null;
    	}
    	
    	return voterHtml;
    }
    
    public Map<String, String> getSites() {
    	if (sites != null)
    		return sites;
    	
    	try {
	        sites = new HashMap<String, String>();
	        BufferedReader sr = new BufferedReader(new FileReader(getResource(SITES_FILE)));
	        String line;
	        while ((line = sr.readLine()) != null) {
	            line = line.trim();
	            String[] split = line.split(" ");
	            if (!line.startsWith("#") && split.length == 2) {
	                String site = split[0];
	                String banner = split[1];
	                sites.put(site, banner);
	                ProxyServer.getInstance().getLogger().info("Registered site: " + site + " with banner: " + banner);
	            }
	        }
	        sr.close();
    	} catch (IOException e) {
			ProxyServer.getInstance().getLogger().warning("Failed to load sites.txt: " + e.getLocalizedMessage());
			sites = null;
    	}
    	
    	return sites;
    }

    public VoteEntry getEntry(String user) {
        return getDatabase().select(VoteEntry.class).where().equal("name", user).execute().findOne();
    }

    public boolean canVote(VoteEntry entry) {
        return entry == null || entry.getLastVote() + getVoteInterval() < getTime();
    }
	
}
