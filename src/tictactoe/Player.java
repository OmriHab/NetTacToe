package tictactoe;

public enum Player {
    X, O;
    private static Player[] vals = values();
    public Player next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }
    public char getVal() {
        return (this == X) ? 'X' : 'O';
    }
}