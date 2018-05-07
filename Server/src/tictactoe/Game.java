package tictactoe;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Game implements Runnable {

    private static int game_number;

    private int[] board;

    private DataInputStream  XInStream;
    private DataInputStream  OInStream;
    private DataOutputStream XOutStream;
    private DataOutputStream OOutStream;

    private Socket PlayerX;
    private Socket PlayerO;

    private static synchronized int getGameNumber() {
        return Game.game_number++;
    }

    public Game(Socket PlayerX, Socket PlayerO) {
        if (PlayerX == null || PlayerO == null) {
            throw new IllegalArgumentException();
        }
        this.PlayerX = PlayerX;
        this.PlayerO = PlayerO;
    }

    private void manageGame() {

        int game_number = getGameNumber();
        System.out.println("Starting game " + game_number);

        try {
            XInStream  = new DataInputStream(PlayerX.getInputStream());
            OInStream  = new DataInputStream(PlayerO.getInputStream());
            XOutStream = new DataOutputStream(PlayerX.getOutputStream());
            OOutStream = new DataOutputStream(PlayerO.getOutputStream());

            board = new int[9];

            // Let each player know who he is
            XOutStream.writeInt(TicTacToeCodes.PLAYER_X.ordinal());
            OOutStream.writeInt(TicTacToeCodes.PLAYER_O.ordinal());

            DataInputStream  currPlayerInStream;
            DataOutputStream currPlayerOutStream;

            DataInputStream  secondPlayerInStream;
            DataOutputStream secondPlayerOutStream;

            boolean xTurn = true;
            while (true) {
                if (xTurn) {
                    currPlayerInStream    = XInStream;
                    currPlayerOutStream   = XOutStream;
                    secondPlayerInStream  = OInStream;
                    secondPlayerOutStream = OOutStream;
                }
                else {
                    currPlayerInStream    = OInStream;
                    currPlayerOutStream   = OOutStream;
                    secondPlayerInStream  = XInStream;
                    secondPlayerOutStream = XOutStream;
                }
                // Get current player's move
                byte move = currPlayerInStream.readByte();

                // Move should be legal
                if (!checkMove(move)) {
                    AbortGame();
                    break;
                }

                // Send move to second player
                secondPlayerOutStream.writeByte(move);
                if (xTurn) {
                    board[move] = TicTacToeCodes.PLAYER_X.ordinal();
                }
                else {
                    board[move] = TicTacToeCodes.PLAYER_O.ordinal();
                }

                // On game over, wait for restart confirmation from each player, then reset game.
                if (winCheck() || boardFull()) {
                    int x_restart_confirmation = XInStream.readInt();
                    int o_restart_confirmation = OInStream.readInt();
                    if (x_restart_confirmation == TicTacToeCodes.RESTART_GAME.ordinal() &&
                        o_restart_confirmation == TicTacToeCodes.RESTART_GAME.ordinal())
                    {
                        XOutStream.writeInt(TicTacToeCodes.RESTART_GAME.ordinal());
                        OOutStream.writeInt(TicTacToeCodes.RESTART_GAME.ordinal());
                        this.reset();
                        xTurn = true;
                        continue;
                    }
                    else {
                        AbortGame();
                        break;
                    }
                }
                xTurn = !xTurn;
            }
        }
        catch (IOException e) {
            System.out.println("Lost connection to game " + game_number);
        }
        finally {
            try {
                if (PlayerX != null) {
                    PlayerX.close();
                }
                if (PlayerO != null) {
                    PlayerO.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void reset() {
        for (int i = 0; i < 9; i++) {
            this.board[i] = 0;
        }
    }

    private void AbortGame() throws IOException {
        XOutStream.writeInt(TicTacToeCodes.ABORT_GAME.ordinal());
        OOutStream.writeInt(TicTacToeCodes.ABORT_GAME.ordinal());
    }

    /**
     * Check if move is legal in a board.
     * Prints error messages on illegal moves.
     * @return if move is legal in board.
     */
    private boolean checkMove(byte move) {
        if (move < 0 || move >= board.length) {
            System.out.println("Error in game" + game_number + ", got byte " + move);
            System.out.println("Expecting byte between 0-" + (board.length-1));
            return false;
        }
        if (board[move] != 0) {
            System.out.println("Error in game " + game_number + " place " + move + " not free");
            return false;
        }
        return true;

    }

    /**
     * Check if there is a win.
     * @return true if there is a win.
     */
    private boolean winCheck() {
        if (board == null || board.length != 9) {
            return false;
        }
        // row check
        for (int i = 0; i < 9; i += 3) {
            if (board[i] != 0 && board[i] == board[i + 1] && board[i] == board[i + 2]) {
                return true;
            }
        }
        // column check
        for (int i = 0; i < 3; i++) {
            if (board[i] != 0 && board[i] == board[i + 3] && board[i] == board[i + 6]) {
                return true;
            }
        }
        // diagonal check
        return (board[0] != 0 && board[0] == board[4] && board[0] == board[8]) ||
                (board[2] != 0 && board[0] == board[4] && board[0] == board[6]);

    }

    private boolean boardFull() {
        for (int i = 0; i < 9; i++) {
            if (board[i] == 0) {
                return false;
            }
        }
        return true;
    }

    private void shutDown() {

    }

    @Override
    public void run() {
        this.manageGame();
    }
}
