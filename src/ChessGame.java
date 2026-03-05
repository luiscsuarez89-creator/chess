import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ChessGame {
    public static void main(String[] args) {
        Game game = new Game();
        game.run();
    }
}

enum Color {
    WHITE,
    BLACK;

    Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}

enum PieceType {
    KING('K'),
    QUEEN('Q'),
    ROOK('R'),
    BISHOP('B'),
    KNIGHT('N'),
    PAWN('P');

    final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }
}

final class Piece {
    final PieceType type;
    final Color color;

    Piece(PieceType type, Color color) {
        this.type = type;
        this.color = color;
    }

    char asBoardChar() {
        char c = type.symbol;
        return color == Color.WHITE ? c : Character.toLowerCase(c);
    }
}

final class Move {
    final int fromRow;
    final int fromCol;
    final int toRow;
    final int toCol;
    final PieceType promotion;

    Move(int fromRow, int fromCol, int toRow, int toCol, PieceType promotion) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.promotion = promotion;
    }

    static Move parse(String input) {
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
            promotion = switch (clean.charAt(4)) {
                case 'q' -> PieceType.QUEEN;
                case 'r' -> PieceType.ROOK;
                case 'b' -> PieceType.BISHOP;
                case 'n' -> PieceType.KNIGHT;
                default -> throw new IllegalArgumentException("Promoción inválida. Usa q, r, b o n.");
            };
        }

        return new Move(fromRow, fromCol, toRow, toCol, promotion);
    }

    static int fileToCol(char file) {
        if (file < 'a' || file > 'h') {
            throw new IllegalArgumentException("Columna inválida: " + file);
        }
        return file - 'a';
    }

    static int rankToRow(char rank) {
        if (rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Fila inválida: " + rank);
        }
        return 8 - (rank - '0');
    }

    static String toSquare(int row, int col) {
        return "" + (char) ('a' + col) + (8 - row);
    }
}

final class Board {
    private final Piece[][] squares = new Piece[8][8];

    Piece get(int row, int col) {
        if (!inBounds(row, col)) return null;
        return squares[row][col];
    }

    void set(int row, int col, Piece piece) {
        if (!inBounds(row, col)) {
            throw new IllegalArgumentException("Casilla fuera del tablero.");
        }
        squares[row][col] = piece;
    }

    static boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    Board copy() {
        Board clone = new Board();
        for (int r = 0; r < 8; r++) {
            System.arraycopy(this.squares[r], 0, clone.squares[r], 0, 8);
        }
        return clone;
    }
}

final class Game {
    private Board board = new Board();
    private Color turn = Color.WHITE;

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteARookMoved = false;
    private boolean whiteHRookMoved = false;
    private boolean blackARookMoved = false;
    private boolean blackHRookMoved = false;

    private int enPassantRow = -1;
    private int enPassantCol = -1;

