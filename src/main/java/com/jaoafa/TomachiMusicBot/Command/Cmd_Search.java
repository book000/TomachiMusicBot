package com.jaoafa.TomachiMusicBot.Command;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.audio.AudioPlayer;

public class Cmd_Search {
	public static Map<Long, List<File>> searchData = new HashMap<>();

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

				List<File> matchFiles = searchData.get(author.getLongID());

				if((track -1) >= matchFiles.size()){
					embed.appendField("Error", "指定されたトラック番号の曲は見つかりません。", false);
					embed.withColor(Color.RED);

					channel.sendMessage("", embed.build());
					return;
				}

				File file = matchFiles.get(track-1);
				AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);

				embed.withColor(Color.ORANGE);
				embed.withTitle("TomachiMusicBot - Search - Track Add");

				try{
					Mp3File mp3file = new Mp3File(file);
					String title = "", artist = "", album = "";
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

					embed.appendField("Title", "`" + title + "`", false);
					embed.appendField("Album", "`" + album + "`", false);
					embed.appendField("Artist", "`" + artist + "`", false);
					embed.appendField("Time", "`" + TimeStr + "`", false);
				}catch(InvalidDataException | UnsupportedTagException | IOException e){
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					embed.appendField("Error", "トラックのパースに失敗しました。", false);
					embed.appendField("StackTrace", "```" + sw.toString() + "```", false);
					embed.withColor(Color.RED);

					channel.sendMessage("", embed.build());
					return;
				}
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

		List<File> songDir = getIterateListFiles(new File("music"));

		if(songDir == null || songDir.size() == 0){
			embed.appendField("Error", "musicディレクトリにファイルが見つかりません。", false);
			embed.withColor(Color.RED);

			channel.sendMessage("", embed.build());
			return;
		}

		List<File> matchFiles = new ArrayList<>();
		for(File file : songDir){
			try {
				Mp3File mp3file = new Mp3File(file);

				if(mp3file.hasId3v2Tag()){
					ID3v2 id3v2Tag = mp3file.getId3v2Tag();

					String title = id3v2Tag.getTitle();
					String artist = id3v2Tag.getArtist();
					String album = id3v2Tag.getAlbum();

					// ONLY Search
					if(searchTitle != null){
						if(title.contains(searchTitle)){
							matchFiles.add(file);
						}
						continue; // それしか検索しない
					}
					if(searchArtist != null){
						if(artist.contains(searchArtist)){
							matchFiles.add(file);
						}
						continue; // それしか検索しない
					}
					if(searchAlbum != null){
						if(album.contains(searchAlbum)){
							matchFiles.add(file);
						}
						continue; // それしか検索しない
					}

					if(title.contains(searchText)){
						matchFiles.add(file);
						continue;
					}
					if(artist.contains(searchText)){
						matchFiles.add(file);
						continue;
					}
					if(album.contains(searchText)){
						matchFiles.add(file);
						continue;
					}
				}else if(mp3file.hasId3v1Tag()){
					ID3v1 id3v1Tag = mp3file.getId3v1Tag();

					String title = id3v1Tag.getTitle();
					String artist = id3v1Tag.getArtist();
					String album = id3v1Tag.getAlbum();

					// ONLY Search
					if(searchTitle != null){
						if(title.contains(searchTitle)){
							matchFiles.add(file);
						}
						continue; // それしか検索しない
					}
					if(searchArtist != null){
						if(artist.contains(searchArtist)){
							matchFiles.add(file);
						}
						continue; // それしか検索しない
					}
					if(searchAlbum != null){
						if(album.contains(searchAlbum)){
							matchFiles.add(file);
						}
						continue; // それしか検索しない
					}

					if(title.contains(searchText)){
						matchFiles.add(file);
						continue;
					}
					if(artist.contains(searchText)){
						matchFiles.add(file);
						continue;
					}
					if(album.contains(searchText)){
						matchFiles.add(file);
						continue;
					}
				}else{
					if(file.getName().contains(searchText)){
						matchFiles.add(file);
						continue;
					}
				}
			} catch (UnsupportedTagException | InvalidDataException | IOException | IllegalArgumentException e) {
				continue;
			}
		}

		if(matchFiles.isEmpty()){
			embed.appendField("Error", "ファイルがマッチしませんでした。", false);
			embed.withColor(Color.RED);

			channel.sendMessage("", embed.build());
			return;
		}else if(matchFiles.size() == 1){
			// 1つ→流す
			AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(guild);
			File file = matchFiles.get(0);

			embed.withColor(Color.ORANGE);
			embed.withTitle("TomachiMusicBot - Search - Track Add");

			try{
				Mp3File mp3file = new Mp3File(file);
				String title = "", artist = "", album = "";
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

				embed.appendField("Title", "`" + title + "`", false);
				embed.appendField("Album", "`" + album + "`", false);
				embed.appendField("Artist", "`" + artist + "`", false);
				embed.appendField("Time", "`" + TimeStr + "`", false);
			}catch(InvalidDataException | UnsupportedTagException | IOException | IllegalArgumentException e){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				embed.appendField("Error", "トラックのパースに失敗しました。", false);
				embed.appendField("StackTrace", "```" + sw.toString() + "```", false);
				embed.withColor(Color.RED);

				channel.sendMessage("", embed.build());
				return;
			}
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
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
		List<File> matchFiles = searchData.get(id);
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
		for(File file : matchFiles.subList(page * 10, max)){
			try{
				Mp3File mp3file = new Mp3File(file);
				String title = "", artist = "", album = "";
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
			}catch(InvalidDataException | UnsupportedTagException | IOException e){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				channel.sendMessage("[" + sdf.format(new Date()) + "] " + "ERROR: トラックのパースに失敗しました。。 ```" + sw.toString() + "```");
			}
			i++;
		}
		embed.appendField("Tip", "`*search page <Page>`で指定したページを閲覧できます。`*search select <TrackNum>`でその曲を選択(再生)できます。", false);
		embed.withColor(Color.ORANGE);
		channel.sendMessage("", embed.build());
	}

	static List<File> getIterateListFiles(File dir){
		List<File> returnFiles = new ArrayList<>();
		if(!dir.isDirectory()){
			return returnFiles;
		}
		File[] files = dir.listFiles();
		if(files == null){
			return returnFiles;
		}
		for(File file : files){
			if(!file.exists()){
				continue;
			}else if(file.isDirectory()){
				List<File> dir_files = getIterateListFiles(file);
				if(dir_files.size() != 0) returnFiles.addAll(dir_files);
			}else if(file.isFile()){
				returnFiles.add(file);
			}
		}
		return returnFiles;
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
