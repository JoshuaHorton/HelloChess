package HelloChess;

public record MoveResult(boolean isValid, Piece capturedPiece, String algebraicMove, String message) {
    
    public static MoveResult invalid(String message) {
        return new MoveResult(false, null, "", message);
    }
    
    public static MoveResult valid(Piece capturedPiece, String algebraicMove) {
        return new MoveResult(true, capturedPiece, algebraicMove, "");
    }
}