    void run() {
        setupInitialPosition();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Ajedrez Java - Introduce movimientos como e2e4, g1f3, e7e8q.");
        System.out.println("Escribe 'salir' para terminar.");

        while (true) {
            printBoard();
            if (isInCheck(turn)) {
                System.out.println("Jaque a " + colorName(turn) + ".");
            }

            List<Move> legalMoves = legalMovesFor(turn);
            if (legalMoves.isEmpty()) {
                if (isInCheck(turn)) {
                    System.out.println("Jaque mate. Gana " + colorName(turn.opposite()) + ".");
                } else {
                    System.out.println("Tablas por ahogado.");
                }
                return;
            }

            System.out.print(colorName(turn) + " mueve: ");
            String line = scanner.nextLine();
            if (line == null) continue;

            if (line.trim().equalsIgnoreCase("salir")) {
                System.out.println("Partida finalizada.");
                return;
            }

            try {
                Move move = Move.parse(line);
                if (!containsMove(legalMoves, move)) {
                    System.out.println("Movimiento ilegal según reglas del ajedrez.");
                    continue;
                }
                applyMove(move);
                turn = turn.opposite();
            } catch (IllegalArgumentException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private boolean containsMove(List<Move> legalMoves, Move move) {
        for (Move candidate : legalMoves) {
            if (candidate.fromRow == move.fromRow && candidate.fromCol == move.fromCol &&
                candidate.toRow == move.toRow && candidate.toCol == move.toCol) {

                if (candidate.promotion == null || move.promotion == null) {
                    return candidate.promotion == move.promotion;
                }
                return candidate.promotion == move.promotion;
            }
        }
        return false;
    }

    private void setupInitialPosition() {
        for (int c = 0; c < 8; c++) {
            board.set(1, c, new Piece(PieceType.PAWN, Color.BLACK));
            board.set(6, c, new Piece(PieceType.PAWN, Color.WHITE));
        }

        board.set(0, 0, new Piece(PieceType.ROOK, Color.BLACK));
        board.set(0, 7, new Piece(PieceType.ROOK, Color.BLACK));
        board.set(7, 0, new Piece(PieceType.ROOK, Color.WHITE));
        board.set(7, 7, new Piece(PieceType.ROOK, Color.WHITE));

        board.set(0, 1, new Piece(PieceType.KNIGHT, Color.BLACK));
        board.set(0, 6, new Piece(PieceType.KNIGHT, Color.BLACK));
        board.set(7, 1, new Piece(PieceType.KNIGHT, Color.WHITE));
        board.set(7, 6, new Piece(PieceType.KNIGHT, Color.WHITE));

        board.set(0, 2, new Piece(PieceType.BISHOP, Color.BLACK));
        board.set(0, 5, new Piece(PieceType.BISHOP, Color.BLACK));
        board.set(7, 2, new Piece(PieceType.BISHOP, Color.WHITE));
        board.set(7, 5, new Piece(PieceType.BISHOP, Color.WHITE));

        board.set(0, 3, new Piece(PieceType.QUEEN, Color.BLACK));
        board.set(7, 3, new Piece(PieceType.QUEEN, Color.WHITE));

        board.set(0, 4, new Piece(PieceType.KING, Color.BLACK));
        board.set(7, 4, new Piece(PieceType.KING, Color.WHITE));
    }

    private void printBoard() {
        System.out.println();
        for (int r = 0; r < 8; r++) {
            System.out.print((8 - r) + " ");
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(r, c);
                System.out.print((p == null ? '.' : p.asBoardChar()) + " ");
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h");
        System.out.println();
    }

    private String colorName(Color color) {
        return color == Color.WHITE ? "Blancas" : "Negras";
    }

    private List<Move> legalMovesFor(Color side) {
        List<Move> pseudo = pseudoLegalMoves(side);
        List<Move> legal = new ArrayList<>();
        for (Move m : pseudo) {
            StateSnapshot before = snapshot();
            applyMoveUnchecked(m);
            boolean kingInCheck = isInCheck(side);
            restore(before);
            if (!kingInCheck) {
                legal.add(m);
            }
        }
        return legal;
    }

    private List<Move> pseudoLegalMoves(Color side) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(r, c);
                if (p == null || p.color != side) continue;
                switch (p.type) {
                    case PAWN -> addPawnMoves(r, c, p, moves);
                    case KNIGHT -> addKnightMoves(r, c, p, moves);
                    case BISHOP -> addSlidingMoves(r, c, p, moves, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
                    case ROOK -> addSlidingMoves(r, c, p, moves, new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
                    case QUEEN -> addSlidingMoves(r, c, p, moves, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1},{1,0},{-1,0},{0,1},{0,-1}});
                    case KING -> {
                        addKingMoves(r, c, p, moves);
                        addCastlingMoves(r, c, p, moves);
                    }
                }
            }
        }
        return moves;
    }

    private void addPawnMoves(int r, int c, Piece pawn, List<Move> out) {
        int direction = pawn.color == Color.WHITE ? -1 : 1;
        int startRow = pawn.color == Color.WHITE ? 6 : 1;
        int promoteRow = pawn.color == Color.WHITE ? 0 : 7;

        int oneStepRow = r + direction;
        if (Board.inBounds(oneStepRow, c) && board.get(oneStepRow, c) == null) {
            addPawnAdvanceOrPromotion(r, c, oneStepRow, c, promoteRow, out);

            int twoStepRow = r + 2 * direction;
            if (r == startRow && board.get(twoStepRow, c) == null) {
                out.add(new Move(r, c, twoStepRow, c, null));
            }
        }

        int[] captureCols = {c - 1, c + 1};
        for (int nc : captureCols) {
            int nr = r + direction;
            if (!Board.inBounds(nr, nc)) continue;

            Piece target = board.get(nr, nc);
            if (target != null && target.color != pawn.color) {
                addPawnAdvanceOrPromotion(r, c, nr, nc, promoteRow, out);
            }

            if (nr == enPassantRow && nc == enPassantCol) {
                out.add(new Move(r, c, nr, nc, null));
            }
        }
    }

    private void addPawnAdvanceOrPromotion(int fromRow, int fromCol, int toRow, int toCol, int promoteRow, List<Move> out) {
        if (toRow == promoteRow) {
            out.add(new Move(fromRow, fromCol, toRow, toCol, PieceType.QUEEN));
            out.add(new Move(fromRow, fromCol, toRow, toCol, PieceType.ROOK));
            out.add(new Move(fromRow, fromCol, toRow, toCol, PieceType.BISHOP));
            out.add(new Move(fromRow, fromCol, toRow, toCol, PieceType.KNIGHT));
        } else {
            out.add(new Move(fromRow, fromCol, toRow, toCol, null));
        }
    }

    private void addKnightMoves(int r, int c, Piece knight, List<Move> out) {
        int[][] deltas = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] d : deltas) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (!Board.inBounds(nr, nc)) continue;
            Piece target = board.get(nr, nc);
            if (target == null || target.color != knight.color) {
                out.add(new Move(r, c, nr, nc, null));
            }
        }
    }

    private void addSlidingMoves(int r, int c, Piece piece, List<Move> out, int[][] directions) {
        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];
            while (Board.inBounds(nr, nc)) {
                Piece target = board.get(nr, nc);
                if (target == null) {
                    out.add(new Move(r, c, nr, nc, null));
                } else {
                    if (target.color != piece.color) {
                        out.add(new Move(r, c, nr, nc, null));
                    }
                    break;
                }
                nr += dir[0];
                nc += dir[1];
            }
        }
    }

    private void addKingMoves(int r, int c, Piece king, List<Move> out) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr;
                int nc = c + dc;
                if (!Board.inBounds(nr, nc)) continue;
                Piece target = board.get(nr, nc);
                if (target == null || target.color != king.color) {
                    out.add(new Move(r, c, nr, nc, null));
                }
            }
        }
    }

    private void addCastlingMoves(int r, int c, Piece king, List<Move> out) {
        if (isInCheck(king.color)) return;

        if (king.color == Color.WHITE) {
            if (!whiteKingMoved && !whiteHRookMoved && board.get(7, 5) == null && board.get(7, 6) == null) {
                if (!isSquareAttacked(7, 5, Color.BLACK) && !isSquareAttacked(7, 6, Color.BLACK)) {
                    Piece rook = board.get(7, 7);
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == Color.WHITE) {
                        out.add(new Move(r, c, 7, 6, null));
                    }
                }
            }
            if (!whiteKingMoved && !whiteARookMoved && board.get(7, 1) == null && board.get(7, 2) == null && board.get(7, 3) == null) {
                if (!isSquareAttacked(7, 2, Color.BLACK) && !isSquareAttacked(7, 3, Color.BLACK)) {
                    Piece rook = board.get(7, 0);
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == Color.WHITE) {
                        out.add(new Move(r, c, 7, 2, null));
                    }
                }
            }
        } else {
            if (!blackKingMoved && !blackHRookMoved && board.get(0, 5) == null && board.get(0, 6) == null) {
                if (!isSquareAttacked(0, 5, Color.WHITE) && !isSquareAttacked(0, 6, Color.WHITE)) {
                    Piece rook = board.get(0, 7);
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == Color.BLACK) {
                        out.add(new Move(r, c, 0, 6, null));
                    }
                }
            }
            if (!blackKingMoved && !blackARookMoved && board.get(0, 1) == null && board.get(0, 2) == null && board.get(0, 3) == null) {
                if (!isSquareAttacked(0, 2, Color.WHITE) && !isSquareAttacked(0, 3, Color.WHITE)) {
                    Piece rook = board.get(0, 0);
                    if (rook != null && rook.type == PieceType.ROOK && rook.color == Color.BLACK) {
                        out.add(new Move(r, c, 0, 2, null));
                    }
                }
            }
        }
    }

    private boolean isInCheck(Color side) {
        int kingRow = -1;
        int kingCol = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(r, c);
                if (p != null && p.type == PieceType.KING && p.color == side) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        if (kingRow == -1) return false;
        return isSquareAttacked(kingRow, kingCol, side.opposite());
    }

    private boolean isSquareAttacked(int row, int col, Color bySide) {
        int pawnDir = bySide == Color.WHITE ? -1 : 1;
        int pawnRow = row - pawnDir;
        for (int dc : new int[]{-1, 1}) {
            int pc = col + dc;
            if (Board.inBounds(pawnRow, pc)) {
                Piece p = board.get(pawnRow, pc);
                if (p != null && p.color == bySide && p.type == PieceType.PAWN) return true;
            }
        }

        int[][] knight = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] d : knight) {
            int nr = row + d[0], nc = col + d[1];
            if (Board.inBounds(nr, nc)) {
                Piece p = board.get(nr, nc);
                if (p != null && p.color == bySide && p.type == PieceType.KNIGHT) return true;
            }
        }

        int[][] diag = {{1,1},{1,-1},{-1,1},{-1,-1}};
        if (rayAttack(row, col, bySide, diag, PieceType.BISHOP, PieceType.QUEEN)) return true;

        int[][] straight = {{1,0},{-1,0},{0,1},{0,-1}};
        if (rayAttack(row, col, bySide, straight, PieceType.ROOK, PieceType.QUEEN)) return true;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr, nc = col + dc;
                if (Board.inBounds(nr, nc)) {
                    Piece p = board.get(nr, nc);
                    if (p != null && p.color == bySide && p.type == PieceType.KING) return true;
                }
            }
        }
        return false;
    }

    private boolean rayAttack(int row, int col, Color bySide, int[][] dirs, PieceType a, PieceType b) {
        for (int[] d : dirs) {
            int nr = row + d[0], nc = col + d[1];
            while (Board.inBounds(nr, nc)) {
                Piece p = board.get(nr, nc);
                if (p != null) {
                    if (p.color == bySide && (p.type == a || p.type == b)) {
                        return true;
                    }
                    break;
                }
                nr += d[0];
                nc += d[1];
            }
        }
        return false;
    }

    private void applyMove(Move move) {
        applyMoveUnchecked(move);
    }

    private void applyMoveUnchecked(Move move) {
        Piece moving = board.get(move.fromRow, move.fromCol);
        Piece captured = board.get(move.toRow, move.toCol);

        boolean enPassantCapture = moving != null && moving.type == PieceType.PAWN && move.toRow == enPassantRow && move.toCol == enPassantCol && captured == null;
        if (enPassantCapture) {
            int capturedPawnRow = moving.color == Color.WHITE ? move.toRow + 1 : move.toRow - 1;
            board.set(capturedPawnRow, move.toCol, null);
        }

        board.set(move.fromRow, move.fromCol, null);
        if (moving != null && moving.type == PieceType.PAWN && (move.toRow == 0 || move.toRow == 7)) {
            PieceType promoted = move.promotion == null ? PieceType.QUEEN : move.promotion;
            board.set(move.toRow, move.toCol, new Piece(promoted, moving.color));
        } else {
            board.set(move.toRow, move.toCol, moving);
        }

        if (moving != null && moving.type == PieceType.KING) {
            if (moving.color == Color.WHITE) whiteKingMoved = true; else blackKingMoved = true;

            if (Math.abs(move.toCol - move.fromCol) == 2) {
                if (move.toCol == 6) {
                    Piece rook = board.get(move.toRow, 7);
                    board.set(move.toRow, 7, null);
                    board.set(move.toRow, 5, rook);
                } else if (move.toCol == 2) {
                    Piece rook = board.get(move.toRow, 0);
                    board.set(move.toRow, 0, null);
                    board.set(move.toRow, 3, rook);
                }
            }
        }

        if (moving != null && moving.type == PieceType.ROOK) {
            if (move.fromRow == 7 && move.fromCol == 0) whiteARookMoved = true;
            if (move.fromRow == 7 && move.fromCol == 7) whiteHRookMoved = true;
            if (move.fromRow == 0 && move.fromCol == 0) blackARookMoved = true;
            if (move.fromRow == 0 && move.fromCol == 7) blackHRookMoved = true;
        }

        if (captured != null && captured.type == PieceType.ROOK) {
            if (move.toRow == 7 && move.toCol == 0) whiteARookMoved = true;
            if (move.toRow == 7 && move.toCol == 7) whiteHRookMoved = true;
            if (move.toRow == 0 && move.toCol == 0) blackARookMoved = true;
            if (move.toRow == 0 && move.toCol == 7) blackHRookMoved = true;
        }

        enPassantRow = -1;
        enPassantCol = -1;
        if (moving != null && moving.type == PieceType.PAWN && Math.abs(move.toRow - move.fromRow) == 2) {
            enPassantRow = (move.fromRow + move.toRow) / 2;
            enPassantCol = move.fromCol;
        }
    }

    private StateSnapshot snapshot() {
        return new StateSnapshot(
            board.copy(),
            turn,
            whiteKingMoved,
            blackKingMoved,
            whiteARookMoved,
            whiteHRookMoved,
            blackARookMoved,
            blackHRookMoved,
            enPassantRow,
            enPassantCol
        );
    }

    private void restore(StateSnapshot s) {
        this.board = s.board;
        this.turn = s.turn;
        this.whiteKingMoved = s.whiteKingMoved;
        this.blackKingMoved = s.blackKingMoved;
        this.whiteARookMoved = s.whiteARookMoved;
        this.whiteHRookMoved = s.whiteHRookMoved;
        this.blackARookMoved = s.blackARookMoved;
        this.blackHRookMoved = s.blackHRookMoved;
        this.enPassantRow = s.enPassantRow;
        this.enPassantCol = s.enPassantCol;
    }

    private record StateSnapshot(
        Board board,
        Color turn,
        boolean whiteKingMoved,
        boolean blackKingMoved,
        boolean whiteARookMoved,
        boolean whiteHRookMoved,
        boolean blackARookMoved,
        boolean blackHRookMoved,
        int enPassantRow,
        int enPassantCol
    ) {}
}
