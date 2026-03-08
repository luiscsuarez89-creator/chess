package com.mycompany.pieces;

public enum PieceType {
    KING('K'),
    QUEEN('Q'),
    ROOK('R'),
    BISHOP('B'),
    KNIGHT('N'),
    PAWN('P');

    public final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }
}
