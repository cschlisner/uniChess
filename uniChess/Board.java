package uniChess;

import java.util.List;
import java.util.ArrayList;

/**
*	An object holding a boardstate. Each Board holds an array of 64 Tiles, any of which can be occupied by a Piece.
*	A board state will never actually change. When performMove() is called a new board is generated with the new boardstate.
*	This means that all information about this board (including legal moves, piece locations, etc) is in an artificial static state.
*	<p> 
*	Since all information about the board will never change, two lists of legal moves for each player is generated on creation. 
*	These lists will be publicly acessable and unchanging so that no additional calculation will need to be done for the board. 
*/
public class Board {
	private Tile[][] state = new Tile[8][8];

	private List<Move> legalWhiteMoves;
	
	private List<Move> legalBlackMoves;


	/** Sets the orientation of the string representation of the board. */
	public static boolean reversed;

	public Board(Board other){
		for (int y = 0; y < 8; ++y)
			for (int x = 0; x < 8; ++x)
				state[y][x] = new Tile(other.getTile(x, 7-y));
	}

	public Board(){
		for (int y = 0; y < 8; ++y)
			for (int x = 0; x < 8; ++x)
				state[y][x] = new Tile(new Location(x, 7-y));

		createMaterial(Game.Color.BLACK);
		createMaterial(Game.Color.WHITE);
	}

	private void createMaterial(Game.Color color){
   		int d = (color.equals(Game.Color.BLACK))?-1:1;
        Location org = (d>0)?new Location(0,0):new Location(7,7);

        getTile(org.x+(d*0), org.y).setOccupator(new Piece(color, Game.PieceType.ROOK));
        getTile(org.x+(d*7), org.y).setOccupator(new Piece(color, Game.PieceType.ROOK));

        getTile(org.x+(d*1), org.y).setOccupator(new Piece(color, Game.PieceType.KNIGHT));
        getTile(org.x+(d*6), org.y).setOccupator(new Piece(color, Game.PieceType.KNIGHT));

       getTile(org.x+(d*2), org.y).setOccupator(new Piece(color, Game.PieceType.BISHOP));
       getTile(org.x+(d*5), org.y).setOccupator(new Piece(color, Game.PieceType.BISHOP));

        // King and queen are symmetrical
        getTile(org.x+(d*((d>0)?4:3)), org.y).setOccupator(new Piece(color, Game.PieceType.KING));
        getTile(org.x+(d*((d>0)?3:4)), org.y).setOccupator(new Piece(color, Game.PieceType.QUEEN));

        for (int i = 0; i < 8; ++i)
           getTile(i, ((d>0)?org.y+1:org.y-1)).setOccupator(new Piece(color, Game.PieceType.PAWN));
    }

    /**
    *	@return A list of this Board's Tile objects 
    */
    public List<Tile> getTileList(){
    	List<Tile> res = new ArrayList<>();
    	for (int i = 0; i < 8; ++i)
    		for (int j = 0; j < 8; ++j)
    			res.add(state[i][j]);
    	return res;
    }

    /** 
    *	Returns the Tile object at a certain location, with the bottom left corner of the board 
    *	defined as (0,0) and the top right defined as (7,7).
    *	
    *	@param l The location of the tile to return
    *	@return The tile at the specified location
    */
    public Tile getTile(Location l){
		return getTile(l.x, l.y);
	}
	
	/** 
    *	Returns the Tile object at a certain location, with the bottom left corner of the board 
    *	defined as (0,0) and the top right defined as (7,7).
    *	
    *	@param x x-coordinate of the specified Tile
    *	@param y y-coordinate of the specified Tile 
    *	@return The Tile at the specified location
    */
	public Tile getTile(int x, int y){
		return state[7-y][x];
	}

	/** 
    *	Returns a two-dimensional array of Tile objects wherein array[0][0] is the top left corner
    *	of the board and array[7][7] is the bottom right.
    *	
    *	@return The array of Tile objects
    */
	public Tile[][] getBoardState(){
		return state;
	}

	private String COLDIM = "\033[2m", INVCOL="\033[7m", TERMCOL = "\033[0m";

	private String writeColumnLabels(int max, boolean reversed){
		StringBuilder res = new StringBuilder();
		res.append(" ");
		for (int x = 0; x<9; ++x){
			if (x>0) res.append(COLDIM+(" ABCDEFGH".charAt((reversed)?9-x:x)));
			for (int k=0;k<(max-1);++k)	
				res.append(" ");
		}
		res.append(TERMCOL+"\n");
		return res.toString();
	}

