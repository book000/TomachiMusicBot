package com.jaoafa.TomachiMusicBot.Lib;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JoySound {
	public static String search(final String _title, final String _artist) throws ClassNotFoundException, SQLException{
		String title = _title;
		String artist = _artist;

		title = title.replaceAll("\\(.*?\\)", "");
		title = title.replaceAll("\\[.*?\\]", "");
		title = title.replaceAll("'", "_");
		title = title.replaceAll("\"", "_");
		title = title.trim();

		if(artist != null){
			artist = artist.replaceAll("\\(.*?\\)", "");
			artist = artist.replaceAll("\\[.*?\\]", "");
			artist = artist.replaceAll("'", "_");
			artist = artist.replaceAll("\"", "_");
			artist = artist.trim();
		}

		File sqliteFile = new File("JoySound.db");
		if(!sqliteFile.exists()){
			return null;
		}

		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:JoySound.db");
		PreparedStatement statement;
		if(artist != null){
			statement = conn.prepareStatement("SELECT * FROM data WHERE title LIKE ? AND artist LIKE ?", ResultSet.TYPE_SCROLL_SENSITIVE);
			statement.setString(2, "%" + artist + "%");
		}else{
			statement = conn.prepareStatement("SELECT * FROM data WHERE title LIKE ?", ResultSet.TYPE_SCROLL_SENSITIVE);
		}
		statement.setString(1, "%" + title + "%");

		ResultSet res = statement.executeQuery();

		res.last();
		int number_of_row = res.getRow();
		res.beforeFirst();

		if(res.next()){
			// ROWあり
			if(artist == null && number_of_row >= 2){
				// 2つ以上ある
				conn.close();
				return null;
			}
			String lyrics = res.getString("lyrics");
			conn.close();
			return lyrics;
		}else{
			// ROWなし
			conn.close();
			return null;
		}
	}
}
