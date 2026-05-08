package HelloChess;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

public class PuzzleMiner {

    private static final int MATE_THRESHOLD = 90000;
    
    public static void minePuzzles(int count) {
        int found = 0;
        Random rand = new Random();
        try (PrintWriter out = new PrintWriter(new FileWriter("C:\\Users\\jcarl\\Projects\\GitHub\\HelloChess\\HelloChess\\puzzles.txt", true))) {
            while (found < count) {
                FastBoard board = new FastBoard();
                boolean isMateIn3 = false;
                
                for (int ply = 0; ply < 100; ply++) {
                    List<Integer> moves = MoveGenerator.generateLegalMoves(board);
                    if (moves.isEmpty()) break;
                    
                    int maxScore = -Integer.MAX_VALUE;
                    
                    if (ply > 10) {
                        for (int m : moves) {
                            FastBoard temp = new FastBoard(board);
                            temp.executeMove(m);
                            // search 4 plies deep to find mate in 3 (current move is 1st ply, so total 5 plies)
                            int score = -SearchEngine.search(temp, 4, -1000000, 1000000);
                            if (score > maxScore) {
                                maxScore = score;
                            }
                        }
                        
                        if (maxScore > MATE_THRESHOLD) {
                            String fen = FenParser.getFEN(board);
                            System.out.println("Found mate puzzle! " + fen);
                            out.println(fen);
                            out.flush();
                            found++;
                            isMateIn3 = true;
                            break;
                        }
                    }
                    
                    moves.sort((a, b) -> {
                        FastBoard tA = new FastBoard(board); tA.executeMove(a);
                        FastBoard tB = new FastBoard(board); tB.executeMove(b);
                        int scoreA = -SearchEngine.evaluate(tA);
                        int scoreB = -SearchEngine.evaluate(tB);
                        return Integer.compare(scoreB, scoreA);
                    });
                    
                    int pick = rand.nextInt(Math.min(3, moves.size()));
                    board.executeMove(moves.get(pick));
                }
                
                if (!isMateIn3) {
                    System.out.println("Game finished without puzzle, retrying...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Mining 2 puzzles...");
        minePuzzles(2);
        System.out.println("Done.");
    }
}
