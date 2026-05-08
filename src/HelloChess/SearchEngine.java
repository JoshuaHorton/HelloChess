package HelloChess;

import java.util.List;

public class SearchEngine {
    
    private static final int MATE_SCORE = 100000;
    
    private static final int PAWN_VAL = 100;
    private static final int KNIGHT_VAL = 320;
    private static final int BISHOP_VAL = 330;
    private static final int ROOK_VAL = 500;
    private static final int QUEEN_VAL = 900;
    
    private static final int[] CENTER_BONUS = {
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 5, 5, 5, 5, 5, 5, 0,
        0, 5,10,10,10,10, 5, 0,
        0, 5,10,20,20,10, 5, 0,
        0, 5,10,20,20,10, 5, 0,
        0, 5,10,10,10,10, 5, 0,
        0, 5, 5, 5, 5, 5, 5, 0,
        0, 0, 0, 0, 0, 0, 0, 0
    };

    public static int evaluate(FastBoard board) {
        int score = 0;
        for (int i = 0; i < 64; i++) {
            int piece = board.getPiece(i);
            if (piece == FastBoard.EMPTY) continue;
            
            int abs = Math.abs(piece);
            int sign = piece > 0 ? 1 : -1;
            int val = 0;
            switch(abs) {
                case FastBoard.W_PAWN: val = PAWN_VAL; break;
                case FastBoard.W_KNIGHT: val = KNIGHT_VAL + CENTER_BONUS[i]; break;
                case FastBoard.W_BISHOP: val = BISHOP_VAL + CENTER_BONUS[i]; break;
                case FastBoard.W_ROOK: val = ROOK_VAL; break;
                case FastBoard.W_QUEEN: val = QUEEN_VAL; break;
            }
            score += sign * val;
        }
        return board.isWhiteTurn ? score : -score;
    }
    
    public static int search(FastBoard board, int depth, int alpha, int beta) {
        if (depth == 0) {
            return evaluate(board);
        }
        
        List<Integer> moves = MoveGenerator.generateLegalMoves(board);
        if (moves.isEmpty()) {
            if (MoveGenerator.isSquareAttacked(board, getKingSquare(board, board.isWhiteTurn), !board.isWhiteTurn)) {
                return -(MATE_SCORE + depth);
            }
            return 0;
        }
        
        for (int move : moves) {
            FastBoard temp = new FastBoard(board);
            temp.executeMove(move);
            int score = -search(temp, depth - 1, -beta, -alpha);
            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }
        return alpha;
    }
    
    private static int getKingSquare(FastBoard board, boolean isWhite) {
        int king = isWhite ? FastBoard.W_KING : FastBoard.B_KING;
        for (int i = 0; i < 64; i++) {
            if (board.board[i] == king) return i;
        }
        return -1;
    }
}
