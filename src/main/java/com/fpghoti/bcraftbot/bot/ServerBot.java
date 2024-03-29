package com.fpghoti.bcraftbot.bot;

import java.util.HashMap;
import java.util.logging.Level;

import com.fpghoti.bcraftbot.Main;
import com.fpghoti.bcraftbot.bot.command.AddMeCommand;
import com.fpghoti.bcraftbot.bot.util.CommandParser;
import com.fpghoti.bcraftbot.sql.MySQLConnection;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class ServerBot {
	
	private Main plugin;
	
	private JDA jda;
	private static ShardManager shardm;
	
	private CommandParser parser;
	
	private HashMap<String, Command> commands = new HashMap<String, Command>();	
	
	public ServerBot(Main plugin) {
		this.plugin = plugin;
	}
	
	public void runBot(){
		parser = new CommandParser();
		plugin.log(Level.INFO, "Initializing Discord Bot...");
		String token = plugin.getBotToken();
		plugin.log(Level.INFO, "Connecting bot to Discord...");
		try{
			shardm = DefaultShardManagerBuilder.createDefault(token)
					.setChunkingFilter(ChunkingFilter.ALL)
					.setMemberCachePolicy(MemberCachePolicy.ALL)
					.enableIntents(GatewayIntent.getIntents(GatewayIntent.DEFAULT))
					.enableIntents(GatewayIntent.GUILD_MEMBERS)
					.enableIntents(GatewayIntent.GUILD_MESSAGES)
					.build();
			jda = shardm.getShardById(0);
			try {
				jda.awaitReady();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			jda.addEventListener(new BotListener(this));
			jda.setAutoReconnect(true);
			String link = "https://discordapp.com/oauth2/authorize?&client_id=" + jda.getSelfUser().getId();
			link = link + "&scope=bot&permissions=8";
			plugin.log(Level.INFO, "Connection successful!");
			plugin.log(Level.INFO, "You can add this bot to Discord using this link: ");
			plugin.log(Level.INFO, link);
		}catch(Exception e){
			e.printStackTrace();
			plugin.log(Level.SEVERE, "There was an issue connecting to Discord. Bot shutting down!");
		}
		
		commands.put("addme",  new AddMeCommand(this));
	}

	public void handleCommand(CommandParser.CommandContainer cmd){
		if(commands.containsKey(cmd.invoke)){
			boolean safe = commands.get(cmd.invoke).called(cmd.args, cmd.event);

			if(safe){
				commands.get(cmd.invoke).action(cmd.args, cmd.event);
				commands.get(cmd.invoke).executed(safe, cmd.event);
			}else{
				commands.get(cmd.invoke).executed(safe, cmd.event);
			}
		}
	}
	
	public MySQLConnection getSQL() {
		return plugin.getSQL();
	}
	
	public String getTableName() {
		return plugin.getTableName();
	}
	
	public void log(Level level, String msg) {
		plugin.log(level, msg);
	}
	
	public CommandParser getCommandParser() {
		return parser;
	}
	
	public JDA getJDA() {
		return jda;
	}

	public void shutDown(){
		jda.shutdownNow();
	}
	
	public Main getPlugin() {
		return plugin;
	}
	
}
