package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.audio.AudioPlayer;

public class Cmd_Clear {
	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Clear");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);
		audioP.clear();

		embed.appendField("Successful", ":put_litter_in_its_place:", false);
		embed.withColor(Color.ORANGE);

		channel.sendMessage("", embed.build());
	}
}
