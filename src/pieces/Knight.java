package pieces;

import board.*;
import main.*;

import java.util.List;

public class Knight extends Piece {
    public Knight(Color color) {
        super(PieceType.KNIGHT, color);
    }

    @Override
    public void addPseudoLegalMoves(Board board, int r, int c, List<Move> out, Game game) {
        int[][] deltas = { { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 }, { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 } };
        for (int[] d : deltas) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (!Board.inBounds(nr, nc))
                continue;
            Piece target = board.get(nr, nc);
            if (target == null || target.color != this.color) {
                out.add(new Move(r, c, nr, nc, null));
            }
        }
    }
}
