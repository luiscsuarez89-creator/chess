package pieces;

import board.*;
import main.*;

import java.util.List;

public class Queen extends Piece {
    private static final int[][] DIRECTIONS = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }, { 1, 0 }, { -1, 0 },
            { 0, 1 }, { 0, -1 } };

    public Queen(Color color) {
        super(PieceType.QUEEN, color);
    }

    @Override
    public void addPseudoLegalMoves(Board board, int r, int c, List<Move> out, Game game) {
        addSlidingMoves(board, r, c, out, DIRECTIONS);
    }
}
