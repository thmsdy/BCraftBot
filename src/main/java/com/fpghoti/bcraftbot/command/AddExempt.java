package com.fpghoti.bcraftbot.command;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.fpghoti.bcraftbot.Main;

public class AddExempt implements CommandExecutor{

	private Main plugin;

	public AddExempt(Main plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(label.equalsIgnoreCase("addexempt")){
			if(sender instanceof Player) {
				if(!((Player)sender).hasPermission("bcraftbot.addexempt")) {
					((Player)sender).sendMessage(ChatColor.RED + "[BCraftBot] You do not have permission to do this!");
					return true;
				}
				if(args.length != 1) {
					((Player)sender).sendMessage(ChatColor.GOLD + "[BCraftBot] Usage: /addexempt <username>");
					((Player)sender).sendMessage(ChatColor.RED + "[BCraftBot] There was an issue with your syntax.");
					return true;
				}
			}else {
				if(args.length != 1) {
					plugin.log(Level.INFO, "Usage: addexempt <username>");
					plugin.log(Level.INFO, "There was an issue with your syntax.");
					return true;
				}
			}
			String name = args[0];
			if(name.length() > 16) {
				return true;
			}
			plugin.addExempt(name);
			if(sender instanceof Player) {
				((Player)sender).sendMessage(ChatColor.GREEN + "[BCraftBot] Player added to the exempt list!");
			}else {
				plugin.log(Level.INFO, "Player added to the exempt list!");
			}
		}
		return true;
	}
	
}