package uniChess;

import java.util.ArrayList;
import java.util.List;

/**
*	An object representing a replacement of one Tile in a Board object with another.
*/
public class Move{
    /**	Boolean flag for special move type.*/
	public boolean ENPASSE, QCASTLE, KCASTLE, PROMOTION, CHECKMATE;

	/**	Location of Tile containing piece to move*/
	public Location origin;

	/**	Location of Tile to move piece to*/
	public Location destination;
	
	public Board board, sim = null;

	public byte piece;

	/** Value of the piece this move would capture*/
    public double materialValue;


	public Move(Location a, Location b, Board bo){
		origin = a;
		destination = b;
		board = bo;
		piece = bo.getTile(origin);
	}

	public Move(Move m){
		this.ENPASSE = m.ENPASSE;
		this.QCASTLE = m.QCASTLE;
		this.KCASTLE = m.KCASTLE;
		this.PROMOTION = m.PROMOTION;
		this.CHECKMATE = m.CHECKMATE;
		this.origin = m.origin;
		this.destination = m.destination;
		this.board = m.board;
		this.sim = (m.sim!=null) ? m.sim : null;
		this.piece = m.piece;
		this.materialValue = m.materialValue;
	}

	public Board getSimulation(){
		if (sim == null) sim = new Board(board, this);
		return sim;
	}

	public boolean isSpecial(){
		return (ENPASSE || QCASTLE || KCASTLE || PROMOTION || CHECKMATE);
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
	public static Move parseMove(Board board, int color, String in) throws GameException{
    	if (in.equals("0-0"))
    		in = (color == Color.BLACK) ? "kg8":"kg1";
    	else if (in.equals("0-0-0"))
    		in = (color == Color.BLACK) ? "kc8":"kc1";
    	
    	else if (in.length() == 2)
    		in = "p"+in;
    	
    	String[] tokens = new String[in.length()];
		for (int i = 0; i < in.length(); ++i)
			tokens[i] = String.valueOf(in.charAt(i));

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

		List<Move> potentialMoves = new ArrayList<>();

		Location ploc;
		for (int y = 0; y < 8; ++y){
			for (int x = 0; x < 8; ++x){
				if (((rank < 0 ^ y == rank)) && ((file < 0 ^ x == file))) // if the rank or file have been specified, add only matching pieces
				{
					ploc = new Location(x,y);
					byte p = board.getTile(ploc);

					if (p == Piece.NONE || Piece.color(p) != color ||
							!Piece.symbol(p,false).toLowerCase().equals(pieceSymbol))
						continue;

					Move pmove = new Move(ploc, dest, board);
					if (board.isValidMove(pmove))
						potentialMoves.add(pmove);

				}
			}
		}

		if (potentialMoves.size() == 1){
			return potentialMoves.get(0);
		}

		if (potentialMoves.size() < 1)
			throw new GameException(GameException.INVALID_MOVE, "Invalid move.");

		if (potentialMoves.size() > 1){
			throw new GameException(GameException.AMBIGUOUS_MOVE, "yep");
		}
		return null;
    }
	
	/**
	*	@return A full algebraic notation representation of this move, including rank and file specifiers.	
	*/
	public String getANString(){
		if (KCASTLE) return ("0-0");
		if (QCASTLE) return ("0-0-0");
		return String.format("%s%s%s", Piece.symbol(board.getTile(origin), false).toLowerCase(), origin, destination);
	}

    @Override
	public String toString(){
		return String.format("%s > %s%s", Piece.symbol(board.getTile(origin)), (materialValue>0 ? Piece.symbol(board.getTile(destination)) : ""), destination);
	}

}