package uniChess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
*	An object holding a boardstate. Each Board holds an array of 64 Tiles, any of which can be occupied by a Piece.
*	A board state will never actually change. When performMove() is called a new board is generated with the new boardstate.
*	This means that all information about this board (including legal moves, piece locations, etc) is in an artificial static state.
*	<p> 
*	Since all information about the board will never change, two lists of legal moves for each player is generated on creation. 
*	These lists will be publicly accessible and unchanging so that no additional calculations will need to be done for the board.
*/
public class Board {

	/** Board iteration, generated from previous board - same as game move count **/
	private int iteration;

	private int blackMaterial=0;
	private int whiteMaterial=0;

    /*
        state[0][0] = upper-left tile = A8 = (0,7)
        state[7][7] = lower-right tile = H1 = (7,0)
     */
	private byte[][] state = new byte[8][8];

	private List<Location> enpassentablePawns = new ArrayList<>();

	private List<Move> legalWhiteMoves;
	private List<Move> legalBlackMoves;

	private List<Move> validWhiteMoves;
	private List<Move> validBlackMoves;

	private byte[] deathRow = new byte[0];

    public HashSet<Location> hasMoved = new HashSet<>();


	/** Sets the orientation of the string representation of the board. */
	public static boolean reversed;

	/**
	 * Creates a copy of a given board
	 * @param other Board to copy
	 */
	public Board(Board other){
		this(other, null);
	}

	/**
	 * Creates the Board resulting from a given move on a given parent board
	 * @param parent Parent Board
	 * @param move Move made
	 */
	public Board(Board parent, Move move){
		long t0 = System.currentTimeMillis();

		iteration = parent.iteration + (move==null ? 0 : 1);

		for (int y = 0; y < 8; ++y) {
			for (int x = 0; x < 8; ++x) {
				state[y][x] = parent.state[y][x];
				byte piece = state[y][x];

				if (piece != Piece.NONE){
					if (Piece.isw(piece))
						whiteMaterial += Piece.value(piece);
					else blackMaterial += Piece.value(piece);
				}
			}
		}
		this.deathRow = Arrays.copyOf(parent.deathRow, parent.deathRow.length);

		if (move != null){

			// make the move
			this.moveOccupator(move.origin, move.destination);

			// set passable status (able to be en-passanted) of pawn if it's the pawns first move
			if (Piece.type(move.piece) == Piece.Type.PAWN){
				int dx = move.destination.x - move.origin.x;
				int dy = Piece.dir(move.piece) * (move.destination.y - move.origin.y);
				if (dy == 2 && dx == 0)
				    enpassentablePawns.add(move.destination);
			}
			if (move.ENPASSE) {
				addToDeathRow(this.getTile(move.destination.x, move.origin.y));
				setTile(move.destination.x, move.origin.y, Piece.NONE);
			}
			else if (move.KCASTLE) {
			    try {
                    moveOccupator(new Location(move.origin.x + 3, move.origin.y), new Location(move.origin.x + 1, move.origin.y));
                } catch (Exception e){
                    System.out.println(parent);
                    System.out.println(move);
                    System.out.println(move.origin);
                    System.out.println(move.destination);
                    Iterator<Location> i = parent.hasMoved.iterator();
                    while (i.hasNext())
                        System.out.print(i.next());
                    System.out.println("");
                    throw e;
                }
			}
			else if (move.QCASTLE) {
				moveOccupator(new Location(move.origin.x-4, move.origin.y), new Location(move.origin.x-1, move.origin.y));
			}
			else if (move.PROMOTION) {
				setTile(move.destination, Piece.isw(move.piece)?Piece.QUEEN:Piece.blk(Piece.QUEEN));
			}

			if (move.materialValue > 0) {
				addToDeathRow(parent.getTile(move.destination));
			}
		}
		else if (iteration>0){
			// these values have already been calculated and haven't changed
			if (iteration%2==0)
				this.legalWhiteMoves = parent.getLegalMoves(Color.WHITE);
			else this.legalBlackMoves = parent.getLegalMoves(Color.BLACK);
		}

        this.hasMoved.addAll(parent.hasMoved);
        this.hasMoved.add(move.origin);

	}

