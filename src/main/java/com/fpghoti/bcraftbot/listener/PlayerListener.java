package com.fpghoti.bcraftbot.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.fpghoti.bcraftbot.Main;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class PlayerListener implements Listener {

	private Main plugin;

	public PlayerListener(Main plugin){
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event){
		Player player = event.getPlayer();

		if(!plugin.isMember(player) && !plugin.isExempt(player.getName())) {
			event.disallow(Result.KICK_OTHER, plugin.getKickMessage());
			return;
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
					guild.getController().addRolesToMember(mem, role).queue();
				}

			}
		}
	}

	
}
