package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

public class Board extends JPanel {
    private XOButton[] xoButtons;

    public Board() throws NullPointerException {
        this.setLayout(new GridLayout(3,3));
        this.setFocusable(true);
        // Init and add buttons
        xoButtons = new XOButton[9];
        for (int i = 0; i < 9; i++) {
            xoButtons[i] = new XOButton();
            this.add(xoButtons[i]);
        }

    }

    public void reset() {
        for (XOButton button : this.xoButtons) {
            button.reset();
        }
    }

    public void addMouseListenersToChildren(MouseListener ml) {
        for (XOButton button : this.xoButtons) {
            button.addMouseListener(ml);
        }
    }

    public void setAt(int index, Player player) {
        this.xoButtons[index].setAs(player);
    }

    public int find(XOButton button) {
        for (int i = 0; i < xoButtons.length; i++) {
            if (xoButtons[i] == button) {
                return i;
            }
        }
        return -1;
    }

    public boolean checkWin() {
        return (this.checkColumnWin() || this.checkRowWin() || this.checkDiagonalWin());
    }

    public boolean isFull() {
        for (XOButton button : this.xoButtons) {
            if (button.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /*---Win checkers---*/
    private boolean checkColumnWin() {
        for (int i = 0; i < 3; i++) {
            if (!this.xoButtons[i].isEmpty() &&
                this.xoButtons[i].equals(this.xoButtons[i+3]) &&
                this.xoButtons[i].equals(this.xoButtons[i+6]))
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkRowWin() {
        for (int i = 0; i < 9; i += 3) {
            if (!this.xoButtons[i].isEmpty() &&
                this.xoButtons[i].equals(this.xoButtons[i+1]) &&
                this.xoButtons[i].equals(this.xoButtons[i+2]))
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkDiagonalWin() {
        return (
                   (!this.xoButtons[0].isEmpty() && this.xoButtons[0].equals(this.xoButtons[4]) && this.xoButtons[0].equals(this.xoButtons[8])) ||
                   (!this.xoButtons[2].isEmpty() && this.xoButtons[2].equals(this.xoButtons[4]) && this.xoButtons[2].equals(this.xoButtons[6]))
               );
    }


}