	public Board(){
		iteration = 0;

		createMaterial(Color.BLACK);
		createMaterial(Color.WHITE);
	}

	public Board(String layout) throws Exception{
		if (layout.length() != 64)
			throw new Exception("NO!!!!!!!!!!");

		char[] allowed = {'.','p','b','k','n','r','q'};

		for (int i = 0; i < 64; ++i) {

			int y = i / 8, x = i <= 7 ? i : i - 8 * y;

			boolean inset = false;
			for (char a : allowed){
				if (Character.toLowerCase(layout.charAt(i)) == allowed[a]) {
					inset = true;
					break;
				}
			}
			if (!inset)
				throw new Exception("NO!!!!!!!!!!!!!!");

			if (layout.charAt(i) != '.')
				state[y][x] = Piece.synth(layout.charAt(i));
		}
	}

	public int getIteration(){
		return iteration;
	}

	public String getLayout(){
		String boardlayout = "";
		for (int i = 0; i < 64; ++i){
			int y = i/8, x = i <=7 ? i : i - 8 * y;
			if (getTile(x,y) != Piece.NONE)
				boardlayout += Piece.symbol(getTile(x,y), false);
			else boardlayout += ".";
		}
		return boardlayout;
	}

	private void createMaterial(int color){
   		int d = color;
        Location org = (d>0)?new Location(0,0):new Location(7,7);

        setTile(org.x+(d*0), org.y, d>0?Piece.ROOK:Piece.blk(Piece.ROOK));
        setTile(org.x+(d*7), org.y, d>0?Piece.ROOK:Piece.blk(Piece.ROOK));

        setTile(org.x+(d*1), org.y, d>0?Piece.KNIGHT:Piece.blk(Piece.KNIGHT));
        setTile(org.x+(d*6), org.y, d>0?Piece.KNIGHT:Piece.blk(Piece.KNIGHT));

        setTile(org.x+(d*2), org.y, d>0?Piece.BISHOP:Piece.blk(Piece.BISHOP));
        setTile(org.x+(d*5), org.y, d>0?Piece.BISHOP:Piece.blk(Piece.BISHOP));

        // King and queen are symmetrical
        setTile(org.x+(d*((d>0)?4:3)), org.y, d>0?Piece.KING:Piece.blk(Piece.KING));
        setTile(org.x+(d*((d>0)?3:4)), org.y, d>0?Piece.QUEEN:Piece.blk(Piece.QUEEN));

        for (int i = 0; i < 8; ++i)
            setTile(i, ((d>0)?org.y+1:org.y-1), d>0?Piece.PAWN:Piece.blk(Piece.PAWN));
    }


    public int getMaterialCount(int color){
		return (color == Color.BLACK) ? blackMaterial : whiteMaterial;
	}

    protected List<Byte> tileList = new ArrayList<>();
    /**
    *	@return A list of this Board's Tile objects 
    */
    public List<Byte> getTileList(){
        if (tileList.isEmpty()) {
            for (int i = 0; i < 8; ++i)
                for (int j = 0; j < 8; ++j)
                    tileList.add(state[i][j]);
        }
        return tileList;
    }

