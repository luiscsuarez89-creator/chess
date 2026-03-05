import java.util.List;

public class Rook extends Piece {
    private static final int[][] DIRECTIONS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

    public Rook(Color color) {
        super(PieceType.ROOK, color);
    }

    @Override
    public void addPseudoLegalMoves(Board board, int r, int c, List<Move> out, Game game) {
        addSlidingMoves(board, r, c, out, DIRECTIONS);
    }
}
