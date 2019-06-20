package bg.sofia.uni.fmi.mjt.spotify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

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
	
	
	
	@After
	public void tearDown() {
		file.delete();
	}
}
