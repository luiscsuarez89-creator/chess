public final class Board {
    private final Piece[][] squares = new Piece[8][8];

    public Piece get(int row, int col) {
        if (!inBounds(row, col))
            return null;
        return squares[row][col];
    }

    public void set(int row, int col, Piece piece) {
        if (!inBounds(row, col)) {
            throw new IllegalArgumentException("Casilla fuera del tablero.");
        }
        squares[row][col] = piece;
    }

    public static boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public Board copy() {
        Board clone = new Board();
        for (int r = 0; r < 8; r++) {
            System.arraycopy(this.squares[r], 0, clone.squares[r], 0, 8);
        }
        return clone;
    }
}
