package HelloJosh;

import java.util.*;

public class Grid {
	private Integer width = 8;
	private Integer height = 8;
	private Integer size = width * height; 
	
	private HashMap<Integer, ArrayList<Square>> board = new HashMap<Integer, ArrayList<Square>>();
	
	public Grid() {
		this.createBoard();
	}
	
	private void createBoard(){
		for (int rank=1; rank<=height; rank++) {
			ArrayList<Square> row = new ArrayList<Square>();
			PieceColor squareColor;	
			for (int file=1; file<=width; file++) {
				if (rank%2>0) {
					if (file%2>0) {
						squareColor = PieceColor.BLACK;
					} else {squareColor = PieceColor.WHITE; }
				} else {
					if (file%2>0) {
						squareColor = PieceColor.WHITE;
					} else {squareColor = PieceColor.BLACK; }
				}	
				char fileLabel = '\0';
				switch (file) {
					case 1: fileLabel = 'a'; 
					break;
					case 2: fileLabel = 'b'; 
					break; 
					case 3: fileLabel = 'c'; 
					break; 
					case 4: fileLabel = 'd'; 
					break; 
					case 5: fileLabel = 'e'; 
					break; 
					case 6: fileLabel = 'f'; 
					break; 
					case 7: fileLabel = 'g'; 
					break; 
					case 8: fileLabel = 'h'; 
					break;
				}
				Square square = new Square(rank, fileLabel, squareColor );
				row.add(file-1, square);
			}
			board.put(rank, row);
		}
	}
	
	public Integer getSize() {
		return size;
	}
	
	public HashMap<Integer, ArrayList<Square>> getBoard(){
		return board;
	}
	
	public ArrayList<String> getSquareLabels(){
		ArrayList<String> square_labels = new ArrayList<String>();
		for (Integer row = 8; row>=1; row--) {
			ArrayList<Square> squaresList = this.board.get(row);
			for (Square s : squaresList) {
					square_labels.add(s.getSquareName());
			}
		}
		return square_labels;
	}
	
	public ArrayList<String> getSquareLabels(int row){
		ArrayList<String> square_labels = new ArrayList<String>();
		ArrayList<Square> squaresList = this.board.get(row);
		for (Square s : squaresList) {
				square_labels.add(s.getSquareName());
		}
		return square_labels;
	}
	
	public Square getSquare(String square_name) {
		// System.out.println("search name: " + square_name);
		String temp = Character.toString(square_name.charAt(0));
		int rank = Integer.parseUnsignedInt(temp);
		// char file = square_name.charAt(1);
		ArrayList<Square> row = this.board.get(rank);
		// ArrayList<Square> row = this.board.get(rank);
		Square retval = new Square(0, '\0', null);
		for (Square s : row) {
			// System.out.println("square name: " + s.getSquareName());
			if (s.getSquareName().equals(square_name)) {
				// System.out.println("Found a match!");
				retval = s;
			}
		}
		// exception here
		// System.out.println("Could not find square "+square_name+" on board!");
		return retval;
	}
	
	public void setSquare(String square_name, Piece new_occupant) {
		// if (new_occupant != null) {
		// System.out.println("Setting " + square_name + " to " + new_occupant.getName() + "\n");
		// }
		for (Integer row = 8; row>=1; row--) {
			ArrayList<Square> squaresList = this.board.get(row);
			for (Square s : squaresList) {
					// System.out.println( "Comparing " + s.getSquareName() + " with " + square_name );
					if (s.getSquareName() == square_name) {
						// if (new_occupant != null) {
							s.setOccupant(new_occupant);
						// }
						return;
					}
			}
		}
	}
	
	public String toString() {
		String retval = "\n";
		// for (Map.Entry<Integer, ArrayList<Square>> entry : board.entrySet()) {
		//	Integer rowID = entry.getKey();
		for (Integer row = 8; row>=1; row--) {
			retval = retval + row.toString() + ": ";
			ArrayList<Square> squaresList = this.board.get(row);
			for (Square i : squaresList){
				retval = retval + " " + i.getSquareName() + " (" + i.getColor() + ") ";
				if (i.getOccupant() != null) {
					retval = retval + i.getOccupant().getColor() + " " + i.getOccupant().getType();
				}
			}
			retval = retval + "\n";
		}
		return retval;
	}
	
}

