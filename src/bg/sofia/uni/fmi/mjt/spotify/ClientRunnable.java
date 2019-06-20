package bg.sofia.uni.fmi.mjt.spotify;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class ClientRunnable implements Runnable {

	private static final int BUFFER_SIZE = 4096;
	private static final String AUDIO_FORMAT = "audioFormat";

	private Socket socket;
	private boolean isSongPlaying;

	public ClientRunnable(Socket socket) {
		this.socket = socket;
		this.isSongPlaying = false;
	}

	@Override
	public void run() {

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			while (true) {
				if (socket.isClosed()) {
					System.out.println("Client is closed, stop waiting for server messages");
					return;
				}

				String line;
				while ((line = reader.readLine()) != null) {

					if (line.startsWith(AUDIO_FORMAT)) {
						playSong(line);
						continue;
					}
					if (line.equals(Commands.STOP.getCommandName())) {
						stopSong();
						continue;
					}
					if (line.equals(Commands.DISCONNECT.getCommandName())) {
						socket.close();
					}
					System.out.println(line);
				}
			}
		} catch (IOException e) {
			System.out.println("Socket is closed due to disconnect");
		}
	}

	public void playSong(String format) {

		String[] formatParameters = format.split(" ");

		Encoding encoding = new Encoding(formatParameters[1]);
		float sampleRate = Float.parseFloat(formatParameters[2]);
		int sampleSizeInBits = Integer.parseInt(formatParameters[3]);
		int channels = Integer.parseInt(formatParameters[4]);
		int frameSize = Integer.parseInt(formatParameters[5]);
		float frameRate = Float.parseFloat(formatParameters[6]);
		boolean bigEndian = Boolean.parseBoolean(formatParameters[7]);

		try {
			AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize,
					frameRate, bigEndian);

			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);

			audioLine.open();
			audioLine.start();

			System.out.println("Playing audio");

			isSongPlaying = true;
			byte[] bytesBuffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;

			DataInputStream receiver = new DataInputStream(socket.getInputStream());

			while (isSongPlaying && (bytesRead = receiver.read(bytesBuffer)) != -1) {
				audioLine.write(bytesBuffer, 0, bytesRead);
				if(bytesRead < 4096) {
					stopSong();
				}
			}
			
			if (!isSongPlaying) {
				System.out.println("Song stopped playing");
			} else {
				isSongPlaying = false;
			}
			
			audioLine.drain();
			audioLine.close();

			System.out.println("Playback completed");

		} catch (LineUnavailableException e) {
			System.out.println("Audio line for playing back is unavailable.");
		} catch (IOException e) {
			System.out.println("Error playing the audio file.");
		}
	}

	public void stopSong() {
		isSongPlaying = false;
	}
}
