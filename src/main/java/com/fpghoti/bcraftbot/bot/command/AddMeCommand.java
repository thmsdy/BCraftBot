package com.fpghoti.bcraftbot.bot.command;

import java.util.logging.Level;

import com.fpghoti.bcraftbot.bot.Command;
import com.fpghoti.bcraftbot.bot.ServerBot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AddMeCommand implements Command{

	private ServerBot bot;

	public AddMeCommand(ServerBot bot) {
		this.bot = bot;
	}

	private final String HELP = "USAGE: !addme <username>";

	@Override
	public boolean called(String[] args, MessageReceivedEvent event) {
		return true;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent event) {
		if(args.length == 1) {
			String name = args[0].toLowerCase();
			if(name.length() > 16) {
				return;
			}
			bot.log(Level.INFO, event.getAuthor().getName() + " issued a Discord Bot command: !addme " + name);

			if(bot.getPlugin().mainRecordKeeper()) {

				if(bot.getSQL().itemExists("DiscordID", event.getAuthor().getId(), bot.getTableName())) {
					bot.getSQL().set("MinecraftName", name, "DiscordID", "=", event.getAuthor().getId(), bot.getTableName());
				}else {
					bot.getSQL().update("INSERT INTO " + bot.getTableName() + " (DiscordID,MinecraftName) VALUES (\'" + event.getAuthor().getId() + "\',\'" + name + "\');");
				}

			}

			if(bot.getPlugin().outputToDiscord()) {
				event.getChannel().sendMessage("Updating Minecraft user database with username **" + args[0] + "**.").queue();
			}
		}

	}

	@Override
	public String help() {

		return HELP;
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent event) {
		return;
	}

}
