package HelloChess;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Scanner;



public class Game {
	private Grid board = new Grid();
	ArrayList<String> labels = board.getSquareLabels(); // just the algebraic notation labels for each square
	private ArrayList<Piece> pieces_list = new ArrayList<Piece>();
	// private Position initial_position = new Position(0, board);
	private HashMap<String, Piece> position_map = new HashMap<String, Piece>(); // This will map the pieces to the board 
																				// positions for each move
																				// with square_name as key
	private Position initial_position  = new Position(0, labels);
	private Position new_position, last_position; // temporary registers
	private Map<Integer, Position> position_history = new HashMap<Integer, Position>(); // accummulation of move-by-move history
	private int position_no = 0;
	// private Scanner sc ; // gets input from command line for move
	Piece piece_to_move;
	PieceColor move_color = PieceColor.WHITE;
	
	public List<Piece> getCapturedWhitePieces() { return capturedWhitePieces; }
	public List<Piece> getCapturedBlackPieces() { return capturedBlackPieces; }
	private List<Piece> capturedWhitePieces = new ArrayList<>();
	private List<Piece> capturedBlackPieces = new ArrayList<>();
	
	
	public Game() {
		System.out.println("Setting Pieces.");
		this.setPieces();
		this.updateBoardwithPosition(initial_position);
		position_history.put(position_no, initial_position); // starting position for game history
		// this.printPositionHistory();
		last_position = initial_position;
		
		
		/* SCANNER-BASED INPUT LOOP
		last_position = initial_position;
		Piece piece_to_move;
		PieceColor move_color = PieceColor.WHITE;
		String start_pos = "", dest_pos = "";
		
		
		while (position_no>=0) {
			position_no++;
			// Debugging moves
			System.out.println("Move " + position_no);
			new_position = new Position(position_no, labels);
			new_position.setPositionMap(last_position.getPositionMap());
			
			System.out.println(move_color + " has the move. Enter Starting Square:");
			sc = new Scanner(System.in);
			start_pos = sc.next();
			System.out.println("Enter Destination Square");
			dest_pos = sc.next();
			
			
			if ( !start_pos.equals("-1") && !dest_pos.equals("-1") ) {
				piece_to_move = new_position.get_piece_at_square(start_pos);
				if (piece_to_move != null) {
					System.out.println("Moving " + piece_to_move.getName() + " at square " + start_pos + " to " + dest_pos);
					new_position.setPosition(piece_to_move, start_pos, dest_pos);
					this.updateBoardwithPosition(new_position);
					position_history.put(position_no, new_position);
				} else {
					System.err.println("Invalid Move - No Piece at Square " + start_pos);
				}
				last_position = new_position;
				if (move_color == PieceColor.WHITE) { move_color = PieceColor.BLACK; } else { move_color = PieceColor.WHITE; }
			} else { 
				position_no = -1; 
				// game over 
			}
		} 
		sc.close();
		*** END SCANNER-BASED INPUT LOOP */
		
		// To be Run at end of game
	//	System.out.println("Game Over!");
	//	System.out.println("\nGame History:");
	//	this.printPositionHistory();
	}
	
	// Game.java

	/**
	 * This replaces the old 'Scanner' logic.
	 * The GUI calls this whenever a user finishes a drag-and-drop.
	 * @param moveInput Example: "e2 e4"
	 * @return true if the move was legal and executed; false otherwise.
	 */
	public MoveResult processMove(String moveInput) {
	    try {
	        String[] parts = moveInput.split(" ");
	        String from = parts[0];
	        String to = parts[1];

			Piece piece_to_move = last_position.get_piece_at_square(from);
			if (piece_to_move == null) return MoveResult.invalid("No piece at start square");
			
			if (!piece_to_move.getColor().equals(move_color.toString())) return MoveResult.invalid("Not your turn");

	        if (this.isValidMove(from, to, piece_to_move)) {
	            return this.executeInternalMove(from, to, piece_to_move); 
	        } else {
				return MoveResult.invalid("Illegal move pattern or blocked");
			}
	    } catch (Exception e) {
	        return MoveResult.invalid("Error processing move: " + e.getMessage());
	    }
	}
	
