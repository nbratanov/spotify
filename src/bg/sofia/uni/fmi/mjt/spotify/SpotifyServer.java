package bg.sofia.uni.fmi.mjt.spotify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SpotifyServer {

	private static final int PORT = 8080;
	private static final String USERS_PATH = "D:\\Java\\eclipse\\Spotify\\resources\\users";
	private static final String ALREADY_TAKEN_MESSAGE = "Username is already taken";
	private static final String SUCCESSFUL_MESSAGE = "Successfully registered user: ";

	private static final int EMAIL_INDEX = 0;
	private static final int PASSWORD_INDEX = 1;

	private static Map<String, Socket> loggedUsers = new HashMap<>();

	public synchronized static String registerUser(String email, String password) {

		File usersFile = new File(USERS_PATH);
		try (BufferedReader reader = new BufferedReader(new FileReader(usersFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(usersFile, true))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(" ");
				if (tokens[EMAIL_INDEX].equals(email)) {
					return ALREADY_TAKEN_MESSAGE;
				}
			}

			writer.write(email + " " + password + System.lineSeparator());
		} catch (FileNotFoundException e) {
			System.out.println("Users file not found");
		} catch (IOException e) {
			System.out.println("Error in reading or writing the file");
		}

		return SUCCESSFUL_MESSAGE + email;
	}

	private String validatePassword(String user) {
		File usersFile = new File(USERS_PATH);
		try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(" ");
				if (tokens[EMAIL_INDEX].equals(user)) {
					return tokens[PASSWORD_INDEX];
				}
			}
		} catch (IOException e) {
			System.out.println("Error in reading or writing the file");
		}
		return null;
	}

	private synchronized String approveLogin(String userDetails, PrintWriter writer) {

		String[] tokens = userDetails.split(" ");
		String email = tokens[EMAIL_INDEX];
		String password = tokens[PASSWORD_INDEX];

		String realPassword = validatePassword(email);
		if (realPassword == null) {
			writer.println("There is not a user registered with that email");
			return null;
		}
		if (!password.equals(realPassword)) {
			writer.println("The password you entered is incorrect");
			return null;
		}

		if (getLoggedUsers().containsKey(email)) {
			writer.println("Someone is already logged into this email");
			return null;
		}

		writer.println("Successfully logged into the server");
		return email;
	}

	public synchronized static Map<String, Socket> getLoggedUsers() {
		return loggedUsers;
	}

	public synchronized static void removeLoggedUser(String email) {
		loggedUsers.remove(email);
	}

	public void run() {

		SongLibrary.setupSongLibrary();

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {

			System.out.printf("Server is running on localhost: %d%n", PORT);

			while (true) {
				Socket socket = serverSocket.accept();

				System.out.println("Client connected to the server: " + socket.getInetAddress());

				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

				String userDetails = reader.readLine();
				String email = approveLogin(userDetails, writer);

				if (email != null) {

					loggedUsers.put(email, socket);
					System.out.println(email + " logged into the server");

					ClientConnectionRunnable clientConnectionRunnable = new ClientConnectionRunnable(email, socket);
					new Thread(clientConnectionRunnable).start();
				}
			}

		} catch (IOException e) {
			System.out.println("There was a problem when trying to connect to the server");
		}
	}

	public static void main(String[] args) {
		new SpotifyServer().run();
	}
}
