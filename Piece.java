package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Piece implements Cloneable{

	public static final int AVERAGE_PIECE_VAL = ((8*2)+(4*6)+(18)+(20))/16;

	private Game game;
	private Board board;
	private Team team;
	private Game.Color color;
	
	private boolean killed = false;
	private String name;
	private String unicodeSymbol;
	private String symbol;
	private Game.PieceType type;
	private Location startPoint, location;
	
	private MoveSet moveSet;
	
	private Piece qscRook, kscRook;

	public Piece defending;
	public int attackerCount, defenderCount;
	public int value;

	public List<Piece> attackedPieces, protectedPieces;
	private List<Move> moveList;

	public Piece(Game g, Board b, Team tm, Game.PieceType type, Game.Color c, int d, Location l){
		game = g;
    	board = b;
		this.team = tm;
		this.type = type;
		this.color = c;
		this.startPoint = l;
		this.location = startPoint;
		attackedPieces = new ArrayList<Piece>();
		protectedPieces = new ArrayList<Piece>();
		moveList = new ArrayList<Move>();

		switch(type){
			case PAWN:
				this.name = "Pawn";
				this.value = 2;
				this.symbol = "P";
				this.unicodeSymbol = new String(Character.toChars(c.equals(Game.Color.BLACK)?9823:9817)); 
				this.moveSet = new MoveSet(game, d, this){
					@Override
					public boolean isValidMove(Location m){
						Board.Tile t = board.getTile(m);
						int yDiff = (this.dir>0)?m.y-piece.location.y:piece.location.y-m.y; // v this.direction matters
						int xDiff = Math.abs(m.x-piece.location.x);                    // h this.direction doesn't

						if (yDiff == 1 && xDiff==1 && t.containsFriendly(piece))
								if (!protectedPieces.contains(t.getOccupator())) piece.protectedPieces.add(t.getOccupator());
						
						if (!m.equals(piece.location) && t.available(piece) && (!team.inCheck() || (team.inCheck() && team.canMoveWhenChecked(new Move(game, game.getBoard(), piece, t.getLocale()))))){
							if (yDiff<(piece.location.equals(piece.startPoint)?3:2) && yDiff > 0){
								
								boolean canAttack = (yDiff == 1 && xDiff==1 && t.containsEnemy(piece.getTeam()));
								if ((yDiff==2 && xDiff==0 && t.getOccupator()==null) || (yDiff == 1 && xDiff==0 && t.getOccupator()==null) || canAttack){ 
										if (canAttack)
											piece.attackedPieces.add(t.getOccupator());
										return true;	
								}
							}
							return false;
						} 
						return false;
					}
				};
				break;
			case ROOK:
				this.name = "Rook";
				this.value = 10;
				this.symbol = "R";
				this.unicodeSymbol = new String(Character.toChars(c.equals(Game.Color.BLACK)?9820:9814));
				this.moveSet = new MoveSet(game, d, this){
					@Override
					public boolean isValidMove(Location m){
						Board.Tile t = board.getTile(m);
						if (!m.equals(piece.location) && board.cardinalLineOfSightClear(piece.location, m) && t.containsFriendly(piece))
								if (!protectedPieces.contains(t.getOccupator())) 
									piece.protectedPieces.add(t.getOccupator());

						if (!m.equals(piece.location) && t.available(piece) && (!team.inCheck() || (team.inCheck() && team.canMoveWhenChecked(new Move(game, game.getBoard(), piece, t.getLocale()))))){

							if (t.available(piece) && board.cardinalLineOfSightClear(piece.location, m)){
								if (t.containsEnemy(piece.getTeam()))
									piece.attackedPieces.add(t.getOccupator());
								return true;
							}
						}
						return false;
					}
				};
				break;
			case KNIGHT:
				this.name = "Knight";
				this.value = 6;
				this.symbol = "N";
				this.unicodeSymbol = new String(Character.toChars(c.equals(Game.Color.BLACK)?9822:9816));
				this.moveSet = new MoveSet(game, d, this){
					@Override
					public boolean isValidMove(Location m){
						t = board.getTile(m);
						int yDiff = Math.abs(m.y-piece.location.y);
						int xDiff = Math.abs(m.x-piece.location.x);

						if (!m.equals(piece.location) && (((xDiff==2 && yDiff==1) || (xDiff==1 && yDiff==2))) && t.containsFriendly(piece))
								if (!protectedPieces.contains(t.getOccupator())) 
									piece.protectedPieces.add(t.getOccupator());
						
						if (!m.equals(piece.location) && t.available(piece) && (!team.inCheck() || (team.inCheck() && team.canMoveWhenChecked(new Move(game, game.getBoard(), piece, t.getLocale()))))){
							if (((xDiff==2 && yDiff==1) || (xDiff==1 && yDiff==2))){
								if (t.containsEnemy(piece.getTeam()))
									piece.attackedPieces.add(t.getOccupator());
								return true;
							}

						}
						return false;
					}
				};
				break;
			case BISHOP:
				this.name = "Bishop";
				this.value = 6;
				this.symbol = "B";
				this.unicodeSymbol = new String(Character.toChars(c.equals(Game.Color.BLACK)?9821:9815));
				this.moveSet = new MoveSet(game, d, this){
					@Override
					public boolean isValidMove(Location m){
						t = board.getTile(m);

						if (!m.equals(piece.location) && board.diagonalLineOfSightClear(piece.location, m) && t.containsFriendly(piece))
								if (!protectedPieces.contains(t.getOccupator())) piece.protectedPieces.add(t.getOccupator());

						if (!m.equals(piece.location) && t.available(piece) && (!team.inCheck() || (team.inCheck() && team.canMoveWhenChecked(new Move(game, game.getBoard(), piece, t.getLocale()))))){
							if (board.diagonalLineOfSightClear(piece.location, m)){
								if (t.containsEnemy(piece.getTeam()))
									piece.attackedPieces.add(t.getOccupator());
								return true;
							}
						}
						return false;
					}
				};
				break;
			case QUEEN:
				this.name = "Queen";
				this.value = 18;
				this.symbol = "Q";
				this.unicodeSymbol = new String(Character.toChars(c.equals(Game.Color.BLACK)?9819:9813));
				this.moveSet = new MoveSet(game, d, this){
					@Override
					public boolean isValidMove(Location m){
						Board.Tile t = board.getTile(m);

						if (!m.equals(piece.location) && (board.diagonalLineOfSightClear(piece.location, m) || board.cardinalLineOfSightClear(piece.location, m)) 
							&& t.containsFriendly(piece))
								if (!protectedPieces.contains(t.getOccupator())) piece.protectedPieces.add(t.getOccupator());

						if (!m.equals(piece.location) && t.available(piece) && (!team.inCheck() || (team.inCheck() && team.canMoveWhenChecked(new Move(game, game.getBoard(), piece, t.getLocale()))))){
							if (board.diagonalLineOfSightClear(piece.location, m) || board.cardinalLineOfSightClear(piece.location, m)){
								if (t.containsEnemy(piece.getTeam()))
									piece.attackedPieces.add(t.getOccupator());
								return true;
							}
						}
						return false;
					}
				};
				break;
			case KING:	
				this.name = "King";
				this.value = 20;
				this.symbol = "K";
				this.unicodeSymbol = new String(Character.toChars(c.equals(Game.Color.BLACK)?9818:9812));
				this.moveSet = new MoveSet(game, d, this){
					@Override
					public boolean isValidMove(Location m){
						Board.Tile t = board.getTile(m);
						int yDiff = Math.abs(m.y-piece.location.y);
						int xDiff = Math.abs(m.x-piece.location.x);

						if (!m.equals(piece.location) && ((xDiff<2&&yDiff<2) && (board.diagonalLineOfSightClear(piece.location, m) || board.cardinalLineOfSightClear(piece.location, m)))
							 && t.containsFriendly(piece))
								if (!protectedPieces.contains(t.getOccupator())) piece.protectedPieces.add(t.getOccupator());

						if (!m.equals(piece.location) && t.available(piece)){
							
							if (t.containsEnemy(piece.getTeam()) && t.getOccupator().defenderCount > 0)
								return false;

							// this will put the King into check, thus is not legal move
							for (Piece p : piece.getOpponent().getPieceSet())
									if ((p.ofType(Game.PieceType.KING) && Math.abs(m.y-p.location.y)==1 && Math.abs(m.y-p.location.y)==1) // if the king's isValidMove() is called, infinite loop will occur, so we check for it like this
										^ (p.type.equals(Game.PieceType.PAWN) && Math.abs(m.x-p.location.x)==1 && m.y-p.location.y==-dir) // pieces can move in front of a pawn even though it's a valid pawn move
										^ (!p.ofType(Game.PieceType.KING) && !p.ofType(Game.PieceType.PAWN) && p.moveSet.isValidMove(m))) 
										return false;
							
	
							// castling moves
							if (movesMade.isEmpty() && m.y == piece.startPoint.y){
								Location kingSideCastle = new Location(piece.location.x+2, m.y); // kings are symmetrical across center horizontal so direction doesn't matter
								Location queenSideCastle = new Location(piece.location.x-2, m.y);
	
								if (m.equals(kingSideCastle)){
									Piece tmp = board.getTile(7, m.y).getOccupator();
									piece.kscRook = (tmp!=null && tmp.ofType(Game.PieceType.ROOK) && board.cardinalLineOfSightClear(piece.location, tmp.getLocation()) && tmp.getMovesMade().isEmpty())?tmp:null;
								}
								else if(m.equals(queenSideCastle)){
									Piece tmp = board.getTile(0, m.y).getOccupator();
									piece.qscRook = (tmp!=null && tmp.ofType(Game.PieceType.ROOK) && board.cardinalLineOfSightClear(piece.location, tmp.getLocation()) && tmp.getMovesMade().isEmpty())?tmp:null;
								}
								if (kscRook!=null || qscRook!=null)
									return true;
							}
	
							if ((xDiff<2&&yDiff<2) && (board.diagonalLineOfSightClear(piece.location, m) || board.cardinalLineOfSightClear(piece.location, m))){
								if (t.containsEnemy(piece.getTeam()))
									piece.attackedPieces.add(t.getOccupator());
								return true;
							}
						}
						return false;
					}
				};
				break;
		}
	}

	public void update(){
		attackedPieces.clear();
		protectedPieces.clear();
		moveList = moveSet.getValidMoves();

		attackerCount = 0;
		for (Piece p : getOpponent().getPieceSet())
			if (!p.isDead() && p.attackedPieces.contains(this))
				++attackerCount;

		defenderCount = 0;
		for (Piece p : team.getPieceSet()){
			if (attackerCount>0 && !p.isDead() && p.protectedPieces.contains(this)){
				++defenderCount;
				p.defending = this;
				continue;
			}
			p.defending = null;
		}
	}

	public boolean moveTo(Location dest){
		if (!killed && moveSet.isValidMove(dest)){
			Location prevLoc = location;
			boolean r = board.getTile(dest).attemptMove(this);
			if (r){
				if (kscRook!=null) {
					// need to force this move because it's not valid, technically
					Location newRookLoc = new Location(prevLoc.x+(moveSet.dir), prevLoc.y);
					board.getTile(newRookLoc).attemptMove(kscRook);
					kscRook.moveSet.movesMade.add(newRookLoc);
					kscRook = null;
				}
				else if (qscRook!=null){
	 				Location newRookLoc = new Location(prevLoc.x-(moveSet.dir), prevLoc.y);
					board.getTile(newRookLoc).attemptMove(qscRook);
					qscRook.moveSet.movesMade.add(newRookLoc);
				 	qscRook = null;
				}

				// switch pawn to queen once across board
				if (ofType(Game.PieceType.PAWN) && (dest.y == ((moveSet.dir>0)?7:0))){
					kill();
					Piece newQueen = new Piece(game, board, team, Game.PieceType.QUEEN, color, moveSet.dir, dest);
					team.addToPieceSet(newQueen);
					return true;
				}

				moveSet.movesMade.add(dest);
				update();
			}
			return r;
		}
		return false;
	}

	public void setLocation(Location loc){
		board.getTile(location).setOccupator(null);
		if (loc != null){
			board.getTile(loc).setOccupator(this);
			location = loc;
		}
	}

	public boolean moveTo(int x, int y){
		return moveTo(new Location(x, y));
	}

	public void kill(){
		this.killed = true;
	    setLocation(null);
	    team.getPieceSet().remove(this);
	}

	// undoes what kill() does (used in move simulation)
	public void revive(){
		this.killed = false;
		board.getTile(location).setOccupator(this);
	    team.getPieceSet().add(this);
	}

	public boolean isDead(){
		return this.killed;
	}

	public String getName(){
		return name;
	}
	// if unicode is not specified, it uses the default game setting
	public String getSymbol(){
		return Game.unicode?unicodeSymbol:symbol;
	}
	public String getSymbol(boolean unicode){
		return unicode?unicodeSymbol:symbol;
	}
	public Location getLocation(){
		return location;
	}
	public Location getStartingPoint(){
		return startPoint;
	}
	public Team getTeam(){
		return team;
	}
	public Team getOpponent(){
		return (team.equals(game.player1.getTeam())?game.player2.getTeam():game.player1.getTeam());
	}

	public Game.PieceType getType(){
		return type;
	}

	public boolean ofType(Game.PieceType t){
		return type.equals(t);
	}

	public boolean ofType(String s){
		return (symbol.equalsIgnoreCase(s) || unicodeSymbol.equals(s));
	}

	public int getMoveSetDir(){
		return this.moveSet.dir;
	}

	public Object[] getMoves(){
		return moveList.toArray();
	}

	public List<Location> getMovesMade(){
		return moveSet.movesMade;
	}

	public List<Move> getMoveList(){
		return moveList;
	}

	public boolean canMove(Location m){
		return moveSet.isValidMove(m);
	}

	public boolean hasCheck(){
		for (Move move : moveSet.getValidMoves())
			if (move.destTile.containsEnemyOfType(getTeam(), Game.PieceType.KING))
				return true;
		return false;
	}

	/**
	* Determines whether or not this piece would hold check if the given piece
	* were moved to this location. Needed to calculate available moves for opponent team 
	* when in check.
	* 
	* @param simulatedEnemyLocation the location for the enemy piece to be simulated at
	* @return whether this piece holds check when there is an enemy piece at this location. 
	*/
	public boolean canCheck(Move enemyMove){
		return (new Simulator<Boolean>(enemyMove, this){
			@Override
			public Boolean getData(){
				return ((Piece)data[0]).hasCheck();
			}
		}).simulate();
	}

	/**
	* Returns a list of possible moves for this piece if it were to move to the selected location
	* 
	* @return list of possible moves for a simulated piece location
	*/
	public List<Move> getSimulatedMoves(Location simulatedPieceLocation){
		if (simulatedPieceLocation.equals(location)) 
			return moveList;

		//if (moveSet.isValidMove(simulatedPieceLocation)){

			Location orgLoc = this.location;
			this.location = simulatedPieceLocation;

			List<Move> potentialMoves = moveSet.getValidMoves();
			
			this.location = orgLoc;

			return potentialMoves;
		//}
		
		//return new ArrayList<Location>();
	}

	private abstract class MoveSet {
		private Game game;
		public Board board;
		Board.Tile t;
		public int dir;
		public Piece piece;
		public List<Location> movesMade = new ArrayList<Location>();

		public MoveSet(Game g, int d, Piece p){
			this.game = g;
			this.board = game.getBoard();
			this.dir = d;
			this.piece = p;
		}
		
		public abstract boolean isValidMove(Location m);

		public List<Move> getValidMoves(){
		 	List<Move> mList = new ArrayList<Move>();
			for (Board.Tile[] tr : board.getBoardState()){
				for (Board.Tile t : tr){
					if (isValidMove(t.getLocale()))
						mList.add(new Move(game, game.getBoard(), piece, t.getLocale()));
				}
			}
			return mList;
		}
	}

	@Override
	public String toString(){
		return ((Game.unicode)?unicodeSymbol:symbol)+" "+location;
	}
}