package tictactoe;

import javax.swing.*;

public class XOButton extends JButton {
    private ImageIcon XIcon;
    private ImageIcon OIcon;
    private enum State {
        BLANK,
        X,
        O
    }
    private State currState;


    public XOButton() throws NullPointerException {
        this.XIcon = new ImageIcon(this.getClass().getResource("../XIcon.png"));
        this.OIcon = new ImageIcon(this.getClass().getResource("../OIcon.png"));
        this.reset();
    }

    public State getState() {
        return this.currState;
    }

    public boolean isEmpty() {
        return this.currState == State.BLANK;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return this.currState == ((XOButton)obj).currState;
    }

    @Override
    public int hashCode() {
        return 31*this.currState.ordinal();
    }

    public void reset() {
        this.currState = State.BLANK;
        this.setIcon(null);
    }

    void setAs(Player player) {
        if (player == Player.X) {
            this.currState = State.X;
            this.setIcon(this.XIcon);
        }
        else {
            this.currState = State.O;
            this.setIcon(this.OIcon);
        }
    }
}
