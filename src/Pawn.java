import java.util.List;

public class Pawn extends Piece {
    public Pawn(Color color) {
        super(PieceType.PAWN, color);
    }

    @Override
    public void addPseudoLegalMoves(Board board, int r, int c, List<Move> out, Game game) {
        int direction = this.color == Color.WHITE ? -1 : 1;
        int startRow = this.color == Color.WHITE ? 6 : 1;
        int promoteRow = this.color == Color.WHITE ? 0 : 7;

        int oneStepRow = r + direction;
        if (Board.inBounds(oneStepRow, c) && board.get(oneStepRow, c) == null) {
            addPawnAdvanceOrPromotion(r, c, oneStepRow, c, promoteRow, out);

            int twoStepRow = r + 2 * direction;
            if (r == startRow && board.get(twoStepRow, c) == null) {
                out.add(new Move(r, c, twoStepRow, c, null));
            }
        }

        int[] captureCols = { c - 1, c + 1 };
        for (int nc : captureCols) {
            int nr = r + direction;
            if (!Board.inBounds(nr, nc))
                continue;

            Piece target = board.get(nr, nc);
            if (target != null && target.color != this.color) {
                addPawnAdvanceOrPromotion(r, c, nr, nc, promoteRow, out);
            }

            if (nr == game.getEnPassantRow() && nc == game.getEnPassantCol()) {
                out.add(new Move(r, c, nr, nc, null));
            }
        }
    }

    private void addPawnAdvanceOrPromotion(int fromRow, int fromCol, int toRow, int toCol, int promoteRow,
            List<Move> out) {
        if (toRow == promoteRow) {
            out.add(new Move(fromRow, fromCol, toRow, toCol, PieceType.QUEEN));
            out.add(new Move(fromRow, fromCol, toRow, toCol, PieceType.ROOK));
            out.add(new Move(fromRow, fromCol, toRow, toCol, PieceType.BISHOP));
            out.add(new Move(fromRow, fromCol, toRow, toCol, PieceType.KNIGHT));
        } else {
            out.add(new Move(fromRow, fromCol, toRow, toCol, null));
        }
    }
}
