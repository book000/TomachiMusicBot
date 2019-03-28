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

public class Cmd_Volume {
	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		TomachiMusicBot.setChannel(message);
		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Volume");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);

		if(args.length == 1){
			// 表示のみ
			float vol = audioP.getVolume();
			embed.appendField("現在の音量", vol + "%", false);
		}else if(args.length == 2){
			// 変更
			float Changevol = audioP.getVolume();
			try{
				float newVol = Float.valueOf(args[0]);
				embed.appendField("変更前の音量", "" + Changevol, false);
				audioP.setVolume(newVol);
				float New_Vol = audioP.getVolume();
				embed.appendField("変更前の音量", "" + New_Vol, false);
			}catch(NumberFormatException e){
				embed.appendField("Error", "音量には数値(小数含む)を指定してください。", false);
			}
		}else{
			return;
		}
		embed.withColor(Color.ORANGE);

		channel.sendMessage("", embed.build());
	}
}
