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
    
    private static long nodesCount;

    public interface SearchListener {
        void onSearchStarted(List<Integer> rootMoves);
        void onDepthStarted(int depth);
        void onCandidateMoveStarted(int move, int index, int total);
        void onCandidateMoveCompleted(int move, int score);
        void onDepthCompleted(int depth, int bestMove, int score, long nodesSearched, long timeMs);
        void onSearchCompleted(int bestMove, int score, long nodesSearched, long timeMs);
    }

    public static int findBestMove(FastBoard board, int maxDepth, SearchListener listener, int throttleMs) {
        List<Integer> rootMoves = MoveGenerator.generateLegalMoves(board);
        if (rootMoves.isEmpty()) return -1;
        
        if (listener != null) {
            listener.onSearchStarted(rootMoves);
        }
        
        sortMoves(board, rootMoves);
        
        int overallBestMove = rootMoves.get(0);
        int overallBestScore = -Integer.MAX_VALUE;
        long totalNodes = 0;
        long startTime = System.currentTimeMillis();
        
        for (int depth = 1; depth <= maxDepth; depth++) {
            if (Thread.currentThread().isInterrupted()) break;
            
            if (listener != null) {
                listener.onDepthStarted(depth);
            }
            
            int bestMoveForDepth = -1;
            int bestScoreForDepth = -Integer.MAX_VALUE;
            
            int alpha = -Integer.MAX_VALUE;
            int beta = Integer.MAX_VALUE;
            
            for (int i = 0; i < rootMoves.size(); i++) {
                if (Thread.currentThread().isInterrupted()) break;
                
                int move = rootMoves.get(i);
                if (listener != null) {
                    listener.onCandidateMoveStarted(move, i, rootMoves.size());
                }
                
                nodesCount = 0;
                FastBoard temp = new FastBoard(board);
                temp.executeMove(move);
                
                int score = -search(temp, depth - 1, -beta, -alpha);
                totalNodes += nodesCount;
                
                if (score > bestScoreForDepth) {
                    bestScoreForDepth = score;
                    bestMoveForDepth = move;
                }
                
                if (score > alpha) {
                    alpha = score;
                }
                
                if (listener != null) {
                    listener.onCandidateMoveCompleted(move, score);
                }
                
                if (throttleMs > 0) {
                    try {
                        Thread.sleep(throttleMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            if (!Thread.currentThread().isInterrupted() && bestMoveForDepth != -1) {
                overallBestMove = bestMoveForDepth;
                overallBestScore = bestScoreForDepth;
                
                // Move ordering optimization: put the best move of the current depth at the start of the list
                rootMoves.remove((Integer) overallBestMove);
                rootMoves.add(0, overallBestMove);
                
                if (listener != null) {
                    listener.onDepthCompleted(depth, overallBestMove, overallBestScore, totalNodes, System.currentTimeMillis() - startTime);
                }
            }
        }
        
        if (listener != null) {
            listener.onSearchCompleted(overallBestMove, overallBestScore, totalNodes, System.currentTimeMillis() - startTime);
        }
        
        return overallBestMove;
    }

    private static void sortMoves(FastBoard board, List<Integer> moves) {
        moves.sort((a, b) -> {
            int scoreA = scoreMove(board, a);
            int scoreB = scoreMove(board, b);
            return Integer.compare(scoreB, scoreA);
        });
    }

    private static int scoreMove(FastBoard board, int move) {
        int captured = (move >> 16) & 0xF;
        if ((captured & 8) != 0) captured |= 0xFFFFFFF0;
        
        int promoted = (move >> 20) & 0xF;
        if ((promoted & 8) != 0) promoted |= 0xFFFFFFF0;
        
        int score = 0;
        if (captured != 0) {
            score += 1000 * Math.abs(captured);
            int piece = (move >> 12) & 0xF;
            if ((piece & 8) != 0) piece |= 0xFFFFFFF0;
            score -= Math.abs(piece);
        }
        if (promoted != 0) {
            score += 900;
        }
        return score;
    }

    public static int search(FastBoard board, int depth, int alpha, int beta) {
        nodesCount++;
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
