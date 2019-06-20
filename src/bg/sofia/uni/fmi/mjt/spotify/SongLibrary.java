package bg.sofia.uni.fmi.mjt.spotify;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class SongLibrary {

	private static final String SONGS_FOLDER_PATH = "D:\\Java\\eclipse\\Spotify\\resources\\songs";

	private static Map<String, Integer> songs;

	public static void setupSongLibrary() {
		File[] files = new File(SONGS_FOLDER_PATH).listFiles();

		songs = new HashMap<String, Integer>();
		for (File file : files) {
			if (file.isFile()) {
				songs.put(file.getName(), 0);
			}
		}
	}

	public static Set<String> getAllSongs() {
		return songs.keySet();
	}

	public synchronized static Set<String> findMatchingSongs(String keyWords) {
		Set<String> result = new HashSet<>();

		Set<String> songNames = songs.keySet();
		for (String song : songNames) {
			if (song.toLowerCase().contains(keyWords.toLowerCase())) {
				result.add(song);
			}
		}
		return result;
	}

	public synchronized static Set<String> mostPlayedSongs(int number) {

		return songs.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).limit(number)
				.collect(Collectors.toSet());
	}

	public synchronized static void increaseTimesSongIsPlayed(String songName) {
		Set<Entry<String, Integer>> entries = songs.entrySet();
		for (Entry<String, Integer> entry : entries) {
			if (entry.getKey().equals(songName)) {
				entry.setValue(entry.getValue() + 1);
			}
		}
	}
}