    /** 
    *	Returns the Tile object at a certain location, with the bottom left corner of the board 
    *	defined as (0,0) and the top right defined as (7,7).
    *	
    *	@param l The location of the tile to return
    *	@return The tile at the specified location
    */
    public byte getTile(Location l){
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
	public byte getTile(int x, int y){
		return state[7-y][x];
	}

	private void setTile(Location l, byte p){
	    setTile(l.x, l.y, p);
    }

    private void setTile(int x, int y, byte p){
	    state[7-y][x] = p;
    }


	/** 
    *	Returns a two-dimensional array of Tile objects wherein array[0][0] is the top left corner
    *	of the board and array[7][7] is the bottom right.
    *	
    *	@return The array of Tile objects
    */
	public byte[][] getBoardState(){
		return state;
	}

	/**
	 * Get string of captured pieces of a certain color
	 * @param color color of captured pieces
	 * @return string of captured pieces in order of capture
	 */
	public String displayDeathRow(int color){
		StringBuilder res = new StringBuilder();
		res.append("   ");
		for (byte p : deathRow)
			if (Piece.color(p) == color)
				res.append(Piece.symbol(p) + " ");
		return res.toString();
	}

	private String writeColumnLabels(int max, boolean reversed){
		StringBuilder res = new StringBuilder();
		res.append(" ");
		for (int x = 0; x<9; ++x){
			if (x>0) res.append((" ABCDEFGH".charAt((reversed)?9-x:x)));
			for (int k=0;k<(max-1);++k)	
				res.append(" ");
		}
		res.append("\n");
		return res.toString();
	}

	private static <T> int findMaxLen(T[][] arr){
		int max=0;
		for (T[] row : arr)
			for (T el : row)
		        max = (String.valueOf(el).length() > max)?String.valueOf(el).length():max;
		return max;
    }
	public String getBoardString(){
		return getBoardString("","");
	}
	private String getBoardString(Player whiten, Player blackn){
		return getBoardString(whiten.toString(), blackn.toString());
	}
	private String getBoardString(String whiten, String blackn){
		StringBuilder res = new StringBuilder();
		
//		int max = findMaxLen(getBoardState());
        int max = 2;
		res.append("\n\n"+displayDeathRow(reversed ? Color.BLACK : Color.WHITE)+"\n");
		res.append("        "+(reversed ? whiten : blackn)+"\n");
		res.append(writeColumnLabels(max, reversed)+"\n");
		//if (!reversed){
			for (int y = 0; y < 8; ++y){
				res.append((8-y)+"  ");
				for (int x = 0; x < 8; ++x){
					if (state[y][x] != Piece.NONE)
						res.append(Piece.symbol(state[y][x])+"\u2007");
					else if ((x+y)%2==0)
						res.append(Game.unicode?"\u25AC ":"-");
					else res.append("\u25A2 ");
					res.append("");
				}
				res.append("  "+(8-y)+"\n");
			}
		//} else {
//			for (int i = getBoardState().length-1; i >= 0; --i){
//				res.append(y-i);
//				for (int j = getBoardState()[0].length-1; j >= 0; --j){
//					res.append(getBoardState()[i][j]);
//					for (int k=0;k<((max-String.valueOf(Piece.symbol(state[i][j])).length()));++k)
//						res.append(" ");
//				}
//				res.append(" "+(y-i)+"\n");
//			}
//		}
		res.append("\n"+writeColumnLabels(max, reversed));
		res.append("        "+(reversed ? blackn : whiten)+"\n");
		res.append(displayDeathRow(reversed ? Color.WHITE : Color.BLACK)+"\n\n");

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
			byte t = getTile((v)?a.x:i, (v)?i:a.y);
			if (t != Piece.NONE)
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
			if (getTile(x, y) != Piece.NONE)
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
	public double getDistanceFromKing(int color, Location locale){
		Location kingLoc = null;

		stateloop:
		for (int y = 0; y < 8; ++y){
		    for (int x = 0; x < 8; ++x){
		        byte t = state[y][x];
                if (t!=Piece.NONE && Piece.type(t) == Piece.Type.KING && Piece.color(t) == color){
                    kingLoc = Location.fromState(x,y);
                    break stateloop;
                }
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
		boolean validMove = false;

		if (!move.destination.onBoard() || !move.origin.onBoard())
			return false;

		if (move.piece == Piece.NONE || !available(move.destination, Piece.color(move.piece)) || move.origin.equals(move.destination))
			return false;
		
		int direction = Piece.dir(move.piece);

		// delta x and y
		int dx = move.destination.x - move.origin.x;
		int dy = direction * (move.destination.y - move.origin.y);

		boolean enemy = getTile(move.destination) != Piece.NONE;
		
		switch (Piece.type(move.piece)){
			case PAWN:
				byte enpasse = (dy == 1 && (dy + dx == 0 || dy + dx == 2)) ? getTile(move.origin.x+dx, move.origin.y) : Piece.NONE;
                Location enpsseloc = (enpasse != Piece.NONE) ? new Location(move.origin.x+dx, move.origin.y) : null;
				move.PROMOTION = (move.destination.y == (Piece.isw(move.piece) ? 7 : 0));
				if ((dy == 1 && dx == 0 && !enemy)
					|| (move.origin.y == (Piece.isw(move.piece) ? 1 : 6) && dy == 2 && dx == 0 && cardinalLineOfSightClear(move.origin, move.destination) && !enemy)
					|| (dy == 1 && (dy + dx == 0 || dy + dx == 2) && enemy)){
					validMove = true;
					break;
				}
				else if (!enemy &&
							enpasse != Piece.NONE &&
							available(new Location(move.origin.x+dx, move.origin.y), Piece.color(move.piece)) &&
							enpassentablePawns.contains(enpsseloc)){
					move.ENPASSE = true;
					validMove = true;
					break;
				}
				validMove = false;
				break;
			
			case ROOK:
				validMove = (cardinalLineOfSightClear(move.origin, move.destination));
				break;

			case KNIGHT:
				validMove = (dx != 0 && dy != 0 && Math.abs(dx) + Math.abs(dy) == 3);
				break;
			
			case BISHOP:
				validMove = (diagonalLineOfSightClear(move.origin, move.destination));
				break;
			
			case QUEEN:
				validMove = (diagonalLineOfSightClear(move.origin, move.destination))
						^ (cardinalLineOfSightClear(move.origin, move.destination));
				break;
			
			case KING:	
				if (Math.abs(dx) == 2 && dy == 0){
				    if (move.origin.equals(new Location(direction>0?0:7,4))) {
                        move.KCASTLE = canCastleKingside(Piece.color(move.piece));
                        move.QCASTLE = canCastleQueenside(Piece.color(move.piece));
                        if (move.QCASTLE || move.KCASTLE) {
                            validMove = true;
                            break;
                        }
                    }
				}
				validMove = (Math.abs(dx) <= 1 && Math.abs(dy) <= 1);
				break;
		}


		if (validMove && enemy)
			move.materialValue = Piece.value(getTile(move.destination));

		return validMove;
	}

	public boolean canCastleKingside(int c){
	    Location kingStart = new Location(c==Color.WHITE?0:7,4);
        Location rookStart = new Location(c==Color.WHITE?0:7, 7);

        // neither the king nor the rook have moved
        if (!hasMoved.contains(kingStart) && !hasMoved.contains(rookStart)) {

            // check if the tiles in between the king and the rook (k.y, 5) and (k.y, 6) are covered by an enemy piece
            for (Move m : getValidMoves(Color.opposite(c))) {
                if (m.destination == new Location(5, kingStart.y) ||
                        m.destination == new Location(6, kingStart.y))
                    return false;
            }

            // line of sight between pieces is clear
            return cardinalLineOfSightClear(kingStart, rookStart);
        }
        return false;
    }

    public boolean canCastleQueenside(int c){
        Location rookStart = new Location(c==Color.WHITE?0:7, 0);
        Location kingStart = new Location(c==Color.WHITE?0:7,4);

        // neither the king nor the rook have moved
        if (!hasMoved.contains(kingStart) && !hasMoved.contains(rookStart)) {

            // check if the tiles in between the king and the rook are covered by an enemy piece
            for (Move m : getValidMoves(Color.opposite(c))) {
                if (m.destination == new Location(1,kingStart.y) ||
                        m.destination == new Location(2, kingStart.y) ||
                        m.destination == new Location(3, kingStart.y))
                    return false;
            }

            // line of sight between pieces is clear
            return cardinalLineOfSightClear(kingStart, rookStart);
        }
        return false;
    }

	/**
	*	Computes a list of all valid moves for all pieces of a given color
	* 
	*	@param color The color to gather moves for
	*	@return The list of moves
	*/
	private List<Move> calculateValidMoves(int color){
		List<Move> moves = new ArrayList<>();
        for (int y = 0; y < 8; ++y) {
            for (int x = 0; x < 8; ++x) {
                Location ploc = new Location(x, y);
                byte p = getTile(ploc);
                if (!available(ploc, color)) {
                    List<Move> m_list = new ArrayList<>();
                    switch (Piece.type(p)) {
                        case PAWN:
                            m_list.add(new Move(ploc, new Location(ploc.x, ploc.y + (color * 2)), this));
                            m_list.add(new Move(ploc, new Location(ploc.x, ploc.y + color), this));
                            m_list.add(new Move(ploc, new Location(ploc.x + 1, ploc.y + color), this));
                            m_list.add(new Move(ploc, new Location(ploc.x - 1, ploc.y + color), this));
                            break;

                        case ROOK:
                            for (int i = 0; i < 8; ++i) {
                                if (i != ploc.x)
                                    m_list.add(new Move(ploc, new Location(i, ploc.y), this));
                                if (i != ploc.y)
                                    m_list.add(new Move(ploc, new Location(ploc.x, i), this));
                            }
                            break;

                        case KNIGHT:
                            int[][] pos = {
                                    {2, 1},
                                    {2, -1},
                                    {-2, 1},
                                    {-2, -1},
                                    {1, 2},
                                    {1, -2},
                                    {-1, 2},
                                    {-1, -2}
                            };
                            for (int[] ps : pos)
                                m_list.add(new Move(ploc, new Location(ploc.x + ps[0], ploc.y + ps[1]), this));
                            break;

                        case BISHOP:
                            for (int i = 1; i < 8; ++i) {
                                m_list.add(new Move(ploc, new Location(ploc.x + i, ploc.y + i), this));
                                m_list.add(new Move(ploc, new Location(ploc.x + i, ploc.y - i), this));
                                m_list.add(new Move(ploc, new Location(ploc.x - i, ploc.y + i), this));
                                m_list.add(new Move(ploc, new Location(ploc.x - i, ploc.y - i), this));
                            }
                            break;

                        case QUEEN:
                            for (int i = 0, j = 1; i < 7; ++i, ++j) {
                                if (i != ploc.x)
                                    m_list.add(new Move(ploc, new Location(i, ploc.y), this));
                                if (i != ploc.y) ;
                                m_list.add(new Move(ploc, new Location(ploc.x, i), this));

                                m_list.add(new Move(ploc, new Location(ploc.x + j, ploc.y + j), this));
                                m_list.add(new Move(ploc, new Location(ploc.x + j, ploc.y - j), this));
                                m_list.add(new Move(ploc, new Location(ploc.x - j, ploc.y + j), this));
                                m_list.add(new Move(ploc, new Location(ploc.x - j, ploc.y - j), this));
                            }
                            break;

                        case KING:
                            for (int i = 0; i < 3; ++i)
                                for (int j = 0; j < 3; ++j)
                                    m_list.add(new Move(ploc, new Location(ploc.x - 1 + i, ploc.y + 1 - j), this));
                            // potential castling moves
                            m_list.add(new Move(ploc, new Location(ploc.x + 2, ploc.y), this));
                            m_list.add(new Move(ploc, new Location(ploc.x - 2, ploc.y), this));
                            break;
                    }
                    for (Move move : m_list) {
                        if (isValidMove(move))
                            moves.add(move);
                    }
                }
            }
        }
		return moves;
	}

	/**
	*	Returns the list of valid moves for a given color if this method has been called before. 
	*	Otherwise, it will generate the list and return it. 
	* 
	*	@param color The color to gather moves for
	*	@return The list of moves
	*/
	private List<Move> getValidMoves(int color){
		if (color == Color.BLACK){
			if (validBlackMoves == null) validBlackMoves = calculateValidMoves(color);
			return validBlackMoves;
		}
		if (validWhiteMoves == null) validWhiteMoves = calculateValidMoves(color);
		return validWhiteMoves;
	}

	/**
	*	Determines whether a given player holds check on this board
	*	
	*	@param c The color of player to check for
	*	@return Whether the player has check 	
	*/
	public boolean playerHasCheck(int c){
		byte p;
		for (Move m : getValidMoves(c)){
			p = getTile(m.destination);
			if (p != Piece.NONE && Piece.type(p) == Piece.Type.KING)
				return true;
		}
		return false;
	}
	public boolean playerHasCheck(Player player){
		return playerHasCheck(player.color);
	}

	/**
	*	Computes a list of all legal moves for all pieces of a given color
	* 
	*	@param c The color to gather moves for
	*	@return The list of moves
	*/
	private List<Move> calculateLegalMoves(int c){
		List<Move> validMoves = getValidMoves(c);
		List<Move> legalMoves = new ArrayList<>();
		//long t1 = System.currentTimeMillis();
		for (Move m : validMoves){
			if (!new Board(this, m).playerHasCheck(Color.opposite(c))) {
				legalMoves.add(m);
			}
		}
		//System.out.println("Calculated legal in "+(System.currentTimeMillis() - t1)+"ms");

		return legalMoves;
	}

	/**
	*	Returns the list of legal moves for a given color if this method has been called before. 
	*	Otherwise, it will generate the list and return it. 
	* 
	*	@param color The color to gather moves for
	*	@return The list of moves
	*/
	public List<Move> getLegalMoves(int color){
		long t0 = System.currentTimeMillis();
		if (color == Color.BLACK){
			if (legalBlackMoves == null) legalBlackMoves = calculateLegalMoves(color);
			return legalBlackMoves;
		}
		if (legalWhiteMoves == null) legalWhiteMoves = calculateLegalMoves(color);
		return legalWhiteMoves;
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
	*	Populates both legal move lists
	*	
	*/
	public void processLegal(){
		getLegalMoves(iteration % 2 == 0 ? Color.WHITE : Color.BLACK);
	}

	/**
	*	Returns the list of legal moves for a given player's opponent
	* 
	*	@param player The Opponent of the Player to gather moves for
	*	@return The list of moves
	*/
	public List<Move> getOpponentLegalMoves(Player player){
		return getOpponentLegalMoves(player.color);
	}

	/**
	*	Returns the list of legal moves for a given color's opponent
	*
	*	@param color The color of the Opponent of the Player to gather moves for
	*	@return The list of moves
	*/
	public List<Move> getOpponentLegalMoves(int color){
		return getLegalMoves(Color.opposite(color));
	}

	private void moveOccupator(Location a, Location b){
		setTile(b, getTile(a));

		setTile(a, Piece.NONE);
	}

	public void addToDeathRow(byte p){
		deathRow = Arrays.copyOf(deathRow, deathRow.length+1);
        deathRow[deathRow.length-1] = p;
	}

	/**
	 * Returns the board iteration number, and which color has the move.
	 * @return Board iteration
	 */
	@Override
	public String toString(){
		return "Board#"+String.valueOf(iteration)+":"+(iteration%2==0 ? "White":"Black")+" to move.";
	}

	public void print(Player one, Player two){
		System.out.println(getBoardString(one, two));
	}

    /**
     *	Returns whether this tile contains no piece or contains a piece with a color
     *	not equal to the given color. In other words, it returns whether or not a
     *	Piece of the given color can move to this Tile.
     *
     *	@param c The color to use
     *	@return Whether a piece of the given color can move to this tile.
     */
    public boolean available(Location l, int c){
        byte t = getTile(l);
        return (t == Piece.NONE || Piece.color(t) != c);
    }
}