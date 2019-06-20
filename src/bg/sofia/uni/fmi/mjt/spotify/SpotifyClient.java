package bg.sofia.uni.fmi.mjt.spotify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class SpotifyClient {

	private static final String HOST = "localhost";
	private static final int PORT = 8080;

	private static final String VALIDATION_MESSAGE = "Successfully logged into the server";
	private static final int COMMAND_INDEX = 0;
	private static final int FIRST_COMMAND_PARAMETER = 1;
	private static final int SECOND_COMMAND_PARAMETER = 2;

	private PrintWriter writer;
	private BufferedReader reader;
	private boolean isClientConnected = false;

	public void run() {

		try (Scanner sc = new Scanner(System.in)) {

			while (true) {
				String[] commandInput = sc.nextLine().split(" ");

				String command = commandInput[COMMAND_INDEX];

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

		String email = commandInput[FIRST_COMMAND_PARAMETER];
		String password = commandInput[SECOND_COMMAND_PARAMETER];

		String message = SpotifyServer.registerUser(email, password);
		System.out.println(message);
	}

	public void login(String[] commandInput) {

		try {

			Socket socket = new Socket(HOST, PORT);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);

			String email = commandInput[FIRST_COMMAND_PARAMETER];
			String password = commandInput[SECOND_COMMAND_PARAMETER];

			writer.println(email + " " + password);

			String validationMessage = reader.readLine();
			System.out.println(validationMessage);

			if (validationMessage.equals(VALIDATION_MESSAGE)) {

				isClientConnected = true;
				ClientRunnable clientRunnable = new ClientRunnable(socket);
				new Thread(clientRunnable).start();
			} else {

				socket.close();
			}

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
