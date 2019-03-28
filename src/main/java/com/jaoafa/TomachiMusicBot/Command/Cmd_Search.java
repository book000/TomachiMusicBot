package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;
import com.jaoafa.TomachiMusicBot.Lib.MusicFilesDB;
import com.mpatric.mp3agic.Mp3File;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.audio.AudioPlayer;

public class Cmd_Search {
	public static Map<Long, List<Mp3File>> searchData = new HashMap<>();

	public static void onCommand(IDiscordClient client, IGuild guild, IChannel channel, IUser author, IMessage message, String[] args){
		TomachiMusicBot.check(message);
		TomachiMusicBot.setChannel(message);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - Search");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		if(args.length == 1){
			// 1つしか引数がない→ヘルプ
			embed.appendField("Help", "`*search <Title,Artist,Album|page|select> [Value]`\n"
					+ "`title:<Title>`: タイトルのみで検索します。\n"
					+ "`artist:<Artist>`: アーティスト名のみで検索します。\n"
					+ "`album:<Album>`: アルバム名のみで検索します。", false);
			embed.withColor(Color.GREEN);

			channel.sendMessage("", embed.build());
			return;
		}

		// VCにいるか
		Iterator<IVoiceChannel> itr = client.getConnectedVoiceChannels().iterator();
		boolean JoinedVCBool = false;
		while(itr.hasNext()){
			IVoiceChannel vc = itr.next();
			if(vc.getGuild().getLongID() != guild.getLongID()){
				continue;
			}
			JoinedVCBool = true;
		}
		if(!JoinedVCBool){ // VCにいなかったら
			embed.appendField("Error", "ボイスチャンネルに入っていません。", false);
			embed.withColor(Color.RED);

			channel.sendMessage("", embed.build());
			return;
		}

		if(!new File("music").exists() || !new File("music").isDirectory()){
			embed.appendField("Error", "musicディレクトリが見つかりません。", false);
			embed.withColor(Color.RED);

			channel.sendMessage("", embed.build());
			return;
		}

