package com.jaoafa.TomachiMusicBot.Lib;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class MusicFilesDB {
	final static String TABLE_NAME = "files";
	final static String[] InstList = {
		"instrumental",
		"Instrumental",
		"カラオケ",
		"off vocal",
		"ドラマ"
	};

	static int artistCount = 0;
	static int albumCount = 0;
	static int Count = 0;
	static int okSong = 0;
	static int ngTagSong = 0;
	static long size = 0;
	static long sizeOK = 0;
	/**
	 * テーブルを作成します。既に存在する場合は削除して作り直します
	 * @throws SQLException SQLで問題が発生したときにスローします。
	 * @throws ClassNotFoundException
	 */
	private static void createTable() throws SQLException, ClassNotFoundException{
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:MusicFiles.db");
		Statement statement = conn.createStatement();
		if(existTABLE()){
			statement.executeUpdate("DROP TABLE `" + TABLE_NAME + "`;");
			statement = conn.createStatement();
		}

		statement.executeUpdate(
				"CREATE TABLE `" + TABLE_NAME + "` (" +
				"	`id`	INTEGER PRIMARY KEY AUTOINCREMENT," +
				"	`title`	TEXT," +
				"	`artist`	TEXT," +
				"	`album`	TEXT," +
				"	`path`	INTEGER UNIQUE" +
				");"
		);
		conn.close();
	}
	/**
	 * データベースをリフレッシュします。音源からデータを取り直し、最新のデータベースに更新します。
	 * @throws SQLException SQLで問題が発生したときにスローします。
	 * @throws ClassNotFoundException
	 */
	public void refreshDB() throws SQLException, ClassNotFoundException{
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:MusicFiles.db");
		createTable();

		artistCount = 0;
		albumCount = 0;
		okSong = 0;
		ngTagSong = 0;
		size = 0;
		sizeOK = 0;
		Count = 0;

		List<File> songDir = getIterateListFiles(new File("music"));
		Map<String, Integer> artists = new HashMap<>();
		Map<String, Integer> albums = new HashMap<>();
		for(File file : songDir){
			PreparedStatement pre_statement = conn.prepareStatement("INSERT INTO `" + TABLE_NAME + "` (title, artist, album, path) VALUES (?, ?, ?, ?)");
			if(file.isFile()) size += file.length();
			if(file.isFile()) Count++;
			try {
				Mp3File mp3file = new Mp3File(file);

				if(mp3file.hasId3v2Tag()){
					ID3v2 id3v2Tag = mp3file.getId3v2Tag();

					String title = id3v2Tag.getTitle();
					String artist = id3v2Tag.getArtist();
					if(artists.containsKey(artist)){
						artists.put(artist, artists.get(artist) + 1);
					}else{
						artists.put(artist, 1);
					}
					String album = id3v2Tag.getAlbum();
					if(albums.containsKey(album)){
						albums.put(album, albums.get(album) + 1);
					}else{
						albums.put(album, 1);
					}

					pre_statement.setString(1, title);
					pre_statement.setString(2, artist);
					pre_statement.setString(3, album);
					pre_statement.setString(4, file.getCanonicalPath());
					pre_statement.executeUpdate();
				}else if(mp3file.hasId3v1Tag()){
					ID3v1 id3v1Tag = mp3file.getId3v1Tag();

					String title = id3v1Tag.getTitle();
					String artist = id3v1Tag.getArtist();
					if(artists.containsKey(artist)){
						artists.put(artist, artists.get(artist) + 1);
					}else{
						artists.put(artist, 1);
					}
					String album = id3v1Tag.getAlbum();
					if(albums.containsKey(album)){
						albums.put(album, albums.get(album) + 1);
					}else{
						albums.put(album, 1);
					}

					pre_statement.setString(1, title);
					pre_statement.setString(2, artist);
					pre_statement.setString(3, album);
					pre_statement.setString(4, file.getCanonicalPath());
					pre_statement.executeUpdate();
				}else{
					ngTagSong++;
					continue;
				}
				if(file.isFile()) sizeOK += file.length();

				okSong++;
			} catch (UnsupportedTagException | InvalidDataException | IOException | IllegalArgumentException e) {
				continue;
			}
		}

		artistCount = artists.size();
		albumCount = albums.size();
		conn.close();
	}

	/**
	 * データベースをリフレッシュします。音源からデータを取り直し、最新のデータベースに更新します。
	 * @throws SQLException SQLで問題が発生したときにスローします。
	 * @throws ClassNotFoundException
	 */
	public static void staticRefreshDB() throws SQLException, ClassNotFoundException{
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:MusicFiles.db");
		createTable();

		List<File> songDir = getIterateListFiles(new File("music"));
		for(File file : songDir){
			PreparedStatement pre_statement = conn.prepareStatement("INSERT INTO `" + TABLE_NAME + "` (title, artist, album, path) VALUES (?, ?, ?, ?)");
			try {
				Mp3File mp3file = new Mp3File(file);

				if(mp3file.hasId3v2Tag()){
					ID3v2 id3v2Tag = mp3file.getId3v2Tag();

					String title = id3v2Tag.getTitle();
					String artist = id3v2Tag.getArtist();
					String album = id3v2Tag.getAlbum();

					pre_statement.setString(1, title);
					pre_statement.setString(2, artist);
					pre_statement.setString(3, album);
					pre_statement.setString(4, file.getCanonicalPath());
					pre_statement.executeUpdate();
				}else if(mp3file.hasId3v1Tag()){
					ID3v1 id3v1Tag = mp3file.getId3v1Tag();

					String title = id3v1Tag.getTitle();
					String artist = id3v1Tag.getArtist();
					String album = id3v1Tag.getAlbum();

					pre_statement.setString(1, title);
					pre_statement.setString(2, artist);
					pre_statement.setString(3, album);
					pre_statement.setString(4, file.getCanonicalPath());
					pre_statement.executeUpdate();
				}else{
					continue;
				}
			} catch (UnsupportedTagException | InvalidDataException | IOException | IllegalArgumentException e) {
				continue;
			}
		}
		conn.close();
	}
	/**
	 * 全てのアーティストの数を返します。
	 * @return 全てのアーティストの数
	 */
	public int getArtistCount(){
		return artistCount;
	}
	/**
	 * 全てのアルバムの数を返します。
	 * @return 全てのアルバムの数
	 */
	public int getAlbumCount(){
		return albumCount;
	}
	/**
	 * 全てのファイル数を返します。
	 * @return 全てのファイル数
	 */
	public int getCount(){
		return Count;
	}
	/**
	 * 流すことのできる曲の数を返します。
	 * @return 流すことのできる曲の数
	 */
	public int getOkSong(){
		return okSong;
	}
	/**
	 * MP3Tagの設定が理由で流すことのできない曲の数を返します。
	 * @return MP3Tagの設定が理由で流すことのできない曲の数
	 */
	public int getNGTagSong(){
		return ngTagSong;
	}
	/**
	 * 全てのファイルの合計サイズを返します。
	 * @return 全てのファイルの合計サイズ
	 */
	public long getALLSize(){
		return size;
	}
	/**
	 * 流すことのできる曲のファイルの合計サイズを返します。
	 * @return 流すことのできる曲のファイルの合計サイズ
	 */
	public long getSizeOK(){
		return sizeOK;
	}

	public static List<Mp3File> Search(String text) throws SQLException, ClassNotFoundException{
		if(!existTABLE()){
			staticRefreshDB();
		}
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:MusicFiles.db");
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM `" + TABLE_NAME + "` WHERE title LIKE ? OR artist LIKE ? OR album LIKE ?");
		statement.setString(1, "%" + text + "%");
		statement.setString(2, "%" + text + "%");
		statement.setString(3, "%" + text + "%");
		ResultSet res = statement.executeQuery();

		List<Mp3File> matchFiles = new ArrayList<>();
		while(res.next()){
			String path = res.getString("path");
			File file = new File(path);
			if(!file.exists()){
				continue;
			}
			try {
				Mp3File mp3file = new Mp3File(file);
				matchFiles.add(mp3file);
			}catch(UnsupportedTagException | InvalidDataException | IOException | IllegalArgumentException e){
				continue;
			}
		}
		return matchFiles;
	}
	public static List<Mp3File> Random(int max, boolean NOInstrumental) throws SQLException, ClassNotFoundException{
		if(!existTABLE()){
			staticRefreshDB();
		}
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:MusicFiles.db");
		PreparedStatement statement;
		if(NOInstrumental){
			List<String> instSQLs = new ArrayList<>();
			for(String inst : InstList){
				instSQLs.add("title NOT LIKE '%" + inst + "%'");
			}
			String instSQL = String.join(" AND ", instSQLs);
			System.out.println("SELECT * FROM `" + TABLE_NAME + "` WHERE " +
					instSQL
					+ " ORDER BY RANDOM() LIMIT ?");
			statement = conn.prepareStatement("SELECT * FROM `" + TABLE_NAME + "` WHERE " +
					instSQL
					+ " ORDER BY RANDOM() LIMIT ?");
		}else{
			System.out.println("SELECT * FROM `" + TABLE_NAME + "` ORDER BY RANDOM() LIMIT ?");
			statement = conn.prepareStatement("SELECT * FROM `" + TABLE_NAME + "` ORDER BY RANDOM() LIMIT ?");
		}
		statement.setInt(1, max);
		ResultSet res = statement.executeQuery();

		List<Mp3File> matchFiles = new ArrayList<>();
		while(res.next()){
			String path = res.getString("path");
			File file = new File(path);
			if(!file.exists()){
				continue;
			}
			try {
				Mp3File mp3file = new Mp3File(file);
				matchFiles.add(mp3file);
			}catch(UnsupportedTagException | InvalidDataException | IOException | IllegalArgumentException e){
				continue;
			}
		}
		return matchFiles;
	}
	public static List<Mp3File> OnlySearch(String type, String text) throws SQLException, ClassNotFoundException{
		if(!existTABLE()){
			staticRefreshDB();
		}
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:MusicFiles.db");
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM `" + TABLE_NAME + "` WHERE ? LIKE ?");
		statement.setString(1, type);
		statement.setString(2, "%" + text + "%");
		ResultSet res = statement.executeQuery();

		List<Mp3File> matchFiles = new ArrayList<>();
		while(res.next()){
			String path = res.getString("path");
			File file = new File(path);
			if(!file.exists()){
				continue;
			}
			try {
				Mp3File mp3file = new Mp3File(file);
				matchFiles.add(mp3file);
			}catch(UnsupportedTagException | InvalidDataException | IOException | IllegalArgumentException e){
				continue;
			}
		}
		return matchFiles;
	}

	/**
	 * テーブルが存在するかどうかを調べます。
	 * @return 存在すればtrue
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private static boolean existTABLE() throws SQLException, ClassNotFoundException{
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:MusicFiles.db");
		Statement statement = conn.createStatement();
		ResultSet res = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' and name='" + TABLE_NAME + "';");
		if(res.next()){
			if(res.getInt("COUNT(*)") != 0){
				conn.close();
				return true;
			}
		}
		conn.close();
		return false;
	}

	/**
	 * 指定されたディレクトリ内のファイルをListとして返します。
	 * @param dir ディレクトリ
	 * @return ディレクトリ内のファイル
	 */
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
}
