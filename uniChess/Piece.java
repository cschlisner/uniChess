package uniChess;

import java.util.List;
import java.util.ArrayList;

/**
*	An object representing a chess piece. Pieces hold histories of locations they have been moved to.  
*/
public class Piece {
	/**	The Color of the Piece*/
	public Game.Color color;
	
	/**	The unicode representation of the Piece*/
	public String unicodeSymbol;

	/**	The plaintext representation of the piece*/
	public String symbol;

	/**	
	*The {@code PieceType} of this Piece
	*/
	public Game.PieceType type;
	
	/** The material value of this piece.*/
	public int value;

	/**	The list of locations that this piece has moved to*/
	public List<Location> moves;

	public Piece(Piece other){
		this(other.color, other.type);
		this.moves.addAll(other.moves);
	}

	public Piece(Game.Color c, Game.PieceType type){
		this.type = type;
		this.color = c;
		this.moves = new ArrayList<>();
		switch(type){
			case PAWN:
				this.value = 2;
				this.symbol = "P";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9823:9817)); 
				break;
			case ROOK:
				this.value = 10;
				this.symbol = "R";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9820:9814));
				
				break;
			case KNIGHT:
				this.value = 6;
				this.symbol = "N";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9822:9816));
				
				break;
			case BISHOP:
				this.value = 6;
				this.symbol = "B";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9821:9815));
				
				break;
			case QUEEN:
				this.value = 18;
				this.symbol = "Q";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9819:9813));
				
				break;
			case KING:	
				this.value = 20;
				this.symbol = "K";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9818:9812));
				
				break;
		}
		if (c.equals(Game.Color.BLACK))
			this.symbol = this.symbol.toLowerCase();
	}

	/**
	*	Determines whether or not the piece is of Game.PieceType t
	*
	*	@param t The type to check
	*	@return Whether the piece has a type of t
	*/
	public boolean ofType(Game.PieceType t){
		return type.equals(t);
	}

	/**
	*	Determines whether or not the symbol associated with the piece
	*	is equal to a given character
	*
	*	@param s The character to check
	*	@return Whether the piece has a symbolic representation of s
	*/
	public boolean ofType(String s){
		return (symbol.equalsIgnoreCase(s) || unicodeSymbol.equals(s));
	}

	/**
	*	@return The symbol according to the Game unicode setting
	*/
	public String getSymbol(){
		return getSymbol(Game.unicode);
	}

	/**
	*	Returns one of the symbols associated with this piece for representation.
	*
	*	@param unicode Whether to get the unicode or the text representation
	*	@return The symbol according to the specification
	*/
	public String getSymbol(boolean unicode){
		if (unicode)
			return unicodeSymbol;
		else return symbol;
	}

	@Override
	public String toString(){
		return ((Game.unicode)?unicodeSymbol:symbol);
	}
}