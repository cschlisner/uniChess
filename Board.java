package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Board {
	private Tile[][] state = new Tile[8][8];
	public Player white, black;

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

	public void createMaterial(Game.Color color){
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

    public List<Tile> getTileList(){
    	List<Tile> res = new ArrayList<>();
    	for (int i = 0; i < 8; ++i)
    		for (int j = 0; j < 8; ++j)
    			res.add(state[i][j]);
    	return res;
    }
    public Tile getTile(Location l){
		return getTile(l.x, l.y);
	}
	public Tile getTile(int x, int y){
		return state[7-y][x];
	}
	public Tile[][] getBoardState(){
		return state;
	}
	private void writeColumnLabels(int max, boolean reversed){
		String res;
		for (int x = 0; x<9; ++x){
			if (x>0) res.append(" ABCDEFGH".charAt((reversed)?9-x:x));
			for (int k=0;k<(max-1);++k)	
				res.append(" ");
		}
		res.append("\n");
		return res;
	}
	private static <T> int findMaxLen(T[][] arr){
		int max=0;
		for (T[] row : arr)
			for (T el : row)
		 max = (String.valueOf(el).length() > max)?String.valueOf(el).length():max;
		return max;
    }

	public String getBoardString(boolean reversed){
		String res;
		
		int max = findMaxLen(getBoardState());
		int y = 8;
		res.append(writeColumnLabels(max, reversed));
		if (!reversed){
			for (Board.Tile[] row : getBoardState()){
				res.append(y);
				for (Board.Tile el : row){
					res.append(el);
					for (int k=0;k<((max-String.valueOf(el).length()));++k)	
						res.append(" ");
				}
				res.append(y--+"\n");
			}
		} else {
			for (int i = getBoardState().length-1; i >= 0; --i){
				res.append(y-i);
				for (int j = getBoardState()[0].length-1; j >= 0; --j){
					res.append(getBoardState()[i][j]);
					for (int k=0;k<((max-String.valueOf(getBoardState()[i][j]).length()));++k)	
						res.append(" ");
				}
				res.append(y-i+"\n");
			}
		}
		res.append(writeColumnLabels(max, reversed));

		return res;
	}

	// Returns whether or not a line of sight between two Locations is 'clear' 
	// (has no m.pieces between them) in the up, down, left, right directions
	//
	// v = vector = x or y | x = false | y = true
	public boolean cardinalLineOfSightClear(Location a, Location b){
		int xDiff = b.x-a.x;
		int yDiff = b.y-a.y;
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

	// Returns whether or not a line of sight between two Locations is 'clear' 
	// (has no m.pieces between them) in the up, down, left, right directions
	//
	// if (xDiff + yDiff == 0) then x and y are of opposite signs, check y = -x diagonal
	public boolean diagonalLineOfSightClear(Location a, Location b){
		int xDiff = b.x-a.x;
		int yDiff = b.y-a.y;

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
	* Determines whether a given move is valid according to the piece's defined move set.
	* This does not take Check into account or handle castling. 
	* 
	* Evaluation of legality of move will be handled in the board validation method.
	* En Passe and Castling (which require additional moves) will be handled in the move performing method.
	*
	* 
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
				Piece enpasse = (dy + dx == 0 || dy + dx == 2) ? getTile(move.origin.x+dx, move.origin.y).getOccupator() : null;
				if ((dy == 1 && dx == 0 && !enemy)
					|| (movingPiece.moves.size()==0 && dy == 2 && dx == 0 && cardinalLineOfSightClear(move.origin, move.destination)) 
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

	public List<Move> getValidMoves(Game.Color c){
		List<Move> moves = new ArrayList<>();
		for (Tile t : getTileList()){
            if (!t.available(c)){
            	Piece p = t.getOccupator();
            	for (int i = 0; i < 8; ++i){
            		for (int j = 0; j < 8; ++j){
            			Move m = new Move(t.getLocale(), new Location(i, j));
            			if (isValidMove(m)){
            				m.b = this;
            				moves.add(m);
            			}
            		}
            	}
            }
		}

		return moves;
	}

	public Board performMove(Move move){

		Board result = new Board(this);

		result.moveOccupator(move.origin, move.destination);
		if (move.ENPASSE)
			result.getTile(move.origin.x+(move.destination.x - move.origin.x), move.origin.y).setOccupator(null);
		else if (move.KCASTLE)
			result.moveOccupator(new Location(move.origin.x+3, move.origin.y), new Location(move.origin.x+1, move.origin.y));
		else if (move.QCASTLE)
			result.moveOccupator(new Location(move.origin.x-4, move.origin.y), new Location(move.origin.x-1, move.origin.y));

		return result;
	}

	public void moveOccupator(Location a, Location b){
		getTile(b).setOccupator(getTile(a).getOccupator());

		getTile(a).getOccupator().moves.add(b);

		getTile(a).setOccupator(null);
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

		public Location getLocale(){	
			return locale;
		}
		public Piece getOccupator(){
			return occupator;
		}

		public boolean available(Game.Color c){
			return (occupator == null || !occupator.color.equals(c));
		}

		public void setOccupator(Piece p){
			occupator = p;
		}

		@Override
		public String toString(){
			return "|"+(((occupator!=null)?occupator.getSymbol(true):(color.equals(Game.Color.BLACK))?"\u00b7":" "))+"|";
		}
	}
}