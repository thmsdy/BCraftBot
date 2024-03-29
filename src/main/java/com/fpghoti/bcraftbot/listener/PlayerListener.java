package com.fpghoti.bcraftbot.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.fpghoti.bcraftbot.Main;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class PlayerListener implements Listener {

	private Main plugin;

	public PlayerListener(Main plugin){
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event){
		Player player = event.getPlayer();

		if(player.hasPermission("bcraftbot.bypasscheck")) {
			return;
		}

		if(!plugin.isMember(player) && !plugin.isExempt(player.getName())) {
			if(!player.isWhitelisted() && plugin.getBCWhitelist()) {
				event.disallow(Result.KICK_OTHER, plugin.getKickMessage());
			}
			return;
		}

		if(plugin.checkRole()  && !plugin.isExempt(player.getName())) {
			boolean allow = false;

			for(Guild guild : plugin.getBot().getJDA().getGuilds()) {

				for(String rolename : plugin.getRequiredRole().split(",")) {
					Role role = null;
					for(Role r : guild.getRoles()) {
						if(r.getName().equalsIgnoreCase(rolename)) {
							role = r;
						}
					}

					User user = plugin.getDiscordUser(player);
					Member mem = guild.getMember(user);

					if(!plugin.getBCWhitelist()) {
						allow = true;
					}
					if(player.isWhitelisted()) {
						allow = true;
					}
					if(user != null && user.getMutualGuilds().contains(guild) && role != null) {
						if(mem.getRoles().contains(role)) {
							allow = true;
						}
					}
				}
			}

			if(!allow) {
				event.disallow(Result.KICK_OTHER, plugin.getKickMessage());
				return;
			}

		}

		if(plugin.assignRole() && !plugin.isExempt(player.getName())) {
			for(Guild guild : plugin.getBot().getJDA().getGuilds()) {

				Role role = null;
				for(Role r : guild.getRoles()) {
					if(r.getName().equalsIgnoreCase(plugin.getAssignRoleName())) {
						role = r;
					}
				}

				User user = plugin.getDiscordUser(player);
				Member mem = guild.getMember(user);

				if(user != null && user.getMutualGuilds().contains(guild) && role != null) {
					guild.addRoleToMember(mem.getUser().getId(), role).queue();
				}

			}
		}
	}


}
