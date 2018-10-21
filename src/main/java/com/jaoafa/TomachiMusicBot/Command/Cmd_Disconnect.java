package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;

public class Cmd_Disconnect {
	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Disconnect");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		IVoiceChannel vc = client.getOurUser().getVoiceStateForGuild(guild).getChannel();

		if(vc == null){
			embed.appendField("Error", "ボイスチャンネルに入っていません。", false);
			embed.withColor(Color.RED);

			channel.sendMessage("", embed.build());
			return;
		}

		vc.leave();
	}

}
