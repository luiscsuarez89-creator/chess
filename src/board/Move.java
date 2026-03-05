package board;

import pieces.*;

import java.util.Locale;

public final class Move {
    public final int fromRow;
    public final int fromCol;
    public final int toRow;
    public final int toCol;
    public final PieceType promotion;

    public Move(int fromRow, int fromCol, int toRow, int toCol, PieceType promotion) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.promotion = promotion;
    }

    public static Move parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Entrada vacía.");
        }

        String clean = input.trim().toLowerCase(Locale.ROOT);
        clean = clean.replace("-", "").replace(" ", "");

        if (clean.length() != 4 && clean.length() != 5) {
            throw new IllegalArgumentException("Formato inválido. Usa por ejemplo e2e4 o e7e8q.");
        }

        int fromCol = fileToCol(clean.charAt(0));
        int fromRow = rankToRow(clean.charAt(1));
        int toCol = fileToCol(clean.charAt(2));
        int toRow = rankToRow(clean.charAt(3));

        PieceType promotion = null;
        if (clean.length() == 5) {
            switch (clean.charAt(4)) {
                case 'q':
                    promotion = PieceType.QUEEN;
                    break;
                case 'r':
                    promotion = PieceType.ROOK;
                    break;
                case 'b':
                    promotion = PieceType.BISHOP;
                    break;
                case 'n':
                    promotion = PieceType.KNIGHT;
                    break;
                default:
                    throw new IllegalArgumentException("Promoción inválida. Usa q, r, b o n.");
            }
        }

        return new Move(fromRow, fromCol, toRow, toCol, promotion);
    }

    public static int fileToCol(char file) {
        if (file < 'a' || file > 'h') {
            throw new IllegalArgumentException("Columna inválida: " + file);
        }
        return file - 'a';
    }

    public static int rankToRow(char rank) {
        if (rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Fila inválida: " + rank);
        }
        return 8 - (rank - '0');
    }

    public static String toSquare(int row, int col) {
        return "" + (char) ('a' + col) + (8 - row);
    }
}
