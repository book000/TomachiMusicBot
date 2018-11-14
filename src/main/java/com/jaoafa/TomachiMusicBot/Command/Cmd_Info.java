package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class Cmd_Info {
	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		TomachiMusicBot.setChannel(message);
		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Info");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		List<File> songDir = Cmd_Search.getIterateListFiles(new File("music"));

		embed.appendField("累計曲数", songDir.size() + "曲", false);

		int okSong = 0;
		long size = 0;
		long sizeOK = 0;
		for(File file : songDir){
			if(file.isFile()) size = file.length();
			try {
				new Mp3File(file);
				if(file.isFile()) sizeOK = file.length();

				okSong++;
			} catch (UnsupportedTagException | InvalidDataException | IOException | IllegalArgumentException e) {
				continue;
			}
		}
		embed.appendField("そのうち、視聴可能曲数", okSong + "曲", false);

		embed.appendField("音源ファイル分使用容量数", getSizeStr(size), false);
		embed.appendField("そのうち、視聴可能音源ファイル分使用容量数", getSizeStr(sizeOK), false);


		embed.withColor(Color.GREEN);

		channel.sendMessage("", embed.build());
	}
	static String getSizeStr(long size) {
		double dbytes = (double) size;
	    DecimalFormat df = new DecimalFormat("#.##");

	    if (dbytes < 1024) {
	        return df.format(size) + " bytes";
	    } else if (dbytes < 1024 * 1024) {
	        return df.format(dbytes / 1024) + " KB";
	    } else if (dbytes < 1024 * 1024 * 1024) {
	        return df.format(dbytes / 1024 / 1024) + " MB";
	    } else if (dbytes < 1024 * 1024 * 1024 * 1024L) {
	        return df.format(dbytes / 1024 / 1024 / 1024) + " GB";
	    } else {
	        return size + " bytes";
	    }
	}
}
