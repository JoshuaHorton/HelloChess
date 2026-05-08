package HelloChess;

public class FenParser {

    public static String getFEN(FastBoard board) {
        StringBuilder fen = new StringBuilder();
        // Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                int piece = board.getPiece(rank * 8 + file);
                if (piece == FastBoard.EMPTY) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceToChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (rank > 0) fen.append('/');
        }
        
        // Active color
        fen.append(board.isWhiteTurn ? " w " : " b ");
        
        // Castling
        boolean anyCastle = false;
        if ((board.castlingRights & 1) != 0) { fen.append('K'); anyCastle = true; }
        if ((board.castlingRights & 2) != 0) { fen.append('Q'); anyCastle = true; }
        if ((board.castlingRights & 4) != 0) { fen.append('k'); anyCastle = true; }
        if ((board.castlingRights & 8) != 0) { fen.append('q'); anyCastle = true; }
        if (!anyCastle) fen.append('-');
        
        // En Passant
        fen.append(' ');
        if (board.enPassantTarget == -1) {
            fen.append('-');
        } else {
            char file = (char)('a' + (board.enPassantTarget % 8));
            int r = (board.enPassantTarget / 8) + 1;
            fen.append(file).append(r);
        }
        
        // Halfmove / Fullmove
        fen.append(" ").append(board.halfMoveClock).append(" ").append(board.fullMoveNumber);
        
        return fen.toString();
    }
    
    public static void loadFEN(FastBoard board, String fen) {
        String[] parts = fen.trim().split(" ");
        if (parts.length < 1) return;
        
        // Clear board
        for (int i = 0; i < 64; i++) board.board[i] = FastBoard.EMPTY;
        
        String placement = parts[0];
        int rank = 7, file = 0;
        for (int i = 0; i < placement.length(); i++) {
            char c = placement.charAt(i);
            if (c == '/') {
                rank--;
                file = 0;
            } else if (Character.isDigit(c)) {
                file += c - '0';
            } else {
                board.board[rank * 8 + file] = charToPiece(c);
                file++;
            }
        }
        
        if (parts.length > 1) board.isWhiteTurn = parts[1].equals("w");
        
        if (parts.length > 2) {
            board.castlingRights = 0;
            String castling = parts[2];
            if (castling.contains("K")) board.castlingRights |= 1;
            if (castling.contains("Q")) board.castlingRights |= 2;
            if (castling.contains("k")) board.castlingRights |= 4;
            if (castling.contains("q")) board.castlingRights |= 8;
        }
        
        if (parts.length > 3) {
            String ep = parts[3];
            if (ep.equals("-")) {
                board.enPassantTarget = -1;
            } else {
                int epFile = ep.charAt(0) - 'a';
                int epRank = ep.charAt(1) - '1';
                board.enPassantTarget = epRank * 8 + epFile;
            }
        }
        
        if (parts.length > 4) board.halfMoveClock = Integer.parseInt(parts[4]);
        if (parts.length > 5) board.fullMoveNumber = Integer.parseInt(parts[5]);
    }

    private static char pieceToChar(int piece) {
        int abs = Math.abs(piece);
        char base = '?';
        switch (abs) {
            case FastBoard.W_PAWN: base = 'p'; break;
            case FastBoard.W_KNIGHT: base = 'n'; break;
            case FastBoard.W_BISHOP: base = 'b'; break;
            case FastBoard.W_ROOK: base = 'r'; break;
            case FastBoard.W_QUEEN: base = 'q'; break;
            case FastBoard.W_KING: base = 'k'; break;
        }
        return piece > 0 ? Character.toUpperCase(base) : base;
    }

    private static int charToPiece(char c) {
        int color = Character.isUpperCase(c) ? 1 : -1;
        char l = Character.toLowerCase(c);
        return switch (l) {
            case 'p' -> color * FastBoard.W_PAWN;
            case 'n' -> color * FastBoard.W_KNIGHT;
            case 'b' -> color * FastBoard.W_BISHOP;
            case 'r' -> color * FastBoard.W_ROOK;
            case 'q' -> color * FastBoard.W_QUEEN;
            case 'k' -> color * FastBoard.W_KING;
            default -> FastBoard.EMPTY;
        };
    }
}
