package bg.sofia.uni.fmi.mjt.spotify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class ClientConnectionRunnable implements Runnable {

	private static final String FOLDER_PATH = "D:\\Java\\eclipse\\Spotify\\resources\\songs";
	private static final String PLAYLISTS_FOLDER_PATH = "D:\\Java\\eclipse\\Spotify\\resources\\playlists";

	private static final int BUFFER_SIZE = 4096;

	private String email;
	private Socket socket;
	private boolean isSongPlaying;

	public ClientConnectionRunnable(String email, Socket socket) {
		this.email = email;
		this.socket = socket;
		isSongPlaying = false;
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

			while (true) {
				String commandInput = reader.readLine();
				System.out.println(commandInput);
				
				if (commandInput != null) {
					String[] tokens = commandInput.split(" ");
					String command = tokens[0];

					if (command.equals(Commands.DISCONNECT.getCommandName())) {
						disconnect();
					}
					if (command.equals(Commands.SEARCH.getCommandName())) {
						String keyWords = transformInput(tokens, 1);
						search(keyWords, writer);
					}
					if (command.equals(Commands.TOP.getCommandName())) {
						top(Integer.parseInt(tokens[1]), writer);
					}
					if (command.equals(Commands.PLAY.getCommandName())) {
						String songName = transformInput(tokens, 1);
						play(songName, writer);
					}
					if (command.equals(Commands.STOP.getCommandName())) {
						stopSong(writer);
					}
					if (command.equals(Commands.CREATE_PLAYLIST.getCommandName())) {
						String playlistName = transformInput(tokens, 1);
						createPlaylist(playlistName, writer);
					}
					if (command.equals(Commands.ADD_SONG_TO_PLAYLIST.getCommandName())) {
						String playlistName = tokens[1];
						String songName = transformInput(tokens, 2);
						addSongToPlaylist(playlistName, songName, writer);
					}
					if (command.equals(Commands.SHOW_PLAYLIST.getCommandName())) {
						String playlistName = transformInput(tokens, 1);
						showPlaylist(playlistName, writer);
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Client was stopped");
		}
	}

	public void disconnect() throws IOException {
		if (SpotifyServer.getLoggedUsers().containsKey(email)) {
			SpotifyServer.removeLoggedUser(email);
		}

		socket.close();
		return;
	}

	public void search(String keyWords, PrintWriter writer) {

		Set<String> matchingSongs = SongLibrary.findMatchingSongs(keyWords);
		for (String song : matchingSongs) {
			writer.println(song.toString());
		}
	}

	public void top(int number, PrintWriter writer) {

		Set<String> topSongs = SongLibrary.mostPlayedSongs(number);
		for (String song : topSongs) {
			writer.println(song.toString());
		}
	}

	public void play(String songName, PrintWriter writer) {

		boolean songExists = false;
		File[] files = new File(FOLDER_PATH).listFiles();
		for (File file : files) {
			if (file.getName().equals(songName + ".wav")) {
				songExists = true;
			}
		}

		if (!songExists) {
			writer.println("You cannot play a song that is not in the library.");
			return;
		}

		File file = new File(FOLDER_PATH + "\\" + songName + ".wav");
		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
			AudioFormat audioFormat = audioStream.getFormat();

			writer.println("audioFormat " + audioFormat.getEncoding().toString() + " " + audioFormat.getSampleRate()
					+ " " + audioFormat.getSampleSizeInBits() + " " + audioFormat.getChannels() + " "
					+ audioFormat.getFrameSize() + " " + audioFormat.getFrameRate() + " " + audioFormat.isBigEndian());

			byte[] bytesBuffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			isSongPlaying = true;

			DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
			while (isSongPlaying && (bytesRead = audioStream.read(bytesBuffer)) != -1) {
				sender.write(bytesBuffer, 0, bytesRead);
			}

			SongLibrary.increaseTimesSongIsPlayed(songName);
			audioStream.close();

		} catch (UnsupportedAudioFileException e) {
			System.out.println("The specified audio file is not supported");
		} catch (IOException e) {
			System.out.println("Error playing the audio file");
		}
	}
		
	public void stopSong(PrintWriter writer) {
		isSongPlaying = false;
		writer.println(Commands.STOP.getCommandName());
	}

	public void createPlaylist(String playlistName, PrintWriter writer) {
		File file = new File(PLAYLISTS_FOLDER_PATH + "\\" + playlistName);

		try {
			if (file.createNewFile()) {
				writer.println("Created a playlist: " + playlistName);
			} else {
				writer.println("Playlist " + playlistName + " already exists");
			}
		} catch (IOException e) {
			System.out.println("Could not create file");
		}
	}

	public void addSongToPlaylist(String playlistName, String songName, PrintWriter writer) {

		File[] files = new File(PLAYLISTS_FOLDER_PATH).listFiles();
		for (File file : files) {
			if (file.getName().equals(playlistName)) {
				try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
						BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true))) {
					String line = null;
					while ((line = bufferedReader.readLine()) != null) {
						if (songName.toString().equals(line)) {
							writer.println("This song is already added in the playlist");
							return;
						}
					}

					bufferedWriter.write(songName + System.lineSeparator());
					writer.println("Song successfully added to playlist");
					return;
				} catch (FileNotFoundException e) {
					System.out.println("Invalid file path");
				} catch (IOException e) {
					System.out.println("There was a problem with reading from the playlist file");
				}
			}
		}

		writer.println("There is no playlist: " + playlistName);
	}

	public void showPlaylist(String playlistName, PrintWriter writer) throws IOException {
		File[] playlists = new File(PLAYLISTS_FOLDER_PATH).listFiles();
		if (playlists == null) {
			writer.println("There are currently no playlists created");
			return;
		}
		
		for (File file : playlists) {
			if (file.getName().equals(playlistName)) {
				try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
					String line = null;
					while ((line = fileReader.readLine()) != null) {
						writer.println(line);
					}
				}
				return;
			}
		}
		
		writer.println("There is no playlist: " + playlistName);
	}

	private String transformInput(String[] commandInput, int startIndex) {
		StringBuilder result = new StringBuilder();
		for (int i = startIndex; i < commandInput.length; i++) {
			result.append(commandInput[i]);
			if (i != commandInput.length - 1) {
				result.append(" ");
			}
		}
		return result.toString();
	}
}
