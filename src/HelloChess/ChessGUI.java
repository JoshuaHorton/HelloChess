package HelloChess;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Enhanced Chess GUI for Java 22 / Eclipse 2026-03
 * Supports Drag-and-Drop and Turn-based play with Premium UI and AI Deliberation.
 */
public class ChessGUI extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final java.util.List<String> history = new java.util.ArrayList<>();
	private int moveCount = 1;
	private JTextArea historyTextArea = new JTextArea(20, 15);
	private JLabel capturedWhiteLabel = new JLabel(" Captured by Black: ");
	private JLabel capturedBlackLabel = new JLabel(" Captured by White: ");

    private final JLabel statusLabel = new JLabel("White's Turn");
    
    private final Game backend;
    private BoardPanel boardPanel;

    // AI Configuration and Deliberation Console Fields
    private JCheckBox aiWhiteCheckbox;
    private JCheckBox aiBlackCheckbox;
    private JComboBox<String> difficultyCombo;
    
    private JLabel aiStatusLabel;
    private JLabel aiDepthLabel;
    private JLabel aiNodesLabel;
    private JLabel aiSpeedLabel;
    private JLabel aiScoreLabel;
    private JLabel aiBestMoveLabel;
    private JProgressBar aiProgressBar;
    
    private SearchWorker activeWorker = null;

    public ChessGUI(Game backend) {
        this.backend = backend;
        setupWindow();
        addControlPanel();
        initializeBoard();
        setVisible(true);
        checkAIMove();
    }

    private void setupWindow() {
        setTitle("Chess Prototype - Premium Fast UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.SOUTH);
        
        // Setup Sidebar Panel
        historyTextArea.setEditable(false);
        historyTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Move History"));
        scrollPane.setPreferredSize(new Dimension(260, 300));
        
        // Setup AI Brain Deliberations Panel
        JPanel aiPanel = new JPanel(new GridBagLayout());
        aiPanel.setBorder(BorderFactory.createTitledBorder("AI Brain Deliberations"));
        aiPanel.setPreferredSize(new Dimension(260, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.weightx = 1.0;
        
        JLabel statusTitle = new JLabel("Status:");
        statusTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0;
        aiPanel.add(statusTitle, gbc);
        
        aiStatusLabel = new JLabel("Idle");
        aiStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 0;
        aiPanel.add(aiStatusLabel, gbc);
        
        JLabel depthTitle = new JLabel("Search Depth:");
        depthTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 1;
        aiPanel.add(depthTitle, gbc);
        
        aiDepthLabel = new JLabel("-");
        aiDepthLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 1;
        aiPanel.add(aiDepthLabel, gbc);
        
        JLabel nodesTitle = new JLabel("Nodes Visited:");
        nodesTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 2;
        aiPanel.add(nodesTitle, gbc);
        
        aiNodesLabel = new JLabel("0");
        aiNodesLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 2;
        aiPanel.add(aiNodesLabel, gbc);
        
        JLabel speedTitle = new JLabel("Speed (NPS):");
        speedTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 3;
        aiPanel.add(speedTitle, gbc);
        
        aiSpeedLabel = new JLabel("0");
        aiSpeedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 3;
        aiPanel.add(aiSpeedLabel, gbc);
        
        JLabel scoreTitle = new JLabel("Evaluation:");
        scoreTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 4;
        aiPanel.add(scoreTitle, gbc);
        
        aiScoreLabel = new JLabel("0.00");
        aiScoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 4;
        aiPanel.add(aiScoreLabel, gbc);
        
        JLabel bestTitle = new JLabel("Best Move:");
        bestTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 5;
        aiPanel.add(bestTitle, gbc);
        
        aiBestMoveLabel = new JLabel("-");
        aiBestMoveLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 5;
        aiPanel.add(aiBestMoveLabel, gbc);
        
        aiProgressBar = new JProgressBar(0, 100);
        aiProgressBar.setStringPainted(true);
        aiProgressBar.setString("Ready");
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        aiPanel.add(aiProgressBar, gbc);
        
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.add(scrollPane);
        sidebar.add(aiPanel);
        add(sidebar, BorderLayout.EAST);
        
        JPanel capturedPanel = new JPanel(new GridLayout(2, 1));
        capturedPanel.setBorder(BorderFactory.createTitledBorder("Captured Pieces"));
        capturedWhiteLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        capturedBlackLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        capturedPanel.add(capturedWhiteLabel);
        capturedPanel.add(capturedBlackLabel);
        add(capturedPanel, BorderLayout.WEST);
    }

    private void initializeBoard() {
        boardPanel = new BoardPanel(backend, result -> {
            if (result.isValid()) {
                boolean isWhite = !backend.getBoard().isWhiteTurn; // it was flipped after move
                String logEntry = isWhite ? (moveCount + ". " + result.algebraicMove()) : ("    ... " + result.algebraicMove());
                history.add(logEntry);
                historyTextArea.append(logEntry + "\n");
                
                if (!isWhite) moveCount++;
                
                if (result.capturedPiece() != 0) {
                    if (result.capturedPiece() > 0) {
                        capturedWhiteLabel.setText(capturedWhiteLabel.getText() + getUnicodePiece(result.capturedPiece()));
                    } else {
                        capturedBlackLabel.setText(capturedBlackLabel.getText() + getUnicodePiece(result.capturedPiece()));
                    }
                }
                
                statusLabel.setText(backend.getBoard().isWhiteTurn ? "White's Turn" : "Black's Turn");
                checkAIMove();
            } else {
                JOptionPane.showMessageDialog(this, "Illegal Move: " + result.message());
            }
        });
        add(boardPanel, BorderLayout.CENTER);
        pack();
    }

    private String getUnicodePiece(int piece) {
        boolean isWhite = piece > 0;
        return switch (Math.abs(piece)) {
            case 6 -> isWhite ? "♔" : "♚";
            case 5 -> isWhite ? "♕" : "♛";
            case 4 -> isWhite ? "♖" : "♜";
            case 3 -> isWhite ? "♗" : "♝";
            case 2 -> isWhite ? "♘" : "♞";
            case 1 -> isWhite ? "♙" : "♟";
            default -> "";
        };
    }
    
    private void addControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        difficultyCombo = new JComboBox<>(new String[]{"Easy (Depth 2)", "Medium (Depth 3)", "Hard (Depth 4)"});
        difficultyCombo.setSelectedIndex(2); // Hard by default
        difficultyCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        aiWhiteCheckbox = new JCheckBox("AI White", false);
        aiWhiteCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        aiWhiteCheckbox.addActionListener(e -> checkAIMove());
        
        aiBlackCheckbox = new JCheckBox("AI Black", true);
        aiBlackCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        aiBlackCheckbox.addActionListener(e -> checkAIMove());
        
        JButton puzzleBtn = new JButton("Play Puzzle");
        puzzleBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        puzzleBtn.addActionListener(e -> loadRandomPuzzle());
        
        JButton quitBtn = new JButton("Quit & Summary");
        quitBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        quitBtn.setForeground(Color.RED);
        quitBtn.addActionListener(e -> {
            showMoveHistory();
            System.exit(0);
        });

        controlPanel.add(new JLabel("Diff:"));
        controlPanel.add(difficultyCombo);
        controlPanel.add(aiWhiteCheckbox);
        controlPanel.add(aiBlackCheckbox);
        controlPanel.add(puzzleBtn);
        controlPanel.add(quitBtn);
        add(controlPanel, BorderLayout.NORTH);
    }
    
    private void loadRandomPuzzle() {
        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }
        boardPanel.clearAIHighlights();
        boardPanel.setInputBlocked(false);
        aiStatusLabel.setText("Idle");
        aiProgressBar.setString("Ready");

        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(new java.io.File("C:\\Users\\jcarl\\Projects\\GitHub\\HelloChess\\HelloChess\\puzzles.txt").toPath());
            if (!lines.isEmpty()) {
                String fen = lines.get(new java.util.Random().nextInt(lines.size()));
                FenParser.loadFEN(backend.getBoard(), fen);
                history.clear();
                historyTextArea.setText("");
                moveCount = 1;
                capturedWhiteLabel.setText(" Captured by Black: ");
                capturedBlackLabel.setText(" Captured by White: ");
                statusLabel.setText("Puzzle Loaded! " + (backend.getBoard().isWhiteTurn ? "White's Turn" : "Black's Turn"));
                boardPanel.repaint();
                checkAIMove();
            } else {
                JOptionPane.showMessageDialog(this, "No puzzles found in puzzles.txt");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load puzzles.txt: " + ex.getMessage());
        }
    }

    private void showMoveHistory() {
        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }
        
        StringBuilder report = new StringBuilder("--- Game Move History ---\n\n");
        
        if (history.isEmpty()) {
            report.append("No moves were recorded.");
        } else {
            for (String move : history) {
                report.append(move).append("\n");
            }
        }

        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Final Move Readout", JOptionPane.INFORMATION_MESSAGE);
        
        System.out.println("Game Over!");
    	System.out.println("\nGame History:");
    	backend.printPositionHistory();
    }

    private void checkAIMove() {
        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }
        
        FastBoard board = backend.getBoard();
        boolean isWhite = board.isWhiteTurn;
        
        List<Integer> legalMoves = MoveGenerator.generateLegalMoves(board);
        if (legalMoves.isEmpty()) {
            boolean isCheck = MoveGenerator.isSquareAttacked(board, getKingSquare(board, isWhite), !isWhite);
            if (isCheck) {
                statusLabel.setText("Checkmate! " + (isWhite ? "Black" : "White") + " wins.");
            } else {
                statusLabel.setText("Stalemate! Game is drawn.");
            }
            return;
        }
        
        boolean aiPlaysThisTurn = (isWhite && aiWhiteCheckbox.isSelected()) || (!isWhite && aiBlackCheckbox.isSelected());
        if (aiPlaysThisTurn) {
            boardPanel.setInputBlocked(true);
            int depth = getSelectedDepth();
            activeWorker = new SearchWorker(depth);
            activeWorker.execute();
        }
    }

    private int getSelectedDepth() {
        int idx = difficultyCombo.getSelectedIndex();
        return idx + 2; // Index 0 -> Depth 2 (Easy), 1 -> Depth 3 (Medium), 2 -> Depth 4 (Hard)
    }

    private String indexToAlgebraic(int index) {
        char file = (char) ('a' + (index % 8));
        int rank = (index / 8) + 1;
        return "" + file + rank;
    }

    private String formatScore(int score) {
        if (Math.abs(score) > 90000) {
            return "M" + (100000 - Math.abs(score));
        }
        return String.format("%s%.2f", score >= 0 ? "+" : "", score / 100.0);
    }

    private int getKingSquare(FastBoard board, boolean isWhite) {
        int king = isWhite ? FastBoard.W_KING : FastBoard.B_KING;
        for (int i = 0; i < 64; i++) {
            if (board.board[i] == king) return i;
        }
        return -1;
    }

    private record SearchUpdate(int type, int depth, int currentMove, int currentMoveIndex, int totalRootMoves, int bestMove, int score, long nodes, long timeMs) {}

    private class SearchWorker extends SwingWorker<Integer, SearchUpdate> {
        private final int maxDepth;
        private final FastBoard searchBoard;

        public SearchWorker(int maxDepth) {
            this.maxDepth = maxDepth;
            this.searchBoard = new FastBoard(backend.getBoard());
        }

        @Override
        protected Integer doInBackground() throws Exception {
            return SearchEngine.findBestMove(searchBoard, maxDepth, new SearchEngine.SearchListener() {
                @Override
                public void onSearchStarted(List<Integer> rootMoves) {
                    publish(new SearchUpdate(0, 0, -1, 0, rootMoves.size(), -1, 0, 0, 0));
                }

                @Override
                public void onDepthStarted(int depth) {
                    publish(new SearchUpdate(1, depth, -1, 0, 0, -1, 0, 0, 0));
                }

                @Override
                public void onCandidateMoveStarted(int move, int index, int total) {
                    publish(new SearchUpdate(2, 0, move, index, total, -1, 0, 0, 0));
                }

                @Override
                public void onCandidateMoveCompleted(int move, int score) {
                }

                @Override
                public void onDepthCompleted(int depth, int bestMove, int score, long nodesSearched, long timeMs) {
                    publish(new SearchUpdate(3, depth, -1, 0, 0, bestMove, score, nodesSearched, timeMs));
                }

                @Override
                public void onSearchCompleted(int bestMove, int score, long nodesSearched, long timeMs) {
                    publish(new SearchUpdate(4, 0, -1, 0, 0, bestMove, score, nodesSearched, timeMs));
                }
            }, 60); // 60ms throttle per root move to visualize thought process nicely
        }

        @Override
        protected void process(List<SearchUpdate> chunks) {
            if (isCancelled()) return;
            SearchUpdate latest = chunks.get(chunks.size() - 1);

            switch (latest.type) {
                case 0:
                    aiStatusLabel.setText("Thinking...");
                    aiProgressBar.setMaximum(latest.totalRootMoves);
                    aiProgressBar.setValue(0);
                    aiProgressBar.setString("Analyzing moves...");
                    break;
                case 1:
                    aiDepthLabel.setText(String.valueOf(latest.depth));
                    aiProgressBar.setValue(0);
                    break;
                case 2:
                    int from = latest.currentMove & 0x3F;
                    int to = (latest.currentMove >> 6) & 0x3F;
                    boardPanel.setEvaluatingMove(from, to);
                    aiProgressBar.setValue(latest.currentMoveIndex + 1);
                    aiProgressBar.setString("Evaluating " + (latest.currentMoveIndex + 1) + " / " + latest.totalRootMoves);
                    break;
                case 3:
                    int bestFrom = latest.bestMove & 0x3F;
                    int bestTo = (latest.bestMove >> 6) & 0x3F;
                    boardPanel.setBestCandidateMove(bestFrom, bestTo);
                    aiBestMoveLabel.setText(indexToAlgebraic(bestFrom) + indexToAlgebraic(bestTo));
                    aiScoreLabel.setText(formatScore(latest.score));
                    aiNodesLabel.setText(String.format("%,d", latest.nodes));

                    double secs = latest.timeMs / 1000.0;
                    long nps = secs > 0.001 ? (long) (latest.nodes / secs) : 0;
                    aiSpeedLabel.setText(String.format("%,d", nps));
                    break;
            }
        }

        @Override
        protected void done() {
            boardPanel.clearAIHighlights();
            boardPanel.setInputBlocked(false);
            aiStatusLabel.setText("Idle");
            aiProgressBar.setString("Ready");
            
            if (isCancelled()) return;

            try {
                int bestMove = get();
                if (bestMove != -1) {
                    int from = bestMove & 0x3F;
                    int to = (bestMove >> 6) & 0x3F;
                    String moveStr = indexToAlgebraic(from) + " " + indexToAlgebraic(to);

                    MoveResult res = backend.processMove(moveStr);
                    if (res.isValid()) {
                        boardPanel.executeAIMove(bestMove, res);
                    } else {
                        System.err.println("AI generated illegal move: " + moveStr);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Game myGame = new Game();
        SwingUtilities.invokeLater(() -> new ChessGUI(myGame));
    }
}