	private static <T> int findMaxLen(T[][] arr){
		int max=0;
		for (T[] row : arr)
			for (T el : row)
		 max = (String.valueOf(el).length() > max)?String.valueOf(el).length():max;
		return max;
    }

	private String getBoardString(){
		StringBuilder res = new StringBuilder();
		
		int max = findMaxLen(getBoardState());
		int y = 8;
		res.append("\n"+writeColumnLabels(max, reversed)+"\n");
		if (!reversed){
			for (Board.Tile[] row : getBoardState()){
				res.append(COLDIM+y+TERMCOL+" ");
				for (Board.Tile el : row){
					res.append(el);
					for (int k=0;k<((max-String.valueOf(el).length()));++k)	
						res.append("  ");
				}
				res.append("  "+COLDIM+(y--)+TERMCOL+"\n");
			}
		} else {
			for (int i = getBoardState().length-1; i >= 0; --i){
				res.append(y-i);
				for (int j = getBoardState()[0].length-1; j >= 0; --j){
					res.append(getBoardState()[i][j]);
					for (int k=0;k<((max-String.valueOf(getBoardState()[i][j]).length()));++k)	
						res.append(" ");
				}
				res.append(" "+COLDIM+(y-i)+TERMCOL+"\n");
			}
		}
		res.append("\n"+writeColumnLabels(max, reversed));

		return res.toString();
	}

	/**
	*	Returns whether or not a line of sight between two Locations is 'clear' 
	*	(has no Pieces between them) in the up, down, left, right directions.
	*
	*	@param a The first location
	*	@param b The second location
	*	@return Whether the cardinal line of sight between two tiles contains no pieces
	*/
	public boolean cardinalLineOfSightClear(Location a, Location b){
		int xDiff = b.x-a.x;
		int yDiff = b.y-a.y;
		
		// v (vector) = x or y direction | x = false | y = true
		boolean v;
		
		if (yDiff == 0 ^ xDiff == 0) 
			v = (xDiff==0);
		else return false;

		int dir = (((v)?b.y:b.x) < ((v)?a.y:a.x))?1:-1;
		for (int i = ((v)?b.y:b.x)+dir; i != ((v)?a.y:a.x); i += dir){
			if (getTile((v)?a.x:i, (v)?i:a.y).occupator != null)
				return false;
		}
		return true;
	}

	/**
	*	Returns whether or not a line of sight between two Locations is 'clear' 
	*	(has no Pieces between them) in the diagonal directions.
	*
	*	@param a The first location
	*	@param b The second location
	*	@return Whether the diagonal line of sight between two tiles contains no pieces
	*/
	public boolean diagonalLineOfSightClear(Location a, Location b){
		int xDiff = b.x-a.x;
		int yDiff = b.y-a.y;

		// if (xDiff + yDiff == 0) then x and y are of opposite signs, check y = -x diagonal
		if (yDiff == 0 || xDiff == 0) return false;

		int xDir = (xDiff>0)?-1:1;
		int yDir = (yDiff>0)?-1:1;

		if (Math.abs(xDiff) != Math.abs(yDiff)) 
			return false;

		for (int x = b.x+xDir, y = b.y+yDir; x != a.x; x+=xDir, y+=yDir){
			if (getTile(x, y).occupator != null)
				return false;
		}
		return true;
	}


	/**
	*	Returns the distance from a given location to the king of the given color
	*
	*	@param color The color of the king to get the distance for
	*	@param locale The location to calculate the distance for
	*	@return The distance from the given location to the king of the given color
	*/
	public double getDistanceFromKing(Game.Color color, Location locale){
		Location kingLoc = null;
		for (Tile t : getTileList()){
			if (t.getOccupator()!=null && t.getOccupator().type.equals(Game.PieceType.KING) && t.getOccupator().color.equals(color)){
				kingLoc = t.getLocale();
				break;
			}
		}

		return getDistanceFromLocation(locale, kingLoc);
	}	

	/**
	*	Returns the net distance from one location to anoter location on the board
	*	
	*	@param a The original location
	*	@param b The destination
	*	@return The net distance between the two locations
	*/
	public double getDistanceFromLocation(Location a, Location b){
		return Math.sqrt(Math.pow((double)Math.abs(a.x-b.x), 2) + Math.pow((double)Math.abs(a.y-b.y), 2));
	}

