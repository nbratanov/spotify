package bg.sofia.uni.fmi.mjt.spotify;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class SpotifyClient {

	private static final String HOST = "localhost";
	private static final int PORT = 8080;

	private PrintWriter writer;
	private boolean isClientConnected = false;

	public void run() {

		try (Scanner sc = new Scanner(System.in)) {

			while (true) {
				String[] commandInput = sc.nextLine().split(" ");

				String command = commandInput[0];

				if (command.equals(Commands.REGISTER.getCommandName())) {
					register(commandInput);
				} else if (command.equals(Commands.LOGIN.getCommandName())) {
					login(commandInput);
				} else {
					if (!isClientConnected) {
						System.out.println("Client is not connected to the server");
					} else {
						writer.println(String.join(" ", commandInput));

						if (String.join(" ", commandInput).equals(Commands.DISCONNECT.getCommandName())) {
							System.out.println("Closing client");
							writer.println(Commands.DISCONNECT.getCommandName());
							return;
						}
					}
				}
			}
		}
	}

	public void register(String[] commandInput) {

		String email = commandInput[1];
		String password = commandInput[2];

		String message = SpotifyServer.registerUser(email, password);
		System.out.println(message);
	}

	public void login(String[] commandInput) {

		String email = commandInput[1];
		String password = commandInput[2];

		String realPassword = SpotifyServer.validatePassword(email);
		if (realPassword == null) {
			System.out.println("There is not a user registered with that email");
			return;
		}
		if (!password.equals(realPassword)) {
			System.out.println("The password you entered is incorrect");
			return;
		}

		if (SpotifyServer.getLoggedUsers().containsKey(email)) {
			System.out.println("Someone is already logged into this email");
			return;
		}

		try {

			Socket socket = new Socket(HOST, PORT);
			writer = new PrintWriter(socket.getOutputStream(), true);

			System.out.println("Successfully logged into the server");
			isClientConnected = true;
			writer.println(email);
			
			ClientRunnable clientRunnable = new ClientRunnable(socket);
			new Thread(clientRunnable).start();

		} catch (UnknownHostException e) {
			System.out.printf("Cannot recognise host: %s, make sure that the server is started%n", HOST);
		} catch (IOException e) {
			System.out.printf("Cannot connect to server on %s:%d, make sure that the server is started%n", HOST, PORT);
		}
	}

	public static void main(String[] args) {
		new SpotifyClient().run();
	}
}