		if(args[1].equalsIgnoreCase("page")){
			// ページモード
			if(!searchData.containsKey(author.getLongID())){
				embed.appendField("Error", "ページ情報が取得できません。`*search`コマンドを使用して検索を行ってください。", false);
				embed.withColor(Color.RED);

				channel.sendMessage("", embed.build());
				return;
			}
			if(args.length != 3){
				embed.appendField("Error", "引数が不適切です。例: `*search page 2`", false);
				embed.withColor(Color.RED);

				channel.sendMessage("", embed.build());
				return;
			}
			try{
				int page = Integer.parseInt(args[2]);
				if(page <= 0){
					embed.appendField("Error", "引数が不適切です。ページは1以上を指定してください。", false);
					embed.withColor(Color.RED);

					channel.sendMessage("", embed.build());
					return;
				}
				generationPageStr(client, channel, author.getLongID(), page);
				return;
			}catch(NumberFormatException e){
				embed.appendField("Error", "引数が不適切です。ページには数値を指定してください。", false);
				embed.withColor(Color.RED);

				channel.sendMessage("", embed.build());
				return;
			}
		}else if(args[1].equalsIgnoreCase("select")){
			// 選択モード
			if(!searchData.containsKey(author.getLongID())){
				embed.appendField("Error", "ページ情報が取得できません。`*search`コマンドを使用して検索を行ってください。", false);
				embed.withColor(Color.RED);

				channel.sendMessage("", embed.build());
				return;
			}
			try{
				int track = Integer.parseInt(args[2]);
				if(track <= 0){
					embed.appendField("Error", "引数が不適切です。トラック番号は1以上を指定してください。", false);
					embed.withColor(Color.RED);

					channel.sendMessage("", embed.build());
					return;
				}

				List<Mp3File> matchFiles = searchData.get(author.getLongID());

				if((track -1) >= matchFiles.size()){
					embed.appendField("Error", "指定されたトラック番号の曲は見つかりません。", false);
					embed.withColor(Color.RED);

					channel.sendMessage("", embed.build());
					return;
				}

				Mp3File mp3file = matchFiles.get(track-1);
				File file = new File(mp3file.getFilename());
				AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);

				embed.withColor(Color.ORANGE);
				embed.withTitle("TomachiMusicBot - Search - Track Add");

				if(mp3file.hasId3v2Tag()){
					embed.appendField("Title", "`" + mp3file.getId3v2Tag().getTitle() + "`", false);
					embed.appendField("Album", "`" + mp3file.getId3v2Tag().getAlbum() + "`", false);
					embed.appendField("Artist", "`" + mp3file.getId3v2Tag().getArtist() + "`", false);
				}else if(mp3file.hasId3v1Tag()){
					embed.appendField("Title", "`" + mp3file.getId3v1Tag().getTitle() + "`", false);
					embed.appendField("Album", "`" + mp3file.getId3v1Tag().getAlbum() + "`", false);
					embed.appendField("Artist", "`" + mp3file.getId3v1Tag().getArtist() + "`", false);
				}
				long sec = mp3file.getLengthInSeconds();
				String TimeStr = SecToHIS(sec);

				embed.appendField("Time", "`" + TimeStr + "`", false);

				int size = audioP.getPlaylistSize();
				if(size == 0){
					embed.appendField("PlayTiming", "いますぐ！", false);
				}else{
					embed.appendField("PlayTiming", size + "曲後", false);
				}
				channel.sendMessage("", embed.build());

				try {
					audioP.queue(file);
				} catch (IOException | UnsupportedAudioFileException e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					channel.sendMessage("[" + sdf.format(new Date()) + "] " + "ERROR: トラックの再生に失敗しました。 ```" + sw.toString() + "```");
				}
				return;
			}catch(NumberFormatException e){
				embed.appendField("Error", "引数が不適切です。トラック番号には数値を指定してください。", false);
				embed.withColor(Color.RED);

				channel.sendMessage("", embed.build());
				return;
			}


		}

		String[] slicedArgs = Arrays.copyOfRange(args, 1, args.length);

		String searchTitle = null;
		String searchArtist = null;
		String searchAlbum = null;
		for(String arg : slicedArgs){
			if(arg == null) continue;
			if(arg.startsWith("title:")){
				searchTitle = arg.replaceFirst(
						Matcher.quoteReplacement("title:")
						, "");
			}
			if(arg.startsWith("artist:")){
				searchArtist = arg.replaceFirst(
						Matcher.quoteReplacement("artist:")
						, "");
			}
			if(arg.startsWith("album:")){
				searchAlbum = arg.replaceFirst(
						Matcher.quoteReplacement("album:")
						, "");
			}
		}

		String searchText = implode(slicedArgs, " ");

		List<Mp3File> matchFiles;
		try{
			if(searchTitle != null){
				matchFiles = MusicFilesDB.OnlySearch("title", searchTitle);
			}else if(searchArtist != null){
				matchFiles = MusicFilesDB.OnlySearch("artist", searchTitle);
			}else if(searchAlbum != null){
				matchFiles = MusicFilesDB.OnlySearch("album", searchTitle);
			}else{
				matchFiles = MusicFilesDB.Search(searchText);
			}
		}catch(SQLException | ClassNotFoundException e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			channel.sendMessage("[" + sdf.format(new Date()) + "] " + "ERROR: 音源ファイルデータベースの読み込みに失敗しました。 ```" + sw.toString() + "```");
			return;
		}

		if(matchFiles.isEmpty()){
			embed.appendField("Error", "ファイルがマッチしませんでした。", false);
			embed.withColor(Color.RED);

			channel.sendMessage("", embed.build());
			return;
		}else if(matchFiles.size() == 1){
			// 1つ→流す
			AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);
			Mp3File mp3file = matchFiles.get(0);
			File file = new File(mp3file.getFilename());

			embed.withColor(Color.ORANGE);
			embed.withTitle("TomachiMusicBot - Search - Track Add");

			int size = audioP.getPlaylistSize();
			if(size == 0){
				embed.appendField("PlayTiming", "いますぐ！", false);
			}else{
				embed.appendField("PlayTiming", size + "曲後", false);
			}
			channel.sendMessage("", embed.build());

			try {
				audioP.queue(file);
			} catch (IOException | UnsupportedAudioFileException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				channel.sendMessage("[" + sdf.format(new Date()) + "] " + "ERROR: トラックの再生に失敗しました。 ```" + sw.toString() + "```");
			}
			return;
		}
		embed.appendField("Result", matchFiles.size() + "曲が見つかりました。", false);
		searchData.put(author.getLongID(), matchFiles);
		channel.sendMessage("", embed.build());
		generationPageStr(client, channel, author.getLongID(), 1);
		/*		channel.sendMessage("[" + sdf.format(new Date()) + "] "
				+ "1. `" + matchFiles.get(0).getName() + "`"
				+ "2. `" + matchFiles.get(1).getName() + "`"
				+ "3. `" + matchFiles.get(2).getName() + "`");*/
	}

	static void generationPageStr(IDiscordClient client, IChannel channel, Long id, int page){
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// 1ページ10曲
		page--; // 1ページからだけど、計算上0からやりたい

		EmbedBuilder embed = new EmbedBuilder();
		embed.withTitle("TomachiMusicBot - PageList");
		embed.withAuthorIcon(client.getApplicationIconURL());
		embed.withAuthorName("TomachiMusicBot");
		embed.withAuthorUrl("https://github.com/book000/TomachiMusicBot");

		if(!searchData.containsKey(id)){
			return;
		}
		List<Mp3File> matchFiles = searchData.get(id);
		int i = page * 10 + 1;
		if(page * 10 > matchFiles.size()){
			embed.appendField("Error", "そのページは見つかりません。", false);
			channel.sendMessage("", embed.build());
			return;
		}
		int max = page * 10 + 10;
		if(page * 10 + 10 > matchFiles.size()){
			max = matchFiles.size();
		}
		for(Mp3File mp3file : matchFiles.subList(page * 10, max)){
			String title = "";
			String artist = "";
			String album = "";
			if(mp3file.hasId3v2Tag()){
				title = mp3file.getId3v2Tag().getTitle();
				artist = mp3file.getId3v2Tag().getArtist();
				album = mp3file.getId3v2Tag().getAlbum();
			}else if(mp3file.hasId3v1Tag()){
				title = mp3file.getId3v1Tag().getTitle();
				artist = mp3file.getId3v1Tag().getArtist();
				album = mp3file.getId3v1Tag().getAlbum();
			}
			long sec = mp3file.getLengthInSeconds();
			String TimeStr = SecToHIS(sec);

			embed.appendField("Track" + i, "`" + title + "` - `" + album + "`\n"
					+ "Author: " + artist + "\n"
					+ "Time: " + TimeStr, false);

			i++;
		}
		embed.appendField("Tip", "`*search page <Page>`で指定したページを閲覧できます。`*search select <TrackNum>`でその曲を選択(再生)できます。", false);
		embed.withColor(Color.ORANGE);
		channel.sendMessage("", embed.build());
	}

	public static String SecToHIS(long _sec){
		StringBuilder builder = new StringBuilder();
		int hour = (int) (_sec / 3600L);
		int hour_remain = (int) (_sec % 3600L);
		if(hour != 0){
			builder.append(hour + "時間");
		}
		int minute = (int) (hour_remain / 60L);
		if(minute != 0){
			builder.append(minute + "分");
		}
		int sec = (int) (hour_remain % 60L);
		if(sec != 0){
			builder.append(sec + "秒");
		}
		return builder.toString();
	}
	static <T> String implode(T[] slicedArgs, String glue) {
		StringBuilder sb = new StringBuilder();
		for (T e : slicedArgs) {
			sb.append(glue).append(e);
		}
		return sb.substring(glue.length());
	}
}
