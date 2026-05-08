package HelloChess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.function.Consumer;

public class BoardPanel extends JPanel {
    private Game game;
    private FastBoard currentBoard;
    private Consumer<MoveResult> onMoveCallback;
    
    private final Color lightColor = new Color(255, 255, 255);
    private final Color darkColor = new Color(153, 191, 230);
    private final Color highlightColor = new Color(255, 255, 51, 150);
    
    private int squareSize;
    private int boardX, boardY;
    
    private int dragSourceSquare = -1;
    private Point dragPoint = null;
    
    private Timer animationTimer;
    private int animFrom = -1;
    private int animTo = -1;
    private float animProgress = 1.0f;
    private long animStartTime;
    private static final int ANIM_DURATION_MS = 200;

    public BoardPanel(Game game, Consumer<MoveResult> onMoveCallback) {
        this.game = game;
        this.onMoveCallback = onMoveCallback;
        this.currentBoard = game.getBoard();
        
        setPreferredSize(new Dimension(640, 640));
        
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (animProgress < 1.0f) return;
                int sq = getSquareFromPoint(e.getPoint());
                if (sq != -1) {
                    int piece = currentBoard.getPiece(sq);
                    if (piece != FastBoard.EMPTY && (piece > 0) == currentBoard.isWhiteTurn) {
                        dragSourceSquare = sq;
                        dragPoint = e.getPoint();
                        repaint();
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragSourceSquare != -1) {
                    int destSq = getSquareFromPoint(e.getPoint());
                    if (destSq != -1 && destSq != dragSourceSquare) {
                        String fromAlg = indexToAlgebraic(dragSourceSquare);
                        String toAlg = indexToAlgebraic(destSq);
                        
                        MoveResult res = game.processMove(fromAlg + " " + toAlg);
                        if (res.isValid()) {
                            animFrom = dragSourceSquare;
                            animTo = destSq;
                            currentBoard = game.getBoard();
                            
                            animProgress = 0.0f;
                            animStartTime = System.currentTimeMillis();
                            if (animationTimer != null) animationTimer.stop();
                            animationTimer = new Timer(16, ev -> {
                                long now = System.currentTimeMillis();
                                animProgress = (now - animStartTime) / (float) ANIM_DURATION_MS;
                                if (animProgress >= 1.0f) {
                                    animProgress = 1.0f;
                                    animationTimer.stop();
                                    if (onMoveCallback != null) {
                                        onMoveCallback.accept(res);
                                    }
                                }
                                repaint();
                            });
                            animationTimer.start();
                        } else {
                            if (onMoveCallback != null) {
                                onMoveCallback.accept(res);
                            }
                        }
                    }
                    dragSourceSquare = -1;
                    dragPoint = null;
                    repaint();
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragSourceSquare != -1) {
                    dragPoint = e.getPoint();
                    repaint();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }
    
    private int getSquareFromPoint(Point p) {
        if (p.x < boardX || p.x >= boardX + 8 * squareSize || p.y < boardY || p.y >= boardY + 8 * squareSize) return -1;
        int file = (p.x - boardX) / squareSize;
        int rank = 7 - ((p.y - boardY) / squareSize);
        return rank * 8 + file;
    }
    
    private String indexToAlgebraic(int index) {
        char file = (char) ('a' + (index % 8));
        int rank = (index / 8) + 1;
        return "" + file + rank;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        squareSize = Math.min(getWidth(), getHeight()) / 8;
        boardX = (getWidth() - 8 * squareSize) / 2;
        boardY = (getHeight() - 8 * squareSize) / 2;
        
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                int x = boardX + file * squareSize;
                int y = boardY + (7 - rank) * squareSize;
                
                boolean isLight = (rank + file) % 2 != 0;
                Color baseColor = isLight ? lightColor : darkColor;
                g2d.setColor(baseColor);
                g2d.fillRect(x, y, squareSize, squareSize);
                
                if (dragSourceSquare == rank * 8 + file) {
                    g2d.setColor(highlightColor);
                    g2d.fillRect(x, y, squareSize, squareSize);
                }
            }
        }
        
        if (dragSourceSquare != -1) {
            List<Integer> legalMoves = MoveGenerator.generateLegalMoves(currentBoard);
            g2d.setColor(new Color(0, 255, 0, 100));
            for (int move : legalMoves) {
                int from = move & 0x3F;
                int to = (move >> 6) & 0x3F;
                if (from == dragSourceSquare) {
                    int file = to % 8;
                    int rank = to / 8;
                    int x = boardX + file * squareSize + squareSize / 2;
                    int y = boardY + (7 - rank) * squareSize + squareSize / 2;
                    g2d.fill(new Ellipse2D.Double(x - squareSize/6.0, y - squareSize/6.0, squareSize/3.0, squareSize/3.0));
                }
            }
        }
        
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                int index = rank * 8 + file;
                
                if (index == dragSourceSquare) continue;
                if (animProgress < 1.0f && index == animTo) continue;
                
                int piece = currentBoard.getPiece(index);
                if (piece != FastBoard.EMPTY) {
                    int x = boardX + file * squareSize;
                    int y = boardY + (7 - rank) * squareSize;
                    drawPiece(g2d, piece, x, y, squareSize);
                }
            }
        }
        
        if (animProgress < 1.0f && animFrom != -1 && animTo != -1) {
            int piece = currentBoard.getPiece(animTo);
            if (piece != FastBoard.EMPTY) {
                int startFile = animFrom % 8;
                int startRank = animFrom / 8;
                int endFile = animTo % 8;
                int endRank = animTo / 8;
                
                int startX = boardX + startFile * squareSize;
                int startY = boardY + (7 - startRank) * squareSize;
                int endX = boardX + endFile * squareSize;
                int endY = boardY + (7 - endRank) * squareSize;
                
                float t = smootherStep(animProgress);
                int currX = (int) (startX + (endX - startX) * t);
                int currY = (int) (startY + (endY - startY) * t);
                
                drawPiece(g2d, piece, currX, currY, squareSize);
            }
        }
        
        if (dragSourceSquare != -1 && dragPoint != null) {
            int piece = currentBoard.getPiece(dragSourceSquare);
            if (piece != FastBoard.EMPTY) {
                int file = dragSourceSquare % 8;
                int rank = dragSourceSquare / 8;
                int ox = boardX + file * squareSize;
                int oy = boardY + (7 - rank) * squareSize;
                
                Composite oldComp = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                drawPiece(g2d, piece, ox, oy, squareSize);
                g2d.setComposite(oldComp);
                
                int x = dragPoint.x - squareSize / 2;
                int y = dragPoint.y - squareSize / 2;
                drawPiece(g2d, piece, x, y, squareSize);
            }
        }
    }
    
    private float smootherStep(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private String getSolidUnicodePiece(int piece) {
        return switch (Math.abs(piece)) {
            case FastBoard.W_KING -> "♚";
            case FastBoard.W_QUEEN -> "♛";
            case FastBoard.W_ROOK -> "♜";
            case FastBoard.W_BISHOP -> "♝";
            case FastBoard.W_KNIGHT -> "♞";
            case FastBoard.W_PAWN -> "♟";
            default -> "";
        };
    }
    
    private void drawPiece(Graphics2D g2d, int piece, int x, int y, int size) {
        String str = getSolidUnicodePiece(piece);
        if (str.isEmpty()) return;
        
        g2d.setFont(new Font("Segoe UI Symbol", Font.PLAIN, (int)(size * 0.8)));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (size - fm.stringWidth(str)) / 2;
        int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
        
        boolean isWhite = piece > 0;
        
        if (isWhite) {
            // Draw a white piece with a crisp dark outline
            g2d.setColor(new Color(40, 40, 40));
            g2d.drawString(str, textX - 1, textY - 1);
            g2d.drawString(str, textX + 1, textY + 1);
            g2d.drawString(str, textX - 1, textY + 1);
            g2d.drawString(str, textX + 1, textY - 1);
            g2d.setColor(Color.WHITE);
            g2d.drawString(str, textX, textY);
        } else {
            // Draw a flat black piece
            g2d.setColor(new Color(30, 30, 30));
            g2d.drawString(str, textX, textY);
        }
    }
}
