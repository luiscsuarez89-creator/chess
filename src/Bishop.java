import java.util.List;

public class Bishop extends Piece {
    private static final int[][] DIRECTIONS = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };

    public Bishop(Color color) {
        super(PieceType.BISHOP, color);
    }

    @Override
    public void addPseudoLegalMoves(Board board, int r, int c, List<Move> out, Game game) {
        addSlidingMoves(board, r, c, out, DIRECTIONS);
    }
}
