package uniChess;

import java.util.List;
import java.util.ArrayList;

public class Piece {

	public static final int AVERAGE_PIECE_VAL = ((8*2)+(4*6)+(18)+(20))/16;

	public Game.Color color;
	
	public boolean killed = false;
	public String name, unicodeSymbol, symbol;
	public Game.PieceType type;
	
	public int value;
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
				this.name = "Pawn";
				this.value = 2;
				this.symbol = "P";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9823:9817)); 
				break;
			case ROOK:
				this.name = "Rook";
				this.value = 10;
				this.symbol = "R";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9820:9814));
				
				break;
			case KNIGHT:
				this.name = "Knight";
				this.value = 6;
				this.symbol = "N";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9822:9816));
				
				break;
			case BISHOP:
				this.name = "Bishop";
				this.value = 6;
				this.symbol = "B";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9821:9815));
				
				break;
			case QUEEN:
				this.name = "Queen";
				this.value = 18;
				this.symbol = "Q";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9819:9813));
				
				break;
			case KING:	
				this.name = "King";
				this.value = 20;
				this.symbol = "K";
				this.unicodeSymbol = new String(Character.toChars(color.equals(Game.Color.BLACK)?9818:9812));
				
				break;
		}
		if (c.equals(Game.Color.BLACK))
			this.symbol = this.symbol.toLowerCase();
	}

	public boolean ofType(Game.PieceType t){
		return type.equals(t);
	}

	public boolean ofType(String s){
		return (symbol.equalsIgnoreCase(s) || unicodeSymbol.equals(s));
	}

	public String getSymbol(){
		return getSymbol(Game.unicode);
	}
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