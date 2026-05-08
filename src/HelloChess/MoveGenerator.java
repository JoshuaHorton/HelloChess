package HelloChess;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    
    // Flags: 0=normal, 1=double push, 2=en passant, 3=kingside castle, 4=queenside castle
    public static int encodeMove(int from, int to, int piece, int captured, int promoted, int flags) {
        return (from & 0x3F) | 
               ((to & 0x3F) << 6) | 
               ((piece & 0xF) << 12) | 
               ((captured & 0xF) << 16) | 
               ((promoted & 0xF) << 20) | 
               ((flags & 0xF) << 24);
    }
    
    public static List<Integer> generateLegalMoves(FastBoard board) {
        List<Integer> pseudoMoves = generatePseudoLegalMoves(board);
        List<Integer> legalMoves = new ArrayList<>();
        
        for (int move : pseudoMoves) {
            FastBoard temp = new FastBoard(board);
            temp.executeMove(move);
            if (!isKingInCheck(temp, board.isWhiteTurn)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }
    
    private static boolean isKingInCheck(FastBoard board, boolean isWhiteKing) {
        int king = isWhiteKing ? FastBoard.W_KING : FastBoard.B_KING;
        int kingPos = -1;
        for (int i = 0; i < 64; i++) {
            if (board.board[i] == king) {
                kingPos = i;
                break;
            }
        }
        if (kingPos == -1) return false;
        return isSquareAttacked(board, kingPos, !isWhiteKing);
    }
    
    public static boolean isSquareAttacked(FastBoard board, int square, boolean byWhite) {
        int sqRank = square / 8;
        int sqFile = square % 8;
        
        if (byWhite) {
            if (sqRank > 0 && sqFile > 0 && board.board[square - 9] == FastBoard.W_PAWN) return true;
            if (sqRank > 0 && sqFile < 7 && board.board[square - 7] == FastBoard.W_PAWN) return true;
        } else {
            if (sqRank < 7 && sqFile > 0 && board.board[square + 7] == FastBoard.B_PAWN) return true;
            if (sqRank < 7 && sqFile < 7 && board.board[square + 9] == FastBoard.B_PAWN) return true;
        }
        
        int[] knightOffsets = {-17, -15, -10, -6, 6, 10, 15, 17};
        int knight = byWhite ? FastBoard.W_KNIGHT : FastBoard.B_KNIGHT;
        for (int offset : knightOffsets) {
            int target = square + offset;
            if (target >= 0 && target < 64) {
                int tRank = target / 8, tFile = target % 8;
                if (Math.abs(tRank - sqRank) <= 2 && Math.abs(tFile - sqFile) <= 2) {
                    if (board.board[target] == knight) return true;
                }
            }
        }
        
        int[] kingOffsets = {-9, -8, -7, -1, 1, 7, 8, 9};
        int enemyKing = byWhite ? FastBoard.W_KING : FastBoard.B_KING;
        for (int offset : kingOffsets) {
            int target = square + offset;
            if (target >= 0 && target < 64) {
                int tRank = target / 8, tFile = target % 8;
                if (Math.abs(tRank - sqRank) <= 1 && Math.abs(tFile - sqFile) <= 1) {
                    if (board.board[target] == enemyKing) return true;
                }
            }
        }
        
        int rook = byWhite ? FastBoard.W_ROOK : FastBoard.B_ROOK;
        int bishop = byWhite ? FastBoard.W_BISHOP : FastBoard.B_BISHOP;
        int queen = byWhite ? FastBoard.W_QUEEN : FastBoard.B_QUEEN;
        
        int[] straightDirs = {-8, 8, -1, 1};
        for (int dir : straightDirs) {
            int current = square;
            while (true) {
                int next = current + dir;
                if (next < 0 || next >= 64) break;
                if (Math.abs(next / 8 - current / 8) > 1 || Math.abs(next % 8 - current % 8) > 1) break;
                current = next;
                int p = board.board[current];
                if (p != FastBoard.EMPTY) {
                    if (p == rook || p == queen) return true;
                    break;
                }
            }
        }
        
        int[] diagDirs = {-9, -7, 7, 9};
        for (int dir : diagDirs) {
            int current = square;
            while (true) {
                int next = current + dir;
                if (next < 0 || next >= 64) break;
                if (Math.abs(next / 8 - current / 8) > 1 || Math.abs(next % 8 - current % 8) > 1) break;
                current = next;
                int p = board.board[current];
                if (p != FastBoard.EMPTY) {
                    if (p == bishop || p == queen) return true;
                    break;
                }
            }
        }
        
        return false;
    }
    
    public static List<Integer> generatePseudoLegalMoves(FastBoard board) {
        List<Integer> moves = new ArrayList<>();
        boolean isWhite = board.isWhiteTurn;
        
        for (int i = 0; i < 64; i++) {
            int piece = board.board[i];
            if (piece == FastBoard.EMPTY) continue;
            if ((piece > 0) != isWhite) continue;
            
            int type = Math.abs(piece);
            if (type == FastBoard.W_PAWN) generatePawnMoves(board, i, isWhite, moves);
            else if (type == FastBoard.W_KNIGHT) generateKnightMoves(board, i, piece, moves);
            else if (type == FastBoard.W_BISHOP) generateSlidingMoves(board, i, piece, new int[]{-9, -7, 7, 9}, moves);
            else if (type == FastBoard.W_ROOK) generateSlidingMoves(board, i, piece, new int[]{-8, -1, 1, 8}, moves);
            else if (type == FastBoard.W_QUEEN) generateSlidingMoves(board, i, piece, new int[]{-9, -8, -7, -1, 1, 7, 8, 9}, moves);
            else if (type == FastBoard.W_KING) generateKingMoves(board, i, piece, moves);
        }
        
        return moves;
    }

    private static void addPawnMove(int from, int to, int piece, int captured, int flags, List<Integer> moves) {
        int rank = to / 8;
        if (rank == 0 || rank == 7) {
            int color = piece > 0 ? 1 : -1;
            moves.add(encodeMove(from, to, piece, captured, color * FastBoard.W_QUEEN, flags));
            moves.add(encodeMove(from, to, piece, captured, color * FastBoard.W_ROOK, flags));
            moves.add(encodeMove(from, to, piece, captured, color * FastBoard.W_BISHOP, flags));
            moves.add(encodeMove(from, to, piece, captured, color * FastBoard.W_KNIGHT, flags));
        } else {
            moves.add(encodeMove(from, to, piece, captured, FastBoard.EMPTY, flags));
        }
    }

    private static void generatePawnMoves(FastBoard board, int from, boolean isWhite, List<Integer> moves) {
        int dir = isWhite ? 8 : -8;
        int startRank = isWhite ? 1 : 6;
        int piece = board.board[from];
        
        int to = from + dir;
        if (to >= 0 && to < 64 && board.board[to] == FastBoard.EMPTY) {
            addPawnMove(from, to, piece, FastBoard.EMPTY, 0, moves);
            
            if (from / 8 == startRank) {
                int to2 = from + 2 * dir;
                if (board.board[to2] == FastBoard.EMPTY) {
                    moves.add(encodeMove(from, to2, piece, FastBoard.EMPTY, FastBoard.EMPTY, 1));
                }
            }
        }
        
        int[] capOffsets = isWhite ? new int[]{7, 9} : new int[]{-9, -7};
        for (int offset : capOffsets) {
            int capTo = from + offset;
            if (capTo >= 0 && capTo < 64) {
                if (Math.abs(capTo % 8 - from % 8) != 1) continue;
                
                int target = board.board[capTo];
                if (target != FastBoard.EMPTY && (target > 0) != isWhite) {
                    addPawnMove(from, capTo, piece, target, 0, moves);
                } else if (capTo == board.enPassantTarget) {
                    int epCapPiece = isWhite ? FastBoard.B_PAWN : FastBoard.W_PAWN;
                    moves.add(encodeMove(from, capTo, piece, epCapPiece, FastBoard.EMPTY, 2));
                }
            }
        }
    }

    private static void generateKnightMoves(FastBoard board, int from, int piece, List<Integer> moves) {
        int[] offsets = {-17, -15, -10, -6, 6, 10, 15, 17};
        for (int offset : offsets) {
            int to = from + offset;
            if (to >= 0 && to < 64) {
                if (Math.abs(to / 8 - from / 8) <= 2 && Math.abs(to % 8 - from % 8) <= 2) {
                    int target = board.board[to];
                    if (target == FastBoard.EMPTY || (target > 0) != (piece > 0)) {
                        moves.add(encodeMove(from, to, piece, target, FastBoard.EMPTY, 0));
                    }
                }
            }
        }
    }

    private static void generateSlidingMoves(FastBoard board, int from, int piece, int[] dirs, List<Integer> moves) {
        for (int dir : dirs) {
            int current = from;
            while (true) {
                int next = current + dir;
                if (next < 0 || next >= 64) break;
                if (Math.abs(next / 8 - current / 8) > 1 || Math.abs(next % 8 - current % 8) > 1) break;
                current = next;
                
                int target = board.board[current];
                if (target == FastBoard.EMPTY) {
                    moves.add(encodeMove(from, current, piece, FastBoard.EMPTY, FastBoard.EMPTY, 0));
                } else {
                    if ((target > 0) != (piece > 0)) {
                        moves.add(encodeMove(from, current, piece, target, FastBoard.EMPTY, 0));
                    }
                    break;
                }
            }
        }
    }

    private static void generateKingMoves(FastBoard board, int from, int piece, List<Integer> moves) {
        int[] offsets = {-9, -8, -7, -1, 1, 7, 8, 9};
        for (int offset : offsets) {
            int to = from + offset;
            if (to >= 0 && to < 64) {
                if (Math.abs(to / 8 - from / 8) <= 1 && Math.abs(to % 8 - from % 8) <= 1) {
                    int target = board.board[to];
                    if (target == FastBoard.EMPTY || (target > 0) != (piece > 0)) {
                        moves.add(encodeMove(from, to, piece, target, FastBoard.EMPTY, 0));
                    }
                }
            }
        }
        
        boolean isWhite = piece > 0;
        if (isWhite) {
            if (from == 4) {
                if ((board.castlingRights & 1) != 0) {
                    if (board.board[5] == FastBoard.EMPTY && board.board[6] == FastBoard.EMPTY) {
                        if (!isSquareAttacked(board, 4, false) && !isSquareAttacked(board, 5, false) && !isSquareAttacked(board, 6, false)) {
                            moves.add(encodeMove(4, 6, piece, FastBoard.EMPTY, FastBoard.EMPTY, 3));
                        }
                    }
                }
                if ((board.castlingRights & 2) != 0) {
                    if (board.board[3] == FastBoard.EMPTY && board.board[2] == FastBoard.EMPTY && board.board[1] == FastBoard.EMPTY) {
                        if (!isSquareAttacked(board, 4, false) && !isSquareAttacked(board, 3, false) && !isSquareAttacked(board, 2, false)) {
                            moves.add(encodeMove(4, 2, piece, FastBoard.EMPTY, FastBoard.EMPTY, 4));
                        }
                    }
                }
            }
        } else {
            if (from == 60) {
                if ((board.castlingRights & 4) != 0) {
                    if (board.board[61] == FastBoard.EMPTY && board.board[62] == FastBoard.EMPTY) {
                        if (!isSquareAttacked(board, 60, true) && !isSquareAttacked(board, 61, true) && !isSquareAttacked(board, 62, true)) {
                            moves.add(encodeMove(60, 62, piece, FastBoard.EMPTY, FastBoard.EMPTY, 3));
                        }
                    }
                }
                if ((board.castlingRights & 8) != 0) {
                    if (board.board[59] == FastBoard.EMPTY && board.board[58] == FastBoard.EMPTY && board.board[57] == FastBoard.EMPTY) {
                        if (!isSquareAttacked(board, 60, true) && !isSquareAttacked(board, 59, true) && !isSquareAttacked(board, 58, true)) {
                            moves.add(encodeMove(60, 58, piece, FastBoard.EMPTY, FastBoard.EMPTY, 4));
                        }
                    }
                }
            }
        }
    }
}
