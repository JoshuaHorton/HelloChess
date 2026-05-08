package HelloChess;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpriteSlicer {
    private BufferedImage[] whitePieces; // K, Q, B, N, R, P
    private BufferedImage[] blackPieces; // K, Q, B, N, R, P
    
    public SpriteSlicer(String imagePath) {
        whitePieces = new BufferedImage[6];
        blackPieces = new BufferedImage[6];
        
        try {
            BufferedImage sheet = ImageIO.read(new File(imagePath));
            int pieceWidth = sheet.getWidth() / 6;
            int pieceHeight = sheet.getHeight() / 2;
            
            for (int i = 0; i < 6; i++) {
                whitePieces[i] = removeGreenBackground(sheet.getSubimage(i * pieceWidth, 0, pieceWidth, pieceHeight));
                blackPieces[i] = removeGreenBackground(sheet.getSubimage(i * pieceWidth, pieceHeight, pieceWidth, pieceHeight));
            }
        } catch (IOException e) {
            System.err.println("Failed to load sprite sheet: " + e.getMessage());
        }
    }
    
    private BufferedImage removeGreenBackground(BufferedImage img) {
        ImageFilter filter = new RGBImageFilter() {
            public final int filterRGB(int x, int y, int rgb) {
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Pure green tolerance
                if (g > 180 && r < 70 && b < 70) {
                    return rgb & 0x00FFFFFF; // Transparent
                }
                return rgb;
            }
        };
        
        ImageProducer ip = new FilteredImageSource(img.getSource(), filter);
        Image transparentImage = Toolkit.getDefaultToolkit().createImage(ip);
        
        BufferedImage bimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = bimg.createGraphics();
        g2d.drawImage(transparentImage, 0, 0, null);
        g2d.dispose();
        return bimg;
    }
    
    public BufferedImage getPieceImage(int piece) {
        if (piece == FastBoard.EMPTY) return null;
        boolean isWhite = piece > 0;
        int type = Math.abs(piece);
        int index = -1;
        
        switch (type) {
            case FastBoard.W_KING: index = 0; break;
            case FastBoard.W_QUEEN: index = 1; break;
            case FastBoard.W_BISHOP: index = 2; break;
            case FastBoard.W_KNIGHT: index = 3; break;
            case FastBoard.W_ROOK: index = 4; break;
            case FastBoard.W_PAWN: index = 5; break;
        }
        if (index == -1) return null;
        
        return isWhite ? whitePieces[index] : blackPieces[index];
    }
}
