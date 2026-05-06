package HelloChess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Enhanced Chess GUI for Java 22 / Eclipse 2026-03
 * Supports Drag-and-Drop and Turn-based play.
 */
public class ChessGUI extends JFrame {
	// To silence the compiler of Google-generated ChessGUI code
	// Thanks, "serializable class has no definition of serialversionuid"
	private static final long serialVersionUID = 1L;
	
	// Move History 
	private final java.util.List<String> history = new java.util.ArrayList<>();
	private int moveCount = 1; // To track move numbers (e.g., 1. e2e4)

    // Record for clean coordinate handling
    private record Position(int row, int col) {}

    // Track the internal state of the board for the UI
    private final String[][] boardState = new String[8][8];
    private final JButton[][] squares = new JButton[8][8];
    
    private final Color lightColor = new Color(240, 217, 181);
    private final Color darkColor = new Color(181, 136, 99);
    private final Color highlightColor = new Color(130, 151, 105);

    private Position dragSource = null;
    private boolean whiteTurn = true;
    private final JLabel statusLabel = new JLabel("White's Turn");
    
    private final Game backend; // Replace 'ChessBackend' with your actual class name (e.g., Board or Game)

    public ChessGUI(Game backend) {
        this.backend = backend;
        setupWindow();
        addControlPanel();
        initializeData();
        initializeBoard();
        setVisible(true);
    }

    private void setupWindow() {
        setTitle("Chess Prototype - Drag & Drop (Java 22)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void initializeData() {
        // Initialize board with Unicode pieces
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boardState[r][c] = getInitialPiece(r, c);
            }
        }
    }

    private void initializeBoard() {
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setPreferredSize(new Dimension(640, 640));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton square = new JButton(boardState[row][col]);
                square.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 50));
                square.setOpaque(true);
                square.setBorderPainted(false);
                square.setFocusable(false);
                square.setBackground((row + col) % 2 == 0 ? lightColor : darkColor);

                // Mouse Adapter to handle Drag and Drop
                final int r = row;
                final int c = col;
                MouseAdapter dragHandler = new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        handlePress(r, c);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        // Find which component the mouse is over upon release
                        Component comp = boardPanel.getComponentAt(
                            e.getComponent().getX() + e.getX(), 
                            e.getComponent().getY() + e.getY()
                        );
                        if (comp instanceof JButton targetSquare) {
                            // Find coordinates of the targetSquare
                            for (int tr = 0; tr < 8; tr++) {
                                for (int tc = 0; tc < 8; tc++) {
                                    if (squares[tr][tc] == targetSquare) {
                                        handleRelease(new Position(tr, tc));
                                        return;
                                    }
                                }
                            }
                        }
                        resetBoardColors(); // Reset if dropped out of bounds
                    }
                };

                square.addMouseListener(dragHandler);
                square.addMouseMotionListener(dragHandler);

                squares[row][col] = square;
                boardPanel.add(square);
            }
        }
        add(boardPanel, BorderLayout.CENTER);
        pack(); // added to force preferred size
    }

    private void handlePress(int row, int col) {
        String piece = boardState[row][col];
        if (piece.isEmpty()) return;

        // Turn Validation: check if the piece color matches the current turn
        boolean isWhitePiece = "♖♘♗♕♔♙".contains(piece);
        if (isWhitePiece != whiteTurn) return;

        dragSource = new Position(row, col);
        squares[row][col].setBackground(highlightColor);
    }

    private void handleRelease(Position target) {
        if (dragSource == null) return;

        // Basic Move Execution (Logic validation would happen here)
        if (!dragSource.equals(target)) {
            executeMove(dragSource, target);
        }

        dragSource = null;
        resetBoardColors();
        refreshUI();
    }

    private void executeMove(Position from, Position to) {
        String fromStr = toAlgebraic(from);
        String toStr = toAlgebraic(to);
        String command = fromStr + " " + toStr; // e.g., "e2 e4"

        System.out.println("GUI Command Generated: " + command);

        // --- INTERFACE POINT WITH BACKEND ---
        // Assuming your backend controller is named 'chessGame'
        boolean success = backend.processMove(command);
        
        // For now, we simulate success to update the UI
        // boolean success = true; 

        if (success) {
        	// Record the move in the history list
            // Format: "1. e2e4" or "1. ... e7e5"
            String logEntry = (whiteTurn) ? (moveCount + ". " + command) : ("    ... " + command);
            history.add(logEntry);
            
            if (!whiteTurn) moveCount++; // Increment move number after Black plays
            // Update the GUI internal state only if the move was legal
            String piece = boardState[from.row()][from.col()];
            boardState[to.row()][to.col()] = piece;
            boardState[from.row()][from.col()] = "";
            
            whiteTurn = !whiteTurn;
            statusLabel.setText(whiteTurn ? "White's Turn" : "Black's Turn");
        } else {
            // Optional: show a dialog if the move was illegal
            JOptionPane.showMessageDialog(this, "Illegal Move: " + command);
        }
    }

    /**
     * Converts a grid Position (row, col) to Chess Algebraic Notation (e.g., "e2").
     * In Swing GridLayout: 
     * Row 0 is Rank 8, Row 7 is Rank 1.
     * Col 0 is File 'a', Col 7 is File 'h'.
     */
    private String toAlgebraic(Position pos) {
        char file = (char) ('a' + pos.col());   // 0 -> 'a', 1 -> 'b', etc.
        int rank = 8 - pos.row();              // 0 -> 8, 7 -> 1
        return "" + file + rank;
    }
    
    private void resetBoardColors() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                squares[r][c].setBackground((r + c) % 2 == 0 ? lightColor : darkColor);
            }
        }
    }

    private void refreshUI() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                squares[r][c].setText(boardState[r][c]);
            }
        }
    }

    private String getInitialPiece(int r, int c) {
        return switch (r) {
            case 0 -> switch (c) {
                case 0, 7 -> "♜"; case 1, 6 -> "♞"; case 2, 5 -> "♝";
                case 3 -> "♛"; case 4 -> "♚"; default -> "";
            };
            case 1 -> "♟";
            case 6 -> "♙";
            case 7 -> switch (c) {
                case 0, 7 -> "♖"; case 1, 6 -> "♘"; case 2, 5 -> "♗";
                case 3 -> "♕"; case 4 -> "♔"; default -> "";
            };
            default -> "";
        };
    }
    
    private void addControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton quitBtn = new JButton("Quit & Summary");
        
        quitBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        quitBtn.setForeground(Color.RED);

        quitBtn.addActionListener(_ -> {
            showMoveHistory();
            
            System.exit(0);
        });

        controlPanel.add(quitBtn);
        add(controlPanel, BorderLayout.NORTH); // Add to the top of the frame
    }

    private void showMoveHistory() {
        StringBuilder report = new StringBuilder("--- Game Move History ---\n\n");
        
        if (history.isEmpty()) {
            report.append("No moves were recorded.");
        } else {
            for (String move : history) {
                report.append(move).append("\n");
            }
        }

        // Create a scrollable text area for the summary
        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Final Move Readout", JOptionPane.INFORMATION_MESSAGE);
        
     // End Times
        System.out.println("Game Over!");
    	System.out.println("\nGame History:");
    	backend.printPositionHistory();
    
    }

    public static void main(String[] args) {
        // Java 22 Desktop initialization
        // SwingUtilities.invokeLater(ChessGUI::new);
        Game myGame = new Game(); // Your existing logic
        
        // Launch GUI and give it the game instance
        SwingUtilities.invokeLater(() -> new ChessGUI(myGame));
        
       }
}