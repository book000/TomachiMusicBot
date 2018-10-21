package com.jaoafa.TomachiMusicBot.Command;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class MainEvent {
	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		System.out.println("Ready: " + event.getClient().getOurUser().getName());
	}
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		System.out.println("Msg: " + event.getAuthor().getName() + " " + event.getMessage().getContent());

		IDiscordClient client = event.getClient();
		IGuild guild = event.getGuild();
		IChannel channel = event.getChannel();
		IUser author = event.getAuthor();
		IMessage message = event.getMessage();
		String text = event.getMessage().getContent();

		if(!text.startsWith("*")){
			return;
		}

		String[] args;
		if(text.contains(" ")){
			args = text.split(" ");
		}else{
			args = new String[]{text};
		}
		if(args[0].equalsIgnoreCase("*search")){
			Cmd_Search.onCommand(client, guild, channel, author, message, args);
		}else if(args[0].equalsIgnoreCase("*summon")){
			Cmd_Summon.onCommand(client, guild, channel, author, message, args);
		}else if(args[0].equalsIgnoreCase("*disconnect")){
			Cmd_Disconnect.onCommand(client, guild, channel, author, message, args);
		}else if(args[0].equalsIgnoreCase("*clear")){
			Cmd_Clear.onCommand(client, guild, channel, author, message, args);
		}else if(args[0].equalsIgnoreCase("*queue")){
			Cmd_Queue.onCommand(client, guild, channel, author, message, args);
		}else if(args[0].equalsIgnoreCase("*info")){
			Cmd_Info.onCommand(client, guild, channel, author, message, args);
		}else if(args[0].equalsIgnoreCase("*skip")){
			Cmd_Skip.onCommand(client, guild, channel, author, message, args);
		}else if(args[0].equalsIgnoreCase("*np")){
			Cmd_NowPlaying.onCommand(client, guild, channel, author, message, args);
		}else if(args[0].equalsIgnoreCase("*volume")){
			Cmd_Volume.onCommand(client, guild, channel, author, message, args);
		}
	}
	/*
	@EventSubscriber
	public void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent event){
		if(event.getUser().getLongID() == event.getClient().getOurUser().getLongID()){
			return;
		}
		System.out.println("VoiceJoin: " + event.getUser().getName() + " " + event.getVoiceChannel().getName());
		IVoiceChannel voice = event.getVoiceChannel();
		if(voice == null) return;
		voice.join();

		AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(event.getGuild());
		audioP.clear();

		speakinVC(audioP, null, event.getUser().getName() + " joined!");
	}
	*/

	@EventSubscriber
	public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent event){
		if(event.getUser().getLongID() == event.getClient().getOurUser().getLongID()){
			return;
		}
		IVoiceChannel vc = event.getClient().getOurUser().getVoiceStateForGuild(event.getGuild()).getChannel();

		if(vc == null){
			return;
		}
		if(vc.getLongID() != event.getVoiceChannel().getLongID()){
			return;
		}

		if(vc.getConnectedUsers().size() == 1){ // 自分含め
			vc.leave();
		}
	}


	@EventSubscriber
	public void onUserVoiceChannelMoveEvent(UserVoiceChannelMoveEvent event){
		if(event.getUser().getLongID() == event.getClient().getOurUser().getLongID()){
			return;
		}
		IVoiceChannel vc = event.getClient().getOurUser().getVoiceStateForGuild(event.getGuild()).getChannel();

		if(vc == null){
			return;
		}
		if(vc.getLongID() != event.getOldChannel().getLongID()){
			return;
		}

		if(vc.getConnectedUsers().size() == 1){ // 自分含め
			vc.leave();
		}
	}
/*
	void speakinVC(AudioPlayer audioP, IChannel channel, String message){
		String[] List;
		if(message.contains("\n")){
			List = message.split("\n");
		}else{
			List = new String[]{message};
		}
		for(String msg : List){
			try {
				AudioInputStream stream = EmotionalSpeaker.HIKARI.ready().happy().getResponse(msg).audioInputStream();

				audioP.queue(stream);
			}catch(IllegalArgumentException e){
				if(channel == null) continue;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				channel.sendMessage("[" + sdf.format(new Date()) + "] " + e.getMessage());
			}
		}
	}

	@EventSubscriber
	public void onFinish(TrackFinishEvent event){
		if(event.getNewTrack().isPresent()){
			return;
		}
		IVoiceChannel botVoiceChannel = event.getClient().getOurUser().getVoiceStateForGuild(event.getPlayer().getGuild()).getChannel();

		if(botVoiceChannel == null)
			return;

		botVoiceChannel.leave();
	}
	 */
}