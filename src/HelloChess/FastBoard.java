package HelloChess;

public class FastBoard {
    public static final int EMPTY = 0;
    public static final int W_PAWN = 1, W_KNIGHT = 2, W_BISHOP = 3, W_ROOK = 4, W_QUEEN = 5, W_KING = 6;
    public static final int B_PAWN = -1, B_KNIGHT = -2, B_BISHOP = -3, B_ROOK = -4, B_QUEEN = -5, B_KING = -6;

    public int[] board;
    public boolean isWhiteTurn;
    public int castlingRights; // bits: 1=WK, 2=WQ, 4=BK, 8=BQ
    public int enPassantTarget; // -1 if none, else square index 0-63
    public int halfMoveClock;
    public int fullMoveNumber;

    public FastBoard() {
        board = new int[64];
        setupInitialPosition();
    }
    
    // Copy constructor for making moves during search
    public FastBoard(FastBoard other) {
        this.board = other.board.clone();
        this.isWhiteTurn = other.isWhiteTurn;
        this.castlingRights = other.castlingRights;
        this.enPassantTarget = other.enPassantTarget;
        this.halfMoveClock = other.halfMoveClock;
        this.fullMoveNumber = other.fullMoveNumber;
    }

    public void setupInitialPosition() {
        for (int i = 0; i < 64; i++) board[i] = EMPTY;
        
        // White pieces (A1 to H1 is 0 to 7)
        board[0] = W_ROOK; board[1] = W_KNIGHT; board[2] = W_BISHOP; board[3] = W_QUEEN;
        board[4] = W_KING; board[5] = W_BISHOP; board[6] = W_KNIGHT; board[7] = W_ROOK;
        for (int i = 8; i < 16; i++) board[i] = W_PAWN;
        
        // Black pieces (A8 to H8 is 56 to 63)
        for (int i = 48; i < 56; i++) board[i] = B_PAWN;
        board[56] = B_ROOK; board[57] = B_KNIGHT; board[58] = B_BISHOP; board[59] = B_QUEEN;
        board[60] = B_KING; board[61] = B_BISHOP; board[62] = B_KNIGHT; board[63] = B_ROOK;
        
        isWhiteTurn = true;
        castlingRights = 15; // 1 | 2 | 4 | 8
        enPassantTarget = -1;
        halfMoveClock = 0;
        fullMoveNumber = 1;
    }
    
    public int getPiece(int index) {
        return board[index];
    }
    
    public void executeMove(int move) {
        int from = move & 0x3F;
        int to = (move >> 6) & 0x3F;
        int piece = (move >> 12) & 0xF;
        if ((piece & 8) != 0) piece |= 0xFFFFFFF0; // sign extend negative
        
        int captured = (move >> 16) & 0xF;
        if ((captured & 8) != 0) captured |= 0xFFFFFFF0;
        
        int promoted = (move >> 20) & 0xF;
        if ((promoted & 8) != 0) promoted |= 0xFFFFFFF0;
        
        int flags = (move >> 24) & 0xF;
        
        // Execute on board
        board[to] = board[from];
        board[from] = EMPTY;
        
        // Handle promotion
        if (promoted != 0) {
            board[to] = promoted;
        }
        
        // Handle En Passant capture
        if (flags == 2) {
            int captureSquare = isWhiteTurn ? to - 8 : to + 8;
            board[captureSquare] = EMPTY;
        }
        
        // Handle Castling
        if (flags == 3) {
            if (isWhiteTurn) { board[5] = W_ROOK; board[7] = EMPTY; }
            else { board[61] = B_ROOK; board[63] = EMPTY; }
        } else if (flags == 4) {
            if (isWhiteTurn) { board[3] = W_ROOK; board[0] = EMPTY; }
            else { board[59] = B_ROOK; board[56] = EMPTY; }
        }
        
        // Update state
        isWhiteTurn = !isWhiteTurn;
        if (isWhiteTurn) fullMoveNumber++; // if it's now white's turn, full move completed
        
        // Update halfMoveClock
        if (Math.abs(board[to]) == W_PAWN || captured != 0) halfMoveClock = 0;
        else halfMoveClock++;
        
        // Update en passant target
        if (flags == 1) { // double pawn push
            enPassantTarget = (!isWhiteTurn) ? to - 8 : to + 8; // !isWhiteTurn means white just played
        } else {
            enPassantTarget = -1;
        }
        
        // Update castling rights
        if (from == 4 || to == 4) castlingRights &= ~3; // WK, WQ
        if (from == 60 || to == 60) castlingRights &= ~12; // BK, BQ
        if (from == 0 || to == 0) castlingRights &= ~2; // WQ
        if (from == 7 || to == 7) castlingRights &= ~1; // WK
        if (from == 56 || to == 56) castlingRights &= ~8; // BQ
        if (from == 63 || to == 63) castlingRights &= ~4; // BK
    }
}