	private MoveResult executeInternalMove(String sFrom, String sTo, Piece pieceToMove) {
		position_no++;
		System.out.println("Move " + position_no);
		new_position = new Position(position_no, labels);
		new_position.setPositionMap(last_position.getPositionMap());
		
		System.out.println(move_color + " has the move. Moving from " + sFrom + " to " + sTo + "\n");
		
		Piece capturedPiece = new_position.setPosition(pieceToMove, sFrom, sTo);
		if (capturedPiece != null) {
			if (capturedPiece.getColor().equals("WHITE")) {
				capturedWhitePieces.add(capturedPiece);
			} else {
				capturedBlackPieces.add(capturedPiece);
			}
		}
		this.updateBoardwithPosition(new_position);
		position_history.put(position_no, new_position);
		
		last_position = new_position;
		if (move_color == PieceColor.WHITE) { move_color = PieceColor.BLACK; } else { move_color = PieceColor.WHITE; }
		
		return MoveResult.valid(capturedPiece, sFrom + " " + sTo);
	}
	
	private boolean isValidMove(String start_pos, String dest_pos, Piece piece) {
		int startCol = start_pos.charAt(0) - 'a';
		int startRow = start_pos.charAt(1) - '1';
		int destCol = dest_pos.charAt(0) - 'a';
		int destRow = dest_pos.charAt(1) - '1';

		int dCol = destCol - startCol;
		int dRow = destRow - startRow;
		
		Piece targetPiece = last_position.get_piece_at_square(dest_pos);
		if (targetPiece != null && targetPiece.getColor().equals(piece.getColor())) {
			return false;
		}

		switch (piece.getType()) {
			case "PAWN":
				int direction = piece.getColor().equals("WHITE") ? 1 : -1;
				int startRank = piece.getColor().equals("WHITE") ? 1 : 6;
				
				if (dCol == 0) {
					if (dRow == direction && targetPiece == null) return true;
					if (dRow == 2 * direction && startRow == startRank && targetPiece == null && last_position.get_piece_at_square("" + start_pos.charAt(0) + (char)(start_pos.charAt(1) + direction)) == null) return true;
				}
				if (Math.abs(dCol) == 1 && dRow == direction && targetPiece != null) return true;
				return false;

			case "KNIGHT":
				if ((Math.abs(dCol) == 2 && Math.abs(dRow) == 1) || (Math.abs(dCol) == 1 && Math.abs(dRow) == 2)) return true;
				return false;

			case "BISHOP":
				if (Math.abs(dCol) != Math.abs(dRow)) return false;
				return !isPathBlocked(startCol, startRow, destCol, destRow);

			case "ROOK":
				if (dCol != 0 && dRow != 0) return false;
				return !isPathBlocked(startCol, startRow, destCol, destRow);

			case "QUEEN":
				if (dCol != 0 && dRow != 0 && Math.abs(dCol) != Math.abs(dRow)) return false;
				return !isPathBlocked(startCol, startRow, destCol, destRow);

			case "KING":
				if (Math.abs(dCol) <= 1 && Math.abs(dRow) <= 1) return true;
				return false;
				
			default:
				return false;
		}
	}

	private boolean isPathBlocked(int startCol, int startRow, int destCol, int destRow) {
		int stepCol = Integer.signum(destCol - startCol);
		int stepRow = Integer.signum(destRow - startRow);
		
		int c = startCol + stepCol;
		int r = startRow + stepRow;
		
		while (c != destCol || r != destRow) {
			String square = "" + (char)('a' + c) + (char)('1' + r);
			if (last_position.get_piece_at_square(square) != null) {
				return true;
			}
			c += stepCol;
			r += stepRow;
		}
		return false;
	}
	
	public void updateBoardwithPosition(Position position_to_set) {
		// System.out.println("Entering updateBoardwithPosition(" + position_to_set.getPositionID() + ")");
		ArrayList<String> square_labels = new ArrayList<String>(position_map.keySet());
		for (String label : square_labels) {
			// System.out.println("Running setSquare " + label);
			board.setSquare(label, position_map.get(label));
		}
		// this.committed = true; // lock the position
	}
	
