package com.jaoafa.TomachiMusicBot.Event;

import java.io.File;
import java.io.IOException;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

public class Event_TrackFinish {
	@EventSubscriber
	public void onTrackFinishEvent(TrackFinishEvent event){
		if(Event_TrackStart.LyricsMessage != null && !Event_TrackStart.LyricsMessage.isDeleted()){
			Event_TrackStart.LyricsMessage.delete();
		}
		if(event.getNewTrack().isPresent()){
			// 次がある
			Track track = event.getNewTrack().get();
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

				event.getClient().changePresence(StatusType.ONLINE, ActivityType.LISTENING, "Loading... " + title + " - " + artist);
			}catch(InvalidDataException | UnsupportedTagException | IOException | IllegalArgumentException e){
				e.printStackTrace();
				return;
			}
		}else{
			event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "Waiting... | *search <Text>");
		}
	}
}
