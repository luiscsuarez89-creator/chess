import java.util.List;

public abstract class Piece {
    public final PieceType type;
    public final Color color;

    protected Piece(PieceType type, Color color) {
        this.type = type;
        this.color = color;
    }

    public char asBoardChar() {
        char c = type.symbol;
        return color == Color.WHITE ? c : Character.toLowerCase(c);
    }

    public abstract void addPseudoLegalMoves(Board board, int row, int col, List<Move> moves, Game game);

    protected void addSlidingMoves(Board board, int r, int c, List<Move> out, int[][] directions) {
        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];
            while (Board.inBounds(nr, nc)) {
                Piece target = board.get(nr, nc);
                if (target == null) {
                    out.add(new Move(r, c, nr, nc, null));
                } else {
                    if (target.color != this.color) {
                        out.add(new Move(r, c, nr, nc, null));
                    }
                    break;
                }
                nr += dir[0];
                nc += dir[1];
            }
        }
    }
}
