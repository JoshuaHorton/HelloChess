package HelloJosh;

import java.util.Map;
import java.util.HashMap;
import java.lang.Integer;

public class Piece {
	
	private final PieceColor pColor;
	private final PieceType pType;
	private final String pName;
	private String pSquare; // this gets set by Square object when Piece is added to it as occupant
	// private Map<Integer, Square> position_history = new HashMap<Integer, Square>(); // accumulation of past squares this piece has occupied
	
	public Piece( PieceColor color, PieceType type ) {
		this.pColor = color;
		this.pType = type;
		this.pName = color.toString() + " " + type.toString();
	}
	
	public void setSquare(String sq) {
		this.pSquare = sq;
	}
	
	public String getSquare() {
		return this.pSquare;
	}
	
	public String getColor() {
		return this.pColor.toString();
	}
	
	public String getType() {
		return this.pType.toString();
	}
	
	public String getName() {
		return this.pName;
	}

}
