package HelloChess;

import javax.swing.*;
import java.awt.*;

/**
 * Enhanced Chess GUI for Java 22 / Eclipse 2026-03
 * Supports Drag-and-Drop and Turn-based play with Premium UI.
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

    public ChessGUI(Game backend) {
        this.backend = backend;
        setupWindow();
        addControlPanel();
        initializeBoard();
        setVisible(true);
    }

    private void setupWindow() {
        setTitle("Chess Prototype - Premium Fast UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.SOUTH);
        
        historyTextArea.setEditable(false);
        historyTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Move History"));
        add(scrollPane, BorderLayout.EAST);
        
        JPanel capturedPanel = new JPanel(new GridLayout(2, 1));
        capturedPanel.setBorder(BorderFactory.createTitledBorder("Captured Pieces"));
        capturedWhiteLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        capturedBlackLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        capturedPanel.add(capturedWhiteLabel);
        capturedPanel.add(capturedBlackLabel);
        add(capturedPanel, BorderLayout.WEST);
    }

    private void initializeBoard() {
        boardPanel = new BoardPanel(backend, "C:\\Users\\jcarl\\Projects\\GitHub\\HelloChess\\HelloChess\\src\\HelloChess\\chess_pieces_sprite.png", result -> {
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
        JButton quitBtn = new JButton("Quit & Summary");
        
        quitBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        quitBtn.setForeground(Color.RED);

        quitBtn.addActionListener(_ -> {
            showMoveHistory();
            System.exit(0);
        });

        controlPanel.add(quitBtn);
        add(controlPanel, BorderLayout.NORTH);
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

    public static void main(String[] args) {
        Game myGame = new Game();
        SwingUtilities.invokeLater(() -> new ChessGUI(myGame));
    }
}