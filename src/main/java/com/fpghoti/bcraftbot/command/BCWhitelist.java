package com.fpghoti.bcraftbot.command;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.fpghoti.bcraftbot.Main;

public class BCWhitelist implements CommandExecutor{

	private Main plugin;

	public BCWhitelist(Main plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(label.equalsIgnoreCase("bcwhitelist")){
			if(sender instanceof Player) {
				if(!((Player)sender).hasPermission("bcraftbot.whitelist")) {
					((Player)sender).sendMessage(ChatColor.RED + "[BCraftBot] You do not have permission to do this!");
					return true;
				}
				if(args.length != 1) {
					((Player)sender).sendMessage(ChatColor.GOLD + "[BCraftBot] Usage: /bcwhitelist <on/off>");
					((Player)sender).sendMessage(ChatColor.RED + "[BCraftBot] There was an issue with your syntax.");
					return true;
				}
			}else {
				if(args.length != 1) {
					plugin.log(Level.INFO, "Usage: /bcwhitelist <on/off>");
					plugin.log(Level.INFO, "There was an issue with your syntax.");
					return true;
				}
			}
			
			String arg = args[0].toLowerCase();
			int status = 0;
			
			if(arg.equals("on") || arg.equals("true") || arg.equals("enable")) {
				status = 1;
				plugin.setBCWhitelist(true);
			}else if(arg.equals("off") || arg.equals("false") || arg.equals("disable")) {
				status = 2;
				plugin.setBCWhitelist(false);
			}
			
			if(sender instanceof Player) {
				if(status == 1) {
					((Player)sender).sendMessage(ChatColor.GREEN + "[BCraftBot] Whitelist enabled.");
				}else if(status == 2) {
					((Player)sender).sendMessage(ChatColor.GREEN + "[BCraftBot] Whitelist disabled.");
				}else {
					((Player)sender).sendMessage(ChatColor.GOLD + "[BCraftBot] Usage: /bcwhitelist <on/off>");
					((Player)sender).sendMessage(ChatColor.RED + "[BCraftBot] There was an issue with your syntax.");
				}
			}else {
				if(status == 1) {
					plugin.log(Level.INFO, "[BCraftBot] Whitelist enabled.");
				}else if(status == 2) {
					plugin.log(Level.INFO, "[BCraftBot] Whitelist disabled.");
				}else {
					plugin.log(Level.INFO, "[BCraftBot] Usage: /bcwhitelist <on/off>");
					plugin.log(Level.INFO, "[BCraftBot] There was an issue with your syntax.");
				}
			}
		}
		return true;
	}
	
}