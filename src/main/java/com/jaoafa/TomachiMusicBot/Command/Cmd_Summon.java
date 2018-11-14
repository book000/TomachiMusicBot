package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IVoiceState;
import sx.blah.discord.util.EmbedBuilder;

public class Cmd_Summon {
	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		TomachiMusicBot.setChannel(message);
		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Summon");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		IVoiceState vs = author.getVoiceStateForGuild(guild);
		IVoiceChannel vc = vs.getChannel();
		if(vc == null){
			embed.appendField("Error", "ボイスチャンネルが見つかりません。ボイスチャンネルに入ってから再度実行してください。", false);
			embed.withColor(Color.RED);

			channel.sendMessage("", embed.build());
			return;
		}

		vc.join();
	}
}
