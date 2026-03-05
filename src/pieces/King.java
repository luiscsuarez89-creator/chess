package pieces;

import board.*;
import main.*;

import java.util.List;

public class King extends Piece {
    public King(Color color) {
        super(PieceType.KING, color);
    }

    @Override
    public void addPseudoLegalMoves(Board board, int r, int c, List<Move> out, Game game) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0)
                    continue;
                int nr = r + dr;
                int nc = c + dc;
                if (!Board.inBounds(nr, nc))
                    continue;
                Piece target = board.get(nr, nc);
                if (target == null || target.color != this.color) {
                    out.add(new Move(r, c, nr, nc, null));
                }
            }
        }
        addCastlingMoves(board, r, c, out, game);
    }

    private void addCastlingMoves(Board board, int r, int c, List<Move> out, Game game) {
        if (game.isInCheck(this.color))
            return;

        if (this.color == Color.WHITE) {
            if (!game.isWhiteKingMoved() && !game.isWhiteHRookMoved() && board.get(7, 5) == null
                    && board.get(7, 6) == null) {
                if (!game.isSquareAttacked(7, 5, Color.BLACK) && !game.isSquareAttacked(7, 6, Color.BLACK)) {
                    Piece rook = board.get(7, 7);
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == Color.WHITE) {
                        out.add(new Move(r, c, 7, 6, null));
                    }
                }
            }
            if (!game.isWhiteKingMoved() && !game.isWhiteARookMoved() && board.get(7, 1) == null
                    && board.get(7, 2) == null && board.get(7, 3) == null) {
                if (!game.isSquareAttacked(7, 2, Color.BLACK) && !game.isSquareAttacked(7, 3, Color.BLACK)) {
                    Piece rook = board.get(7, 0);
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == Color.WHITE) {
                        out.add(new Move(r, c, 7, 2, null));
                    }
                }
            }
        } else {
            if (!game.isBlackKingMoved() && !game.isBlackHRookMoved() && board.get(0, 5) == null
                    && board.get(0, 6) == null) {
                if (!game.isSquareAttacked(0, 5, Color.WHITE) && !game.isSquareAttacked(0, 6, Color.WHITE)) {
                    Piece rook = board.get(0, 7);
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == Color.BLACK) {
                        out.add(new Move(r, c, 0, 6, null));
                    }
                }
            }
            if (!game.isBlackKingMoved() && !game.isBlackARookMoved() && board.get(0, 1) == null
                    && board.get(0, 2) == null && board.get(0, 3) == null) {
                if (!game.isSquareAttacked(0, 2, Color.WHITE) && !game.isSquareAttacked(0, 3, Color.WHITE)) {
                    Piece rook = board.get(0, 0);
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == Color.BLACK) {
                        out.add(new Move(r, c, 0, 2, null));
                    }
                }
            }
        }
    }
}
