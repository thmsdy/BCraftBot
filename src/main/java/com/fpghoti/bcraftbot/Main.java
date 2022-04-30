package com.fpghoti.bcraftbot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.fpghoti.bcraftbot.bot.ServerBot;
import com.fpghoti.bcraftbot.command.AddExempt;
import com.fpghoti.bcraftbot.command.BCWhitelist;
import com.fpghoti.bcraftbot.listener.PlayerListener;
import com.fpghoti.bcraftbot.sql.MySQLConnection;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Main extends JavaPlugin {

	private String sqlhost;
	private String sqlport;
	private String sqluser;
	private String sqldatabase;
	private String sqlpassword;
	private String sqltable;
	private String bottoken;
	private String kickmsg;
	private String rolename;
	private String requiredrole;

	private boolean stopTimer = false;
	private boolean assignrole = false;
	private boolean checkrole = false;
	private boolean outputtodiscord = false;
	private boolean mainrecordkeeper = false;
	

	private int mysqlTimer = 1140;

	private ArrayList<String> exempt;

	private MySQLConnection sql;

	public static Logger log = Logger.getLogger("Minecraft");

	ServerBot bot;

	public void onEnable() {
		registerConfig();
		messageSet();
		getSettings();
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		sql = new MySQLConnection(this,sqlhost,sqlport,sqluser,sqlpassword,sqldatabase);
		sql.connect();
		if(!sql.tableExists(sqltable)){
			log(Level.INFO, "Table not found. Creating new table...");
			sql.update("CREATE TABLE " + sqltable + " (DiscordID CHAR(18), MinecraftName VARCHAR(16), UUID CHAR(36), PRIMARY KEY (DiscordID));");
			log(Level.INFO, "Table created!");
		}
		startReconnect();
		bot = new ServerBot(this);
		bot.runBot();
		getCommand("AddExempt").setExecutor(new AddExempt(this));
		getCommand("BCWhitelist").setExecutor(new BCWhitelist(this));
	}

	public void onDisable() {
		try {
			
			bot.shutDown();
			
		}catch(NoClassDefFoundError e) {
			
			// This Should only be an issue when the user replaces the .jar file before shutting the server down.
			// The reason for catching this is to provide information to the user.
			
			log(Level.WARNING, "The plugin .jar file has been removed or replaced since the plugin has been loaded! Everything MIGHT work just fine, but this is not encouraged, and you will not get support for any issues that come from this.");
		}
		long initial = System.currentTimeMillis();
		
		//Give bot enough time to shut down
		while(true) {
		       long current =  System.currentTimeMillis();
		       if(current - initial >= 1000) break;
		}
		stopTimer = true;
		sql.disconnect();
	}

	public void startReconnect() {
		new BukkitRunnable(){
			public void run() {
				if(!stopTimer) {
					if(mysqlTimer >= 1200){
						sql.reconnect();
						mysqlTimer = 0;
					}else{
						mysqlTimer++;
					}
				}
			}
		}.runTaskTimerAsynchronously(this, 1*20, 1*20);
	}

	private void registerConfig() {
		this.getConfig().options().copyDefaults(true);
		config.options().copyHeader(true);
		saveConfig();
	}

	public void log(Level level, String msg) {
		log.log(level, "[BCraftBot] " + msg.replaceAll("§[0-9A-FK-OR]", ""));
	}

	FileConfiguration config = this.getConfig();

	public void messageSet(){
		config.options().header("If CheckRole is true, users will need the Discord role listed in RequiredRole to join the server. AssignRole tells the bot whether or not it should give users the role specified in RoleName upon joining the server.\nIf you wish to limit the bot commands to a specific channel, edit its permissions accordingly.\nMainRecordKeeper tells the bot if it is the main record keeper. If you are using the same database for multiple servers, you will only want one bot to insert new users into the database.");
		if (config.get("Host") == null){
			config.createSection("Host");
			config.set("Host", "0.0.0.0");
		}
		if (config.get("Port") == null){
			config.createSection("Port");
			config.set("Port", "3306");
		}
		if (config.get("User") == null){
			config.createSection("User");
			config.set("User", "username");
		}
		if (config.get("Password") == null){
			config.createSection("Password");
			config.set("Password", "pass12345");
		}
		if (config.get("Database") == null){
			config.createSection("Database");
			config.set("Database", "dbname");
		}
		if (config.get("TableName") == null){
			config.createSection("TableName");
			config.set("TableName", "bcraftbot");
		}
		if (config.get("Bot-Token") == null){
			config.createSection("Bot-Token");
			config.set("Bot-Token", "inserttokenhere");
		}
		if (config.get("CheckRole") == null){
			config.createSection("CheckRole");
			config.set("CheckRole", false);
		}
		if (config.get("RequiredRole") == null){
			config.createSection("RequiredRole");
			config.set("RequiredRole", "Whitelisted");
		}
		if (config.get("ExemptUsernames") == null){
			config.createSection("ExemptUsernames");
			config.set("ExemptUsernames", "test1,test2,test3");
		}
		if (config.get("KickMessage") == null){
			config.createSection("KickMessage");
			config.set("KickMessage", "You are either not a member of the Discord or have not yet verified your account!");
		}
		if (config.get("KickMessage") == null){
			config.createSection("KickMessage");
			config.set("KickMessage", "You are either not a member of the Discord or have not yet verified your account!");
		}
		if (config.get("AssignRole") == null){
			config.createSection("AssignRole");
			config.set("AssignRole", true);
		}
		if (config.get("RoleName") == null){
			config.createSection("RoleName");
			config.set("RoleName", "Craftee");
		}
		if (config.get("OutputToDiscord") == null){
			config.createSection("OutputToDiscord");
			config.set("OutputToDiscord", true);
		}
		if (config.get("MainRecordKeeper") == null){
			config.createSection("MainRecordKeeper");
			config.set("MainRecordKeeper", true);
		}
		if (config.get("Whitelist") == null){
			config.createSection("Whitelist");
			config.set("Whitelist", true);
		}
		this.saveConfig();
	}

	public void getSettings(){
		sqlhost = config.getString("Host");
		sqlport = config.getString("Port");
		sqluser = config.getString("User");
		sqlpassword = config.getString("Password");
		sqldatabase = config.getString("Database");
		sqltable = config.getString("TableName");
		bottoken = config.getString("Bot-Token");
		checkrole = config.getBoolean("CheckRole");
		requiredrole = config.getString("RequiredRole");
		setExemptList(config.getString("ExemptUsernames"));
		kickmsg = config.getString("KickMessage");
		assignrole = config.getBoolean("AssignRole");
		rolename = config.getString("RoleName");
		outputtodiscord = config.getBoolean("OutputToDiscord");
		mainrecordkeeper = config.getBoolean("MainRecordKeeper");
	}

	public void setExemptList(String s) {
		exempt = new ArrayList<String>();
		for(String item: s.split(",")) {
			exempt.add(item);
		}
	} 	

	public String getTableName() {
		return sqltable;
	}

	public boolean checkRole() {
		return checkrole;
	}

	public void addExempt(String username) {
		String updated = config.getString("ExemptUsernames") + "," + username;
		if (config.get("ExemptUsernames") != null){
			config.set("ExemptUsernames", updated);
			this.saveConfig();
		}else {
			log.severe("BCraftBot failed to update the exempt user config!");
		}
		setExemptList(updated);
	}
	
	public void setBCWhitelist(Boolean b) {
		if (config.get("Whitelist") != null){
			config.set("Whitelist", b);
			this.saveConfig();
		}else {
			log.severe("BCraftBot failed to update the whitelist in the config!");
		}
	}
	
	public boolean getBCWhitelist() {
		if (config.get("Whitelist") != null){
			return config.getBoolean("Whitelist");
		}
		return false;
	}

	public ServerBot getBot() {
		return bot;
	}

	public boolean isExempt(String name) {
		for(String item: exempt) {
			if(item.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean assignRole() {
		return assignrole;
	}

	public String getAssignRoleName() {
		return rolename;
	}

	public String getKickMessage() {
		return kickmsg;
	}

	public String getBotToken() {
		return bottoken;
	}

	public MySQLConnection getSQL() {
		return sql;
	}

	public boolean outputToDiscord() {
		return outputtodiscord;
	}
	
	public boolean mainRecordKeeper() {
		return mainrecordkeeper;
	}
	
	public boolean isMember(Player p) {
		String name = p.getName().toLowerCase();
		ArrayList<String> ids = new ArrayList<String>();
		ResultSet rs;
		if(sql.itemExists("MinecraftName", name, sqltable)) {
			rs = sql.query("SELECT * FROM " + sqltable + " HAVING MinecraftName = " + "\'" + name.toLowerCase() + "\';");
			try {
				while (rs.next()) {
					String tempid = rs.getString("DiscordID");
					ids.add(tempid);
				}
			} catch (SQLException e) {
				log(Level.SEVERE, "Error getting Discord IDs from database!");
				e.printStackTrace();
			}
			for(String id : ids) {
				User user = bot.getJDA().getUserById(id);
				if(user != null) {
					for(Guild guild : bot.getJDA().getGuilds()) {
						if(guild.isMember(user)) {
							String uuid = p.getUniqueId().toString();
							sql.update("UPDATE " + sqltable + " SET UUID = " + "\'" + uuid  + "\'" + " WHERE DiscordID = "  + "\'" + id  + "\';");
							return true;
						}
					}
				}
			}
		}

		String uuid = p.getUniqueId().toString();
		if(sql.itemExists("UUID", uuid, sqltable)) {
			ids = new ArrayList<String>();
			rs = sql.query("SELECT * FROM " + sqltable + " WHERE UUID = " + "\'" + uuid + "\';");
			try {
				while (rs.next()) {
					ids.add(rs.getString("DiscordID"));
				}
			} catch (SQLException e) {
				log(Level.SEVERE, "Error getting Discord IDs from database!");
				e.printStackTrace();
			}
			for(String id : ids) {
				User user = bot.getJDA().getUserById(id);
				if(user != null) {
					for(Guild guild : bot.getJDA().getGuilds()) {
						if(guild.isMember(user)) {
							sql.update("UPDATE " + sqltable + " SET MinecraftName = " + "\'" + p.getName().toLowerCase()  + "\'" + " WHERE DiscordID = "  + "\'" + id  + "\';");
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public String getRequiredRole() {
		return requiredrole;
	}

	public User getDiscordUser(Player p) {
		String name = p.getName().toLowerCase();
		ArrayList<String> ids = new ArrayList<String>();
		ResultSet rs = sql.query("SELECT * FROM " + sqltable + " HAVING MinecraftName = " + "\'" + name.toLowerCase() + "\';");
		try {
			while (rs.next()) {
				ids.add(rs.getString("DiscordID"));
			}
		} catch (SQLException e) {
			log(Level.SEVERE, "Error getting Discord IDs from database!");
			e.printStackTrace();
		}
		for(String id : ids) {
			User user = bot.getJDA().getUserById(id);
			if(user != null) {
				for(Guild guild : bot.getJDA().getGuilds()) {
					if(guild.isMember(user)) {
						return user;
					}
				}
			}
		}
		return null;
	}

}