	/**
	*	Determines whether a given move is valid according to the move's origin piece's defined move set.
	* 
	*	@return Whether the move is valid according to the relevant piece's move set 
	*/
	public boolean isValidMove(Move move){
		Piece movingPiece = getTile(move.origin).getOccupator();

		if (movingPiece == null || !getTile(move.destination).available(movingPiece.color) || move.origin.equals(move.destination))
			return false;
		
		int direction = movingPiece.color.equals(Game.Color.WHITE) ? 1 : -1;

		// delta x and y
		int dx = move.destination.x - move.origin.x;
		int dy = direction * (move.destination.y - move.origin.y);

		boolean enemy = getTile(move.destination).getOccupator() != null;
		
		switch (movingPiece.type){
			case PAWN:
				Piece enpasse = (dy == 1 && (dy + dx == 0 || dy + dx == 2)) ? getTile(move.origin.x+dx, move.origin.y).getOccupator() : null;
				move.PROMOTION = (move.destination.y == (movingPiece.color.equals(Game.Color.WHITE) ? 7 : 0));
				if ((dy == 1 && dx == 0 && !enemy)
					|| (movingPiece.moves.size()==0 && dy == 2 && dx == 0 && cardinalLineOfSightClear(move.origin, move.destination) && !enemy) 
					|| (dy == 1 && (dy + dx == 0 || dy + dx == 2) && enemy))
					return true;
				else if (!enemy &&
							enpasse != null &&
							getTile(move.origin.x+dx, move.origin.y).available(movingPiece.color) && 
							enpasse.ofType(movingPiece.type) &&
							enpasse.moves.size() == 1){
					move.ENPASSE = true;
					return true;
				}
				return false;
			
			case ROOK:
				return (cardinalLineOfSightClear(move.origin, move.destination));
			
			case KNIGHT:
				return (dx != 0 && dy != 0 && Math.abs(dx) + Math.abs(dy) == 3);
			
			case BISHOP:
				return (diagonalLineOfSightClear(move.origin, move.destination));
			
			case QUEEN:
				return (diagonalLineOfSightClear(move.origin, move.destination))
						^ (cardinalLineOfSightClear(move.origin, move.destination));
			
			case KING:	
				if (movingPiece.moves.size() == 0 && Math.abs(dx) == 2 && dy == 0){
					if (cardinalLineOfSightClear(move.origin, new Location(move.origin.x+dx, move.origin.y))){ // only checks if line of sight is clear, still need to check if every square is uncheckable
						if (dx > 0){
							Piece castleRook = getTile(move.origin.x+3, move.origin.y).getOccupator();
							if (castleRook != null && castleRook.moves.size()==0)
								move.KCASTLE = true;
						}
						else {
							Piece castleRook = getTile(move.origin.x-4, move.origin.y).getOccupator();
							if (castleRook != null && castleRook.moves.size()==0)
								move.QCASTLE = true;
						}
						return true;
					}
				}
				return (Math.abs(dx) <= 1 && Math.abs(dy) <= 1);
		}
		return false;
	}

	/**
	*	Gets a list of all valid moves for all pieces of a given color
	* 
	*	@param c The color to gather moves for
	*	@return The list of moves
	*/
	public List<Move> getValidMoves(Game.Color c){
		List<Move> moves = new ArrayList<>();
		for (Tile t : getTileList()){
            if (!t.available(c)){
            	Piece p = t.getOccupator();
            	for (int i = 0; i < 8; ++i){
            		for (int j = 0; j < 8; ++j){
            			Move m = new Move(t.getLocale(), new Location(i, j), this);
            			if (isValidMove(m)){
            				moves.add(m);
            			}
            		}
            	}
            }
		}

		return moves;
	}

	/**
	*	Determines whether a given player on a given board holds check.
	*	
	*	@param board The board to check on 
	*	@param c The color of player to check for
	*	@return Whether the player has check 	
	*/
	public static boolean playerHasCheck(Board board, Game.Color c){
		Piece p;
		for (Move m : board.getValidMoves(c)){
			p = board.getTile(m.destination).getOccupator();
			if (p != null && p.ofType(Game.PieceType.KING))
				return true;
		}
		return false;
	}

	public static boolean playerHasCheck(Board board, Player player){
		return Board.playerHasCheck(board, player.color);
	}

	/**
	*	Gets a list of all valid moves for all pieces of a given color
	* 
	*	@param c The color to gather moves for
	*	@return The list of moves
	*/
	public List<Move> calculateLegalMoves(Game.Color c){
		List<Move> validMoves = getValidMoves(c);
		List<Move> legalMoves = new ArrayList<>();

		for (Move m : validMoves)
			if (!Board.playerHasCheck(performMove(m), Game.getOpposite(c)))
				legalMoves.add(m);

		return legalMoves;
	}

