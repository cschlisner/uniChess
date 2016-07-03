package uniChess;

import java.util.List;
import java.util.ArrayList;

/**
*	An object representing a replacement of one Tile in a Board object with another.
*/
public class Move {
    /**	Boolean flag for special move type.*/
	public boolean ENPASSE, QCASTLE, KCASTLE, PROMOTION, CHECKMATE;

	/**	Location of Tile containing piece to move*/
	public Location origin;

	/**	Location of Tile to move piece to*/
	public Location destination;
	
	public Board board;

	public Move(Location a, Location b, Board bo){
		origin = a;
		destination = b;
		board = bo;
	}

	@Override
	public boolean equals(Object o){
		if (o instanceof Move){
			Move om = (Move)o;
			return (om.origin.equals(this.origin) && om.destination.equals(this.destination));
		}
		return false;
	}

	/**
	*	Creates a Move instance from a valid Algebraic Noation string. 
	*
	*	@param board The board this move is to be executed on.
	*	@param color The color of the team/player the move is to be created for
	*	@param in The algebraic notation string detailing the move
	*	@return A new Move instance. 
	*	@throws GameException
	*
	**/
	public static Move parseMove(Board board, Game.Color color, String in) throws GameException{
    	if (in.length() == 2)
    		in = "p"+in;
    	
    	String[] tokens = in.split("");
		String pieceSymbol = tokens[0];

		int rank = -1, file = -1;

		//[piece][rank OR file][destination] (e.g. Bfg6)
		if (tokens.length == 4){
			char mysteryToken = tokens[1].toLowerCase().charAt(0);
			if (Character.isLetter(mysteryToken)){
				file = "abcdefgh".indexOf(mysteryToken);
				if (file < 0)
					throw new GameException(GameException.INVALID_MOVE, "Invalid move.");
			}
			else if (Character.isDigit(mysteryToken)){
				rank = Character.getNumericValue(mysteryToken)-1;
				if (rank < 0 || rank > 7)
					throw new GameException(GameException.INVALID_MOVE, "Invalid move.");
			}
		}

		// [piece][rank AND file][destination] (e.g. Bf5g6)
		if (tokens.length == 5){
			file = "abcdefgh".indexOf(tokens[1].toLowerCase().charAt(0));
			rank = Character.getNumericValue(tokens[2].charAt(0))-1;
			if ((file < 0) || (rank < 0 || rank > 7))
				throw new GameException(GameException.INVALID_MOVE, "Invalid move.");
		}

		Location dest;
		try {
			dest = new Location(tokens[tokens.length-2]+tokens[tokens.length-1]);
		} catch(ArrayIndexOutOfBoundsException e){
			throw new GameException(GameException.INVALID_MOVE, "Invalid move.");
		}
	// System.out.format("pieceSymbol: %s\ndest: %s\n", pieceSymbol, dest);

		List<Board.Tile> potentialLocations = new ArrayList<>();

		for (Board.Tile t : board.getTileList()){
			
			Piece p = t.getOccupator();

			if (p == null || !p.color.equals(color))
				continue;
			
			if (p.ofType(pieceSymbol) && board.isValidMove(new Move(t.getLocale(), dest, board))){
				if (((rank < 0 ^ t.getLocale().y == rank)) && ((file < 0 ^ t.getLocale().x == file))) // if the rank or file have been specified, add only matching pieces
					potentialLocations.add(t);
			}
		}

		if (potentialLocations.size() == 1){
			Move m = new Move(potentialLocations.get(0).getLocale(), dest, board);
			board.isValidMove(m); // adds special move flags
			return m;
		}

		if (potentialLocations.size() < 1)
			throw new GameException(GameException.INVALID_MOVE, "Invalid move.");

		if (potentialLocations.size() > 1){
			// boolean specF=false, specR=false;
			// for (Board.Tile t : potentialLocations){
			// 	for (Board.Tile u : potentialLocations){
			// 		if (t.getLocale().x != u.getLocale().x)
			// 			specF = true;
			// 		if (!specF && t.getLocale().y != u.getLocale().y)
			// 			specR = true;
			// 	}
			// }

			// String[] msg = new String[potentialLocations.size()+1];
			// int i = 0;
			// msg[i++] = "Ambiguous move. Options:";
			// for (Board.Tile t : potentialLocations){
			// 	Piece p = t.getOccupator();
			// 	msg[i++] = String.format("%d. %s%s%s%s", i-1, p.getSymbol(true), (specF?t.getLocale().toString().charAt(0):""), (specR?t.getLocale().y:""), dest.toString());
			// }
			throw new GameException(GameException.AMBIGUOUS_MOVE, "yep");
		}
		return null;
    }
	
	/**
	*	@return A full algebraic notation representation of this move, including rank and file specifiers.	
	*/
	public String getANString(){
		return String.format("%s%s%s", board.getTile(origin).getOccupator().getSymbol(false).toLowerCase(), origin, destination);
	}

    @Override
	public String toString(){
		return String.format("%s > %s", board.getTile(origin).getOccupator(), destination);
	}

}

class SmartMove extends Move implements Comparable<SmartMove>{

        public double strategicValue;
        public double tacticalValue;

        public int materialValue;

        public Game.PieceType movingPieceType;

        public SmartMove(Move move){
            super(move.origin, move.destination, move.board);

            this.materialValue = getMaterialValue();

            this.movingPieceType = this.board.getTile(origin).getOccupator().type;
        }

        private int getMaterialValue(){
            return (this.board.getTile(destination).getOccupator() != null ? this.board.getTile(destination).getOccupator().value : 0);
        }

        @Override 
        public int compareTo(SmartMove other){
            if (this.CHECKMATE) return 1;
            return (this.strategicValue > other.strategicValue) ? 1 : (this.strategicValue < other.strategicValue) ? -1 : 0;
        }
}