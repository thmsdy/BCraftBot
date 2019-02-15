package com.fpghoti.bcraftbot.bot;

import java.util.logging.Level;

import com.fpghoti.bcraftbot.bot.ServerBot;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class BotListener extends ListenerAdapter{

	ServerBot bot;
	
	public BotListener(ServerBot bot) {
		this.bot = bot;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event){
		if(event.getChannelType() == ChannelType.TEXT && !event.getAuthor().isBot() && event.getMessage().getContentDisplay().startsWith("!") && event.getMessage().getAuthor().getId() != event.getJDA().getSelfUser().getId()){
			bot.handleCommand(bot.getCommandParser().parse(event.getMessage().getContentRaw().toLowerCase(), event));
		}
	}

	@Override
	public void onReady(ReadyEvent event){
		bot.log(Level.INFO, "Bot is ready!");
	}

}