	/**
	*	Returns the list of legal moves for a given color if this method has been called before. 
	*	Otherwise, it will generate the list and return it. 
	* 
	*	@param color The color to gather moves for
	*	@return The list of moves
	*/
	public List<Move> getLegalMoves(Game.Color color){

		List<Move> legal =  ((color.equals(Game.Color.BLACK) ? legalBlackMoves : legalWhiteMoves));

		if (legal != null)
			return legal;
		
		else legal = calculateLegalMoves(color);

		return legal;
	}

	/**
	*	Returns the list of legal moves for a given player
	* 
	*	@param player The Opponent of the Player to gather moves for
	*	@return The list of moves
	*/
	public List<Move> getLegalMoves(Player player){
		return getLegalMoves(player.color);
	}

	/**
	*	Returns the list of legal moves for a given player's opponent
	* 
	*	@param player The Opponent of the Player to gather moves for
	*	@return The list of moves
	*/
	public List<Move> getOpponentLegalMoves(Player player){
		return getLegalMoves(Game.getOpposite(player.color));
	}

	/**
	*	Determines whether the king of a given color can move to a given location
	*	
	*	@param locale The location being calculated for
	*	@param color The color of the king being calculated for
	*	@return whether the king can move to the location
	*/
	public boolean isValidMoveForKing(Game.Color color, Location locale){
		for (Move m : getLegalMoves(color))
			if (m.destination.equals(locale) && getTile(m.origin).getOccupator().type.equals(Game.PieceType.KING))
				return true;
		return false;
	}



	/**
	*	Performs a given move, as well as any additional actions associated with a
	*	special move type such as En Passent moves, Castling, and Pawn promotion. 
	*	
	*	@param move The move to perform
	*	@return The new board resulting from the move
	*/
	public Board performMove(Move move){

		Board result = new Board(this);

		result.moveOccupator(move.origin, move.destination);
		if (move.ENPASSE)
			result.getTile(move.origin.x+(move.destination.x - move.origin.x), move.origin.y).setOccupator(null);
		else if (move.KCASTLE)
			result.moveOccupator(new Location(move.origin.x+3, move.origin.y), new Location(move.origin.x+1, move.origin.y));
		else if (move.QCASTLE)
			result.moveOccupator(new Location(move.origin.x-4, move.origin.y), new Location(move.origin.x-1, move.origin.y));
		else if (move.PROMOTION)
			result.getTile(move.destination).getOccupator().type = Game.PieceType.QUEEN;
		
		return result;
	}

	private void moveOccupator(Location a, Location b){
		getTile(b).setOccupator(getTile(a).getOccupator());

		getTile(a).getOccupator().moves.add(b);

		getTile(a).setOccupator(null);
	}

	/**
	*	Returns a String representation of the board, oriented so the current player is 
	*	on the bottom, using the Game setting for unicode.
	*	
	*	@return The string representation of this board
	*/
	@Override
	public String toString(){
		return getBoardString();
	}

	public class Tile {
		private Piece occupator = null;
		private Location locale;
		private Game.Color color;

		public Tile(Location loc){
			locale = loc;
			occupator = null;
			color = ((loc.x+loc.y)%2==0)?Game.Color.BLACK:Game.Color.WHITE;
		}

		public Tile(Tile org){
			this.locale = org.locale;
			if (org.occupator != null)
				this.setOccupator(new Piece(org.occupator));
			this.color = org.color;
		}

		/**
		*	@return The location of this Tile
		*/
		public Location getLocale(){	
			return locale;
		}
		
		/**
		*	@return The occupator of this Tile
		*/
		public Piece getOccupator(){
			return occupator;
		}

		/**
		*	Returns whether this tile contains no piece or contains a piece with a color
		*	not equal to the given color. In other words, it returns whether or not a 
		*	Piece of the given color can move to this Tile.
		*	
		*	@param c The color to use
		*	@return Whether a piece of the given color can move to this tile.
		*/
		public boolean available(Game.Color c){
			return (occupator == null || !occupator.color.equals(c));
		}


		/**
		*	Sets the occupator of this Tile to a Piece
		*
		*	@param p The piece to use as the new occupator
		*/
		public void setOccupator(Piece p){
			occupator = p;
		}

		/**
		*	Returns a String representation of this Tile, including the 'color' of the tile and 
		*	any occupants (using the Game setting for unicode).
		*/
		@Override
		public String toString(){
			return " "+(((occupator!=null)?occupator.getSymbol():(color.equals(Game.Color.BLACK))?"\u00B7":" "))+" ";
		}
	}
}