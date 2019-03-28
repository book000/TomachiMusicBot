package com.jaoafa.TomachiMusicBot.Event;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.jaoafa.TomachiMusicBot.TomachiMusicBot;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.events.TrackStartEvent;

public class Event_TrackStart {
	public static IMessage LyricsMessage = null;
	@EventSubscriber
	public void onTrackStartEvent(TrackStartEvent event){
		Track track = event.getTrack();
		if(track == null){
			return;
		}

		File file = (File) track.getMetadata().get("file");
		try{
			Mp3File mp3file = new Mp3File(file);
			String title = "", artist = "";
			if(mp3file.hasId3v2Tag()){
				title = mp3file.getId3v2Tag().getTitle();
				artist = mp3file.getId3v2Tag().getArtist();
			}else if(mp3file.hasId3v1Tag()){
				title = mp3file.getId3v1Tag().getTitle();
				artist = mp3file.getId3v1Tag().getArtist();
			}

			if(LyricsMessage != null && !LyricsMessage.isDeleted()){
				LyricsMessage.delete();
			}
			if(!title.equals("") && !artist.equals("")){
				Map<String, String> data = TomachiMusicBot.getLyrics(title, artist);
				if(data != null && data.get("status").equalsIgnoreCase("true")){
					IChannel channel = TomachiMusicBot.getChannel();
					if(channel.isDeleted()){
						TomachiMusicBot.setChannel(event.getClient()
								.getChannelByID(512242412635029514L));
						channel = TomachiMusicBot.getChannel();
					}
					String lyrics = data.get("lyrics");
					String source = data.get("source");
					if(data.containsKey("realartist")){
						LyricsMessage = channel.sendMessage("`" +  title + "` - `" + data.get("realartist") + "` ```" + lyrics + "```(`" + source + "`)");
					}else{
						LyricsMessage = channel.sendMessage("`" +  title + "` - `" + artist + "` ```" + lyrics + "```(`" + source + "`)");
					}
				}
			}

			event.getClient().changePresence(StatusType.ONLINE, ActivityType.LISTENING, title + " - " + artist);
		}catch(InvalidDataException | UnsupportedTagException | IOException | IllegalArgumentException e){
			e.printStackTrace();
			return;
		}

	}
}
