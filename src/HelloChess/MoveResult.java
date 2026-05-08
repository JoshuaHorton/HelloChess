package HelloChess;

public record MoveResult(boolean isValid, int capturedPiece, String algebraicMove, String message) {
    
    public static MoveResult invalid(String message) {
        return new MoveResult(false, 0, "", message);
    }
    
    public static MoveResult valid(int capturedPiece, String algebraicMove) {
        return new MoveResult(true, capturedPiece, algebraicMove, "");
    }
}
