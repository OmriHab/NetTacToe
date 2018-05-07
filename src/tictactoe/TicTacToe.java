package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class TicTacToe extends JFrame {

    private Board board;

    private Socket           serverConnection;
    private DataInputStream  inputStream;
    private DataOutputStream outputStream;

    private int     port;
    private String  ip;
    private Player  youPlayer;
    private Player  enemyPlayer;

    private boolean yourTurn;
    private boolean endOfGame;
    private boolean afterEnd;

    private CardLayout cLayout;
    private JPanel     mainPanel;
    private JMenuItem  restartMenuItem;

    private TicTacToe() {
        initUI();
        this.ip   = "159.89.16.28";
        this.port = 8887;
        this.setVisible(true);
        if (!this.connectToServer()) {
            JOptionPane.showMessageDialog(this, "Error connection to the server", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        new Thread(this::serverListener).start();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainPanel = new JPanel();
        cLayout   = new CardLayout();
        mainPanel.setLayout(cLayout);

        JLabel waiting = new JLabel("   Waiting for connection...");
        waiting.setFont(new Font("Serif", Font.BOLD, 24));
        mainPanel.add(waiting);

        // Init and add board
        try {
            this.board = new Board();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading image resources", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        mainPanel.add(this.board);
        this.board.addMouseListenersToChildren(new XOButtonListener());
        this.initMenu();

        this.add(mainPanel);

        this.afterEnd = false;

        /*---Set frame properties---*/
        this.setSize(400,400);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initMenu() {
        JMenuBar  menuBar = new JMenuBar();
        JMenu     game    = new JMenu("Game");

        JMenuItem exit       = new JMenuItem("Exit");
        this.restartMenuItem = new JMenuItem("Restart");

        game.setToolTipText("Game options");

        exit.setMnemonic(KeyEvent.VK_E);
        exit.addActionListener(e -> System.exit(0));
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));

        this.restartMenuItem.setMnemonic(KeyEvent.VK_R);
        this.restartMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        this.restartMenuItem.addActionListener(e -> resetGame());
        this.restartMenuItem.setEnabled(false);

        menuBar.add(game);
        game.add(exit);
        game.add(this.restartMenuItem);
        this.setJMenuBar(menuBar);
    }

    private void showDialog(String title, String message) {
        String[] options = {"Restart", "Close"};
        int choice = JOptionPane.showOptionDialog(TicTacToe.this, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            this.resetGame();
        }
        else {
            // Freeze game
            this.afterEnd = true;
            this.restartMenuItem.setEnabled(true);
        }
    }

    private void showWinDialog() {
        this.showDialog("Yay!!", "You win!!");
    }

    private void showLoseDialog() {
        this.showDialog(":(", "You lose :(");
    }

    private void showTieDialog() {
        this.showDialog("Tie", "Tie");
    }

    private void resetGame() {
        this.board.reset();
        this.board.revalidate();
        this.board.repaint();

        this.afterEnd = false;
        this.endOfGame = true;
        try {
            this.outputStream.writeInt(TicTacToeCodes.RESTART_GAME.ordinal());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.youPlayer == Player.X) {
            this.yourTurn = true;
            this.setTitle("Your turn (" + youPlayer.name() + ")");
        }
        else {
            this.yourTurn = false;
            this.setTitle("Opponents turn (" + enemyPlayer.name() + ")");
        }
    }

    private void serverListener() {
        while (true) {
            // If server not supposed to send data, dont listen
            if (!this.endOfGame && (this.inputStream == null || yourTurn)) {
                try {
                    sleep(20);
                    continue;
                } catch (InterruptedException e) {
                    return;
                }
            }
            try {
                if (this.endOfGame) {
                    // Wait for confirmation of enemy restart
                    int enemyMove  = this.inputStream.readInt();
                    if (enemyMove != TicTacToeCodes.RESTART_GAME.ordinal()) {
                        throw new IOException("Unexpected Signal: " + enemyMove + " Expected: " + TicTacToeCodes.RESTART_GAME.ordinal());
                    }
                    this.endOfGame = false;
                    this.restartMenuItem.setEnabled(false);
                }
                int enemyMove = this.inputStream.read();
                if (enemyMove == -1) {
                    throw new IOException("Unexpected EOF");
                }
                this.board.setAt(enemyMove, enemyPlayer);
            }
            catch (IOException e) {
                e.printStackTrace();
                try {
                    inputStream.close();
                    outputStream.close();
                    serverConnection.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                String[] options = {"Ok", "Exit"};
                if (JOptionPane.showOptionDialog(this, "Connection lost", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]) == 1) {
                    System.exit(0);
                }
                return;
            }
            // Check for a win
            if (TicTacToe.this.board.checkWin()) {
                if (yourTurn) {
                    TicTacToe.this.showWinDialog();
                }
                else {
                    TicTacToe.this.showLoseDialog();
                }
            }
            else if (TicTacToe.this.board.isFull()) {
                TicTacToe.this.showTieDialog();
            }
            else {
                // Set next turn
                this.yourTurn = true;
                this.setTitle("Your turn (" + youPlayer.name() + ")");
            }
        }
    }

    private class XOButtonListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent ev) {
            // If after end or not your turn, freeze app
            if (TicTacToe.this.afterEnd || !TicTacToe.this.yourTurn || TicTacToe.this.endOfGame) {
                return;
            }
            XOButton buttonClicked = (XOButton) ev.getSource();
            // If button is already set, ignore click
            if (!buttonClicked.isEmpty()) {
                return;
            }
            // Set button state
            buttonClicked.setAs(TicTacToe.this.youPlayer);
            // Send button clicked
            try {
                outputStream.write(TicTacToe.this.board.find(buttonClicked));
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    inputStream.close();
                    outputStream.close();
                    serverConnection.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            // Check for a win
            if (TicTacToe.this.board.checkWin()) {
                if (yourTurn) {
                    TicTacToe.this.showWinDialog();
                }
                else {
                    TicTacToe.this.showLoseDialog();
                }
            }
            else if (TicTacToe.this.board.isFull()) {
                TicTacToe.this.showTieDialog();
            }
            else {
                yourTurn = false;
                TicTacToe.this.setTitle("Opponents turn (" + enemyPlayer.name() + ")");
            }
        }
    }

    private boolean connectToServer() {
        // Connect to server
        int self_player;
        int attempt = 3;
        while (true) {
            try {
                this.serverConnection = new Socket(ip, port);
                this.inputStream = new DataInputStream(this.serverConnection.getInputStream());
                this.outputStream = new DataOutputStream(this.serverConnection.getOutputStream());
                self_player = inputStream.readInt();
                break;
            } catch (IOException e) {
                if (attempt < 3) {
                    try {
                        attempt++;
                        sleep(2000);
                        continue;
                    } catch (InterruptedException e1) {
                        return false;
                    }
                }
                String message = String.format("Failed to connect to %s:%d\nRetry?", ip, port);
                String[] options = {"Yes", "No"};
                int choice = JOptionPane.showOptionDialog(this, message, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (choice == 0) {
                    attempt = 0;
                    continue;
                }
                System.exit(1);
            }
        }

        if (self_player == TicTacToeCodes.PLAYER_X.ordinal()) {
            this.yourTurn    = true; // X goes first
            this.youPlayer   = Player.X;
            this.enemyPlayer = Player.O;
            this.setTitle("Your turn (" + youPlayer.name() + ")");
        }
        else if (self_player == TicTacToeCodes.PLAYER_O.ordinal()){
            this.yourTurn    = false; // O goes second
            this.youPlayer   = Player.O;
            this.enemyPlayer = Player.X;
            this.setTitle("Opponents turn (" + enemyPlayer.name() + ")");
        }
        // If received bad codes
        else {
            return false;
        }
        this.cLayout.next(this.mainPanel);
        return true;
    }


    public static void main(String[] args) {
        TicTacToe game = new TicTacToe();
    }

}
