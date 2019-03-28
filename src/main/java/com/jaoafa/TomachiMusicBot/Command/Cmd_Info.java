package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;
import com.jaoafa.TomachiMusicBot.Lib.MusicFilesDB;

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

		MusicFilesDB mfdb = new MusicFilesDB();
		try {
			mfdb.refreshDB();
		} catch (SQLException | ClassNotFoundException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			channel.sendMessage("音源ファイルデータベースがリフレッシュできませんでした。```" + sw.toString() + "```");
			return;
		}
		embed.appendDesc("音源ファイルデータベースをリフレッシュしました。");

		embed.appendField("累計ファイル数", mfdb.getCount() + "ファイル", false);

		embed.appendField("そのうち、視聴可能曲数", mfdb.getOkSong() + "曲", false);
		embed.appendField("そのうち、Mp3Tagの設定により視聴不可能曲数", mfdb.getNGTagSong() + "曲", false);
		embed.appendField("累計アーティスト数", "" + mfdb.getArtistCount(), false);
		embed.appendField("累計アルバム数", "" + mfdb.getAlbumCount(), false);

		embed.appendField("音源ファイル分使用容量数", getSizeStr(mfdb.getALLSize()), false);
		embed.appendField("そのうち、視聴可能音源ファイル分使用容量数", getSizeStr(mfdb.getSizeOK()), false);

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
