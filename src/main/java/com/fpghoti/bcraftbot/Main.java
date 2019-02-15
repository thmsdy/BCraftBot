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
import com.fpghoti.bcraftbot.listener.PlayerListener;
import com.fpghoti.bcraftbot.sql.MySQLConnection;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

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

	private boolean stopTimer = false;
	private boolean assignrole;

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
	}

	public void onDisable() {
		bot.shutDown();
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
		setExemptList(config.getString("ExemptUsernames"));
		kickmsg = config.getString("KickMessage");
		assignrole = config.getBoolean("AssignRole");
		rolename = config.getString("RoleName");
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