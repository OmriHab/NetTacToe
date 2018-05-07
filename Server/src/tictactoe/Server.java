package tictactoe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final static int    DEFAULT_PORT  = 8887;
    private final static String USAGE_MESSAGE = "Usage Server [port]\nIf no port given, default port " + DEFAULT_PORT + " will be used";

    private static int game_number = 0;
    private int listening_port;


    public Server(int listening_port) throws IOException {
        this.listening_port = listening_port;

    }

    public void runServer() throws IOException {
        try (ServerSocket listener = new ServerSocket(this.listening_port)) {
            while (true) {
                Socket PlayerX = listener.accept();
                Socket PlayerO = listener.accept();

                new Thread(new Game(PlayerX, PlayerO)).start();
            }
        }
    }

    /**
     * Parses args and returns the port specified.
     * On empty args, returns DEAFULT_PORT.
     * On illegal args, returns -1.
     *
     * @param args Command line arguments
     * @return port specified in args. On empty args, return DEAFULT_PORT. On illegal args, return -1.
     */
    private static int parseArgsForPort(String[] args) {
        int port = -1;
        // No args
        if (args == null || args.length == 0) {
            return Server.DEFAULT_PORT;
        }
        // Too many args
        if (args.length > 1) {
            System.out.println(USAGE_MESSAGE);
            return -1;
        }

        try {
            port = Integer.parseInt(args[0]);
        }
        // Not a legal number
        catch (NumberFormatException e) {
            System.out.println(USAGE_MESSAGE);
            System.out.println(args[0] + " Not a number");
            return -1;
        }
        // Not a legal port number
        if (port < 1 || port > 65535) {
            System.out.println(USAGE_MESSAGE);
            System.out.println("Port must be in range of 1-65535");
            return -1;
        }
        return port;
    }

    public static void main(String[] args) {
        int bind_port = parseArgsForPort(args);
        if (bind_port == -1) {
            return;
        }

        try {
            Server gameManager = new Server(bind_port);
            gameManager.runServer();
        }
        catch (IOException e) {
            System.out.println("Error setting up server, try again later...");
            e.printStackTrace();
        }
    }
}
