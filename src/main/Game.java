package main;

import board.*;
import pieces.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Game {
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

    public void run() {
        setupInitialPosition();

        @SuppressWarnings("resource")
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
            if (line == null)
                continue;

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
            board.set(1, c, new Pawn(Color.BLACK));
            board.set(6, c, new Pawn(Color.WHITE));
        }

        board.set(0, 0, new Rook(Color.BLACK));
        board.set(0, 7, new Rook(Color.BLACK));
        board.set(7, 0, new Rook(Color.WHITE));
        board.set(7, 7, new Rook(Color.WHITE));

        board.set(0, 1, new Knight(Color.BLACK));
        board.set(0, 6, new Knight(Color.BLACK));
        board.set(7, 1, new Knight(Color.WHITE));
        board.set(7, 6, new Knight(Color.WHITE));

        board.set(0, 2, new Bishop(Color.BLACK));
        board.set(0, 5, new Bishop(Color.BLACK));
        board.set(7, 2, new Bishop(Color.WHITE));
        board.set(7, 5, new Bishop(Color.WHITE));

        board.set(0, 3, new Queen(Color.BLACK));
        board.set(7, 3, new Queen(Color.WHITE));

        board.set(0, 4, new King(Color.BLACK));
        board.set(7, 4, new King(Color.WHITE));
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
                if (p == null || p.color != side)
                    continue;
                p.addPseudoLegalMoves(board, r, c, moves, this);
            }
        }
        return moves;
    }

    public boolean isInCheck(Color side) {
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
            if (kingRow != -1)
                break;
        }
        if (kingRow == -1)
            return false;
        return isSquareAttacked(kingRow, kingCol, side.opposite());
    }

    public boolean isSquareAttacked(int row, int col, Color bySide) {
        int pawnDir = bySide == Color.WHITE ? -1 : 1;
        int pawnRow = row - pawnDir;
        for (int dc : new int[] { -1, 1 }) {
            int pc = col + dc;
            if (Board.inBounds(pawnRow, pc)) {
                Piece p = board.get(pawnRow, pc);
                if (p != null && p.color == bySide && p.type == PieceType.PAWN)
                    return true;
            }
        }

        int[][] knight = { { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 }, { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 } };
        for (int[] d : knight) {
            int nr = row + d[0], nc = col + d[1];
            if (Board.inBounds(nr, nc)) {
                Piece p = board.get(nr, nc);
                if (p != null && p.color == bySide && p.type == PieceType.KNIGHT)
                    return true;
            }
        }

        int[][] diag = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        if (rayAttack(row, col, bySide, diag, PieceType.BISHOP, PieceType.QUEEN))
            return true;

        int[][] straight = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        if (rayAttack(row, col, bySide, straight, PieceType.ROOK, PieceType.QUEEN))
            return true;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0)
                    continue;
                int nr = row + dr, nc = col + dc;
                if (Board.inBounds(nr, nc)) {
                    Piece p = board.get(nr, nc);
                    if (p != null && p.color == bySide && p.type == PieceType.KING)
                        return true;
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

        boolean enPassantCapture = moving != null && moving.type == PieceType.PAWN && move.toRow == enPassantRow
                && move.toCol == enPassantCol && captured == null;
        if (enPassantCapture) {
            int capturedPawnRow = moving.color == Color.WHITE ? move.toRow + 1 : move.toRow - 1;
            board.set(capturedPawnRow, move.toCol, null);
        }

        board.set(move.fromRow, move.fromCol, null);
        if (moving != null && moving.type == PieceType.PAWN && (move.toRow == 0 || move.toRow == 7)) {
            PieceType promoted = move.promotion == null ? PieceType.QUEEN : move.promotion;
            Piece promotedPiece;
            switch (promoted) {
                case ROOK:
                    promotedPiece = new Rook(moving.color);
                    break;
                case BISHOP:
                    promotedPiece = new Bishop(moving.color);
                    break;
                case KNIGHT:
                    promotedPiece = new Knight(moving.color);
                    break;
                case QUEEN:
                default:
                    promotedPiece = new Queen(moving.color);
                    break;
            }
            board.set(move.toRow, move.toCol, promotedPiece);
        } else {
            board.set(move.toRow, move.toCol, moving);
        }

        if (moving != null && moving.type == PieceType.KING) {
            if (moving.color == Color.WHITE)
                whiteKingMoved = true;
            else
                blackKingMoved = true;

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
            if (move.fromRow == 7 && move.fromCol == 0)
                whiteARookMoved = true;
            if (move.fromRow == 7 && move.fromCol == 7)
                whiteHRookMoved = true;
            if (move.fromRow == 0 && move.fromCol == 0)
                blackARookMoved = true;
            if (move.fromRow == 0 && move.fromCol == 7)
                blackHRookMoved = true;
        }

        if (captured != null && captured.type == PieceType.ROOK) {
            if (move.toRow == 7 && move.toCol == 0)
                whiteARookMoved = true;
            if (move.toRow == 7 && move.toCol == 7)
                whiteHRookMoved = true;
            if (move.toRow == 0 && move.toCol == 0)
                blackARookMoved = true;
            if (move.toRow == 0 && move.toCol == 7)
                blackHRookMoved = true;
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
                enPassantCol);
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

    private static class StateSnapshot {
        final Board board;
        final Color turn;
        final boolean whiteKingMoved;
        final boolean blackKingMoved;
        final boolean whiteARookMoved;
        final boolean whiteHRookMoved;
        final boolean blackARookMoved;
        final boolean blackHRookMoved;
        final int enPassantRow;
        final int enPassantCol;

        StateSnapshot(Board board, Color turn, boolean whiteKingMoved, boolean blackKingMoved,
                boolean whiteARookMoved, boolean whiteHRookMoved, boolean blackARookMoved,
                boolean blackHRookMoved, int enPassantRow, int enPassantCol) {
            this.board = board;
            this.turn = turn;
            this.whiteKingMoved = whiteKingMoved;
            this.blackKingMoved = blackKingMoved;
            this.whiteARookMoved = whiteARookMoved;
            this.whiteHRookMoved = whiteHRookMoved;
            this.blackARookMoved = blackARookMoved;
            this.blackHRookMoved = blackHRookMoved;
            this.enPassantRow = enPassantRow;
            this.enPassantCol = enPassantCol;
        }
    }

    public int getEnPassantRow() {
        return enPassantRow;
    }

    public int getEnPassantCol() {
        return enPassantCol;
    }

    public boolean isWhiteKingMoved() {
        return whiteKingMoved;
    }

    public boolean isBlackKingMoved() {
        return blackKingMoved;
    }

    public boolean isWhiteARookMoved() {
        return whiteARookMoved;
    }

    public boolean isWhiteHRookMoved() {
        return whiteHRookMoved;
    }

    public boolean isBlackARookMoved() {
        return blackARookMoved;
    }

    public boolean isBlackHRookMoved() {
        return blackHRookMoved;
    }
}