	private void setPieces(){
		for (PieceColor pc : PieceColor.values()) {
			// System.out.println("\tPieceColor: " + pc.toString() +"\n" );
			for (PieceType pt : PieceType.values()) {
				// System.out.println("\t\tPieceType: " + pt.toString() + "\n" );
				String square = "";
				Piece p;
				switch (pt) {
				case KING: 
					p = new Piece(pc, pt);
					pieces_list.add(p);
					if (pc.compareTo(PieceColor.BLACK) == 0) { square = "e8"; } else {square = "e1"; }
					// System.out.println("Setting: " + p.getColor() + " " + p.getName() + " to " + square + "\n");
					initial_position.setPosition(p,  null,  square);
					break;
				case QUEEN: 
					p = new Piece(pc, pt);
					pieces_list.add(p);
					if (pc.compareTo(PieceColor.BLACK) == 0) { square = "d8"; } else {square = "d1"; }
					// System.out.println("Setting: " + p.getColor() + " " + p.getName() + " to " + square + "\n");
					initial_position.setPosition(p,  null,  square);
					break;
				case ROOK:
					for (int i = 1; i<=2; i++ ) { 
						p = new Piece(pc, pt);
						pieces_list.add(p);
						if (pc.compareTo(PieceColor.BLACK) == 0) { 
							if (Integer.compare(i, 1)==0) { square = "h8"; } else { square = "a8"; }
						} else {
							if (Integer.compare(i, 1)==0) { square = "h1"; } else { square = "a1"; } 
						}
						// System.out.println("Setting: " + p.getColor() + " " + p.getName() + " to " + square + "\n");
						initial_position.setPosition(p,  null,  square);
					}
					break;
				case KNIGHT:
					for (int i = 1; i<=2; i++ ) { 
						p = new Piece(pc, pt);
						pieces_list.add(p);
						if (pc.compareTo(PieceColor.BLACK) == 0) { 
							if (Integer.compare(i, 1)==0) { square = "g8"; } else { square = "b8"; }
						} else {
							if (Integer.compare(i, 1)==0) { square = "g1"; } else { square = "b1"; } 
						}
						// System.out.println("Setting: " + p.getColor() + " " + p.getName() + " to " + square + "\n");
						initial_position.setPosition(p,  null,  square);
					}
					break;
				case BISHOP:
					for (int i = 1; i<=2; i++ ) { 
						p = new Piece(pc, pt);
						pieces_list.add(p);
						if (pc.compareTo(PieceColor.BLACK) == 0) { 
							if (Integer.compare(i, 1)==0) { square = "f8"; } else { square = "c8"; }
						} else {
							if (Integer.compare(i, 1)==0) { square = "f1"; } else { square = "c1"; } 
						}
						// System.out.println("Setting: " + p.getColor() + " " + p.getName() + " to " + square + "\n");
						initial_position.setPosition(p,  null,  square);
					}
					break;
				case PAWN:
					for (int i = 1; i<=8; i++ ) { 
						p = new Piece(pc, pt);
						pieces_list.add(p);
						if (pc.compareTo(PieceColor.BLACK) == 0) { 
							switch (i) {
								case 1: square = "h7"; break;
								case 2: square = "g7"; break;
								case 3: square = "f7"; break;
								case 4: square = "e7"; break;
								case 5: square = "d7"; break;
								case 6: square = "c7"; break;
								case 7: square = "b7"; break;
								case 8: square = "a7"; break;
							}
						} else {
							switch (i) {
								case 1: square = "h2"; break;
								case 2: square = "g2"; break;
								case 3: square = "f2"; break;
								case 4: square = "e2"; break;
								case 5: square = "d2"; break;
								case 6: square = "c2"; break;
								case 7: square = "b2"; break;
								case 8: square = "a2"; break;
							} 
						}
						// System.out.println("Setting: " + p.getColor() + " " + p.getName() + " to " + square + "\n");
						initial_position.setPosition(p,  null,  square);
					}
					break;
				}
			} // pt
		} // pc
		// Need to add unoccupied squares to the starting position list, to be propagated with the others
		for (int midboard = 3; midboard <=6; midboard++) {
			ArrayList<String> squares_in_row = new ArrayList<String>(this.board.getSquareLabels(midboard));
			for (String s : squares_in_row) {
				 // System.out.println("Setting: " + s + " ");
				initial_position.setPosition(null, s, s);
			}
		}
		//  System.out.println( initial_position.toString() );
		System.out.println("Exiting setPieces()");
	}
	
	public void printPositionHistory() {
		ArrayList<Integer> keys = new ArrayList<Integer>(this.position_history.keySet());
		for (Integer key : keys) {
			System.out.println("Position " + key.toString());
			this.position_history.get(key).printPosition();
			System.out.println(this.position_history.get(key).getPositionMap());
			
		}
	}
	
	/* private void movePieceToSquare(PieceColor piece_color, PieceType piece_type, String square_name) {
		Square dest = this.board.getSquare(square_name);
		for (Piece p : this.pieces_list) {
			if (p.getColor() == piece_color.toString() && p.getType() == piece_type.toString()) {
				dest.setOccupant(p);
				System.out.println("Able to movePieceToSquare("+ piece_color.toString() + " " + piece_type.toString() + " " + square_name);
			}
		}
		// Exception here
		// System.out.println("Unable to movePieceToSquare("+ piece_color.toString() + " " + piece_type.toString() + " " + square_name);
	} */
	
	public void printGame() {
		System.out.println("Board is "+this.board.toString());
	}
}	