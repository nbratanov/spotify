package bg.sofia.uni.fmi.mjt.spotify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientConnectionRunnableTest {

	private StringWriter out;
	private PrintWriter writer;
	private ClientConnectionRunnable clientConnectionRunnable;
	private File file;
	private static final String filePath = "D:\\Java\\eclipse\\Spotify\\resources\\playlists\\test";

	@Before
	public void setUp() {
		out = new StringWriter();
		writer = new PrintWriter(out);
		clientConnectionRunnable = new ClientConnectionRunnable(null, null);
		file = new File(filePath);
		SongLibrary.setupSongLibrary();
	}

	@Test
	public void testGetTheNamesOfAllSongs() {

		clientConnectionRunnable.search(" ", writer);

		String actual = out.toString();
		String expected = "Marshmello - Alone.wav" + System.lineSeparator() + "Final Countdown.wav"
				+ System.lineSeparator();
		assertEquals(expected, actual);
	}

	@Test
	public void testSearchSongByGivenKeyWord() {

		clientConnectionRunnable.search("final", writer);

		String actual = out.toString();
		String expected = "Final Countdown.wav" + System.lineSeparator();
		assertEquals(expected, actual);
	}

	@Test
	public void testCreateFileGivenNewPlaylist() {

		clientConnectionRunnable.createPlaylist("test", writer);

		assertTrue(file.exists());
	}

	@Test
	public void testCreateFileGivenExistingPlaylist() {

		clientConnectionRunnable.createPlaylist("chill", writer);

		String actual = out.toString();
		String expected = "Playlist chill already exists" + System.lineSeparator();
		assertEquals(expected, actual);
	}

	private String readFile(File file, Charset charset) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), charset);
	}

	@Test
	public void testAddSongToPlaylist() throws IOException {

		clientConnectionRunnable.createPlaylist("test", writer);
		String oldFileOutput = readFile(file, Charset.defaultCharset());

		clientConnectionRunnable.addSongToPlaylist("test", "Final Countdown", writer);

		String expectedFileOutput = oldFileOutput + "Final Countdown" + System.lineSeparator();
		String actualFileOutput = readFile(file, Charset.defaultCharset());
		assertEquals(expectedFileOutput, actualFileOutput);
	}

	@Test
	public void testAddSongToPlaylistGivenItIsAlreadyAdded() throws IOException {

		File file = new File("D:\\Java\\eclipse\\Spotify\\resources\\playlists\\chill");
		String oldFileOutput = readFile(file, Charset.defaultCharset());
		clientConnectionRunnable.addSongToPlaylist("chill", "Marshmello - Alone", writer);

		String newFileOutput = readFile(file, Charset.defaultCharset());
		assertEquals(newFileOutput, oldFileOutput);
	}

	@Test
	public void testShowPlaylist() throws IOException {

		File file = new File("D:\\Java\\eclipse\\Spotify\\resources\\playlists\\chill");
		String expected = readFile(file, Charset.defaultCharset());
		clientConnectionRunnable.showPlaylist("chill", writer);

		String actual = out.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void testShowPlaylistGivenNotExistingOutput() throws IOException {
		
		clientConnectionRunnable.showPlaylist("test", writer);

		String actual = out.toString();
		String expected = "There is no playlist: test" + System.lineSeparator(); 
		assertEquals(expected, actual);
	}

	@After
	public void tearDown() {
		file.delete();
	}
}
