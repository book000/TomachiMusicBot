package com.jaoafa.TomachiMusicBot.Lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KasiTime {
	public static String search(final String _title, final String _artist) throws ClassNotFoundException, SQLException{
		String title = _title;
		String artist = _artist;

		title = title.replaceAll("\\(.*?\\)", "");
		title = title.replaceAll("\\[.*?\\]", "");
		title = title.replaceAll("'", "_");
		title = title.replaceAll("\"", "_");
		title = title.trim();

		artist = artist.replaceAll("\\(.*?\\)", "");
		artist = artist.replaceAll("\\[.*?\\]", "");
		artist = artist.replaceAll("'", "_");
		artist = artist.replaceAll("\"", "_");
		artist = artist.trim();

		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:kasiTime.db");
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM data WHERE title LIKE ? AND artist LIKE ?");

		statement.setString(1, "%" + title + "%");
		statement.setString(2, "%" + artist + "%");

		ResultSet res = statement.executeQuery();
		if(res.next()){
			// ROWあり
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
