package uniChess;

import java.util.ArrayList;
import java.util.List;

/**
*	An object representing a chess piece. Pieces hold histories of locations they have been moved to.  
*/
public class Piece {

	/**	The Color of the Piece*/
	public int color;
	
	/**	The unicode representation of the Piece*/
	public String unicodeSymbol;

	/**	The plaintext representation of the piece*/
	public String symbol;

	/**	
	*The {@code PieceType} of this Piece
	*/
	public Piece.Type type;

	/** The material value of this piece.*/
	public double value;

	/**	The amount of times this piece has moved*/
	public int moves = 0;

	/** Indicates whether this piece is under attack*/
	public Move attackingMove = null;

	/** Whether or not this pawn can currently be captured via en passant**/
	public boolean passable = false;

	/**
	 * Creates new Piece from single char where uppercase is mapped to black pieces and lowercase
	 * is mapped to white
	 * @param character
	 */
	public static Piece synthesizePiece(char character){
		int color = Color.WHITE;
		if (Character.isUpperCase(character))
			color = Color.BLACK;
		switch (Character.toLowerCase(character)) {
			case 'p':
				return new Piece(color, Piece.Type.PAWN);
			case 'r':
				return new Piece(color, Piece.Type.ROOK);
			case 'n':
				return new Piece(color, Piece.Type.KNIGHT);
			case 'b':
				return new Piece(color, Piece.Type.BISHOP);
			case 'q':
				return new Piece(color, Piece.Type.QUEEN);
			case 'k':
				return new Piece(color, Piece.Type.KING);
			default:
				return null;
		}
	}

	public Piece(Piece other){
		this(other.color, other.type);
		this.moves = other.moves;
		this.passable = other.passable;
	}

	public Piece(int color, Piece.Type type){
		this.type = type;
		this.color = color;

		int[] unicodeChars;

		if (color == Color.BLACK || Game.useDarkChars)
			unicodeChars = new int[]{9823,9820,9822,9821,9819,9818};
		else unicodeChars = new int[]{9817,9814,9816,9815,9813,9812};

		switch(type){
			case PAWN:
				this.value = 2.0;
				this.symbol = "P";
				this.unicodeSymbol = new String(Character.toChars(unicodeChars[0]));
				break;
			case ROOK:
				this.value = 6.0;
				this.symbol = "R";
				this.unicodeSymbol = new String(Character.toChars(unicodeChars[1]));

				break;
			case KNIGHT:
				this.value = 4.0;
				this.symbol = "N";
				this.unicodeSymbol = new String(Character.toChars(unicodeChars[2]));

				break;
			case BISHOP:
				this.value = 4.0;
				this.symbol = "B";
				this.unicodeSymbol = new String(Character.toChars(unicodeChars[3]));
				break;
			case QUEEN:
				this.value = 10.0;
				this.symbol = "Q";
				this.unicodeSymbol = new String(Character.toChars(unicodeChars[4]));

				break;
			case KING:
				this.value = 15.0;
				this.symbol = "K";
				this.unicodeSymbol = new String(Character.toChars(unicodeChars[5]));

				break;
		}
		if (color == Color.WHITE)
			this.symbol = this.symbol.toLowerCase();
	}

	/**
	*	Determines whether or not the piece is of Piece.Type t
	*
	*	@param t The type to check
	*	@return Whether the piece has a type of t
	*/
	public boolean ofType(Piece.Type t){
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









	/* 				BYTE-BOARD REWRITE */

	public enum Type {PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING, NONE}

	public static final byte NONE = 0x0;

	public static final byte PAWN = 0x1;
	public static final byte ROOK = 0x2;
	public static final byte KING = 0x3;
	public static final byte QUEEN = 0x4;
	public static final byte BISHOP = 0x5;
	public static final byte KNIGHT = 0x6;

	public static final byte BLACK = 0x8;

	// White pieces are 0x1 - 0x6 = default piece values
	// Black pieces are PAWN|BLACK - KNIGHT|BLACK = <value> | 0x8 = <value>|BLACK


	public static byte synth(char character){
		byte p;
		switch (Character.toLowerCase(character)) {
			case 'p':
				p = PAWN;
				break;
			case 'r':
				p = ROOK;
				break;
			case 'n':
				p = KNIGHT;
				break;
			case 'b':
				p = BISHOP;
				break;
			case 'q':
				p = QUEEN;
				break;
			case 'k':
				p = KING;
				break;
			default:
				return NONE;
		}
		return Character.isUpperCase(character) ? blk(p) : p;
	}

	public static byte blk(byte b){
		return (byte)(b|BLACK);
	}
	public static boolean isw(byte b){
		return b < 0x7;
	}
	public static boolean isb(byte b){
		return b >= 0x8;
	}
	public static int color(byte b){
		return b < 0x7 ? Color.WHITE : Color.BLACK;
	}

	public static int value(byte b){
		switch (b){
			case PAWN:
			case BLACK|PAWN :
				return 2;
			case KNIGHT:
			case BLACK|KNIGHT :
				return 4;
			case BISHOP:
			case BLACK|BISHOP :
				return 4;
			case ROOK:
			case BLACK|ROOK :
				return 6;
			case QUEEN:
			case BLACK|QUEEN :
				return 10;
			case KING:
			case BLACK|KING :
				return 15;
			default:
				return 0;
		}
	}
	public static Piece.Type type(byte b){
		switch (b){
			case PAWN:
			case BLACK|PAWN :
				return Type.PAWN;
			case KNIGHT:
			case BLACK|KNIGHT :
				return Type.KNIGHT;
			case BISHOP:
			case BLACK|BISHOP :
				return Type.BISHOP;
			case ROOK:
			case BLACK|ROOK :
				return Type.ROOK;
			case QUEEN:
			case BLACK|QUEEN :
				return Type.QUEEN;
			case KING:
			case BLACK|KING :
				return Type.KING;
			default:
				return Type.NONE;
		}
	}

	public static String symbol(byte b){
		return symbol(b, true);
	}
	public static String symbol(byte b, boolean u){
		switch (b){
			case PAWN:
				return u?"\u2659":"p";
			case BLACK|PAWN:
				return u?"\u265F":"P";
			case KNIGHT:
				return u?"\u2658":"n";
			case BLACK|KNIGHT :
				return u?"\u265E":"N";
			case BISHOP:
				return u?"\u2657":"b";
			case BLACK|BISHOP :
				return u?"\u265D":"B";
			case ROOK:
				return u?"\u2656":"r";
			case BLACK|ROOK :
				return u?"\u265C":"R";
			case QUEEN:
				return u?"\u2655":"q";
			case BLACK|QUEEN :
				return u?"\u265B":"Q";
			case KING:
				return u?"\u2654":"k";
			case BLACK|KING :
				return u?"\u265A":"K";
			default:
				return " ";
		}
	}

	public static int dir(byte b){
		return isw(b) ? 1 : -1;
	}
}