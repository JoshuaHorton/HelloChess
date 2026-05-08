package HelloChess;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private FastBoard board;
    private List<Integer> capturedWhitePieces = new ArrayList<>();
    private List<Integer> capturedBlackPieces = new ArrayList<>();
    private List<String> history = new ArrayList<>();

    public Game() {
        board = new FastBoard();
    }

    public List<Integer> getCapturedWhitePieces() { return capturedWhitePieces; }
    public List<Integer> getCapturedBlackPieces() { return capturedBlackPieces; }

    public MoveResult processMove(String moveInput) {
        try {
            String[] parts = moveInput.split(" ");
            String from = parts[0];
            String to = parts[1];

            int fromSquare = algebraicToIndex(from);
            int toSquare = algebraicToIndex(to);

            List<Integer> legalMoves = MoveGenerator.generateLegalMoves(board);
            int chosenMove = -1;
            for(int move : legalMoves) {
                int mFrom = move & 0x3F;
                int mTo = (move >> 6) & 0x3F;
                if(mFrom == fromSquare && mTo == toSquare) {
                    chosenMove = move;
                    int promoted = (move >> 20) & 0xF;
                    if(promoted != 0) {
                        if(Math.abs(promoted) == FastBoard.W_QUEEN) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }

            if(chosenMove != -1) {
                int captured = (chosenMove >> 16) & 0xF;
                if ((captured & 8) != 0) captured |= 0xFFFFFFF0;

                if (captured != 0) {
                    if (captured > 0) capturedWhitePieces.add(captured);
                    else capturedBlackPieces.add(captured);
                }

                board.executeMove(chosenMove);
                history.add(moveInput);
                return MoveResult.valid(captured, moveInput);
            } else {
                return MoveResult.invalid("Illegal move pattern or blocked");
            }

        } catch (Exception e) {
            return MoveResult.invalid("Error processing move: " + e.getMessage());
        }
    }

    private int algebraicToIndex(String alg) {
        int file = alg.charAt(0) - 'a';
        int rank = alg.charAt(1) - '1';
        return rank * 8 + file;
    }

    public void printPositionHistory() {
        System.out.println("History of moves: " + history);
    }
}