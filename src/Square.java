package HelloJosh;

public class Square {
	private final int sRank;
	private final char sFile;
	private final String sName;
	private final PieceColor sColor;
	private Piece sOccupant;
	
	public Square(int pRank, char pFile, PieceColor pColor) {
		this.sRank = pRank;
		this.sFile = pFile;
		this.sName = Character.toString(pFile) + Integer.toString(pRank);
		// System.out.println("BLESSing Square " + this.sName + "\n");
		this.sColor = pColor;
	}
	
	public int getRank() {
		return this.sRank;
	}
	
	public char getFile() {
		return this.sFile;
	}
	
	public PieceColor getColor() {
		return this.sColor;
	}
	
	public String getSquareName() {
		return this.sName;
	}
	
	public Piece getOccupant() {
		return this.sOccupant;
	}
	
	public void setOccupant(Piece pOccupant) {
		this.sOccupant = pOccupant;
		if (pOccupant!=null) {
			// we set square name pointer in Piece when it gets added to Square
			sOccupant.setSquare(sName);
		}
		// System.out.println("Piece " + pOccupant.getName() + " moved to Square " + this.sName + "\n");
	}
}
