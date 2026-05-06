package HelloChess;

import java.util.*;



public class Position {
	// private ArrayList<Piece> pieces_list = new ArrayList<Piece>();
	
	private final int positionID; // move number within the game
	private Boolean committed; // set TRUE when no more changes allowed to position_map
	private HashMap<String, Piece> position_map = new HashMap<String, Piece>(); // square_name as key
	private ArrayList<String> squaresList;
	private Grid board = new Grid(); 
	
	public Position(int pos_num, Grid game_board) {
		this.positionID = pos_num;
		this.committed = false;
		this.board = game_board;
		this.mapGridForPosition();
		// this.updateBoardwithPosition();
	}
	
	public Position(int pos_num, ArrayList<String> labels) {
		positionID = pos_num;
		committed = false;
		squaresList = labels;
		mapGridForPosition();
		// this.updateBoardwithPosition();
	}
	
	/* Object Copy taken from https://stackoverflow.com/questions/64036/how-do-you-make-a-deep-copy-of-an-object:
	 * "In general, it is best practice to write your own clone methods for each class of an object in the object graph 
	 * needing cloning." Or, use copy constructors, as I have here. Or use GSon to serialize/deserialize the data
	 * into the new object.
	
	public Position (Position source) {
		this.positionID = source.positionID + 1;
		this.committed = false;
		this.board = source.board;
		this.position_map = source.position_map;
	}
	 **/
	
	private void mapGridForPosition() {
		for (Integer row = 8; row>=1; row--) {
			// ArrayList<String> squaresList = this.board.getSquareLabels();
			for (String label : this.squaresList){
				// System.out.println("Initializing " + label + "\n");
				position_map.put(label, null);
			}
		}
	}
	
	public int getPositionID() {
		return this.positionID;
	}
	
	public Boolean setPosition(Piece piece, String start_position, String end_position) {
		if (!committed){
			if (position_map.get(end_position)==null) { // there's no piece in end_position currently
				position_map.replace(end_position, piece);
				// System.out.println("Removing " + piece.getName() + " at " + start_position);
				String to_be_removed = null;
				if (position_map.get(start_position)!=null) {
					to_be_removed = position_map.get(start_position).getName();
				}
				// System.out.println("  Piece in position map before remove: " + to_be_removed);
				position_map.replace(start_position, null);
				// System.out.println("  Piece in position map after remove: " + position_map.get(start_position));
			}
			return true;
		} else {
			System.out.println("setPosition failure");
			return false;
		}
	}
	
	public HashMap<String, Piece> getPositionMap() {
		// return this.position_map;
		HashMap<String, Piece> the_map = new HashMap<String, Piece>();
		ArrayList<String> square_labels = new ArrayList<String>(position_map.keySet());
		for (String label : square_labels) {
			the_map.put(label, position_map.get(label));
		}
		return the_map;
	}
	
	// TODO: Change this to use CLONE() or like getPositionMap()
	public void setPositionMap(HashMap<String, Piece> target_map) {
		this.position_map = target_map;
	}
	
	Piece get_piece_at_square(String target_square) {
		if (position_map.containsKey(target_square)) {
			return position_map.get(target_square);
		} else {
			return null;
		}
	}
	
	public void printPosition() {
		// Can't use board for this because board is live object - have to print each position at a time
		// i.e. enhance position map
		System.out.println("In PositionID " + getPositionID());
		System.out.println(this.toString());
	}
	
	// TODO: This doesn't actually sort the collection
	private HashMap<String, Piece> sortSquaresInPositionMap(HashMap<String, Piece> position_map) {
		HashMap<String, Piece> sorted_map = new HashMap<String, Piece>();
		char[] files = {'a','b','c','d','e','f','g','h'};
		for (int row=0; row<8; row++) {
			for (int column=0; column<8; column++) {
				String the_square = String.valueOf(files[column]) + String.valueOf(row);
				sorted_map.put(the_square, position_map.get(the_square));
			}
		}
		return sorted_map;
	}
	
	public String toString() {
		String retval = "\n";
		char[] files = {'a','b','c','d','e','f','g','h'};
		for (int row=1; row<=8; row++) {
			for (int column=0; column<8; column++) {
				// String the_square = String.valueOf(row) + String.valueOf(files[column]);
				String the_square = String.valueOf(files[column]) + String.valueOf(row);
				retval = retval + " " + the_square;
				if (position_map.get(the_square)!=null) {
					// String piece_at_square = sorted_map.get(square).getName();
					// System.out.println("PIECE AT " + square + ": " + piece_at_square);
					retval = retval + " " + position_map.get(the_square).getName() + "\n";
				} else {
					retval = retval + "\n";
				}
			}
		}
		/*
		HashMap<String, Piece> sorted_map = this.sortSquaresInPositionMap(position_map);
		for (String square : sorted_map.keySet()) {
			retval = retval + " " + square;
			if (sorted_map.get(square)!=null) {
				// String piece_at_square = sorted_map.get(square).getName();
				// System.out.println("PIECE AT " + square + ": " + piece_at_square);
				retval = retval + " " + sorted_map.get(square).getName() + "\n";
			} else {
				retval = retval + "\n";
			}
		}
		*/
		return retval;
	}
	
}
