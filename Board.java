package uniChess;

public class Board {
	private Tile[][] state = new Tile[8][8];

	public Board(){
		for (int y=0; y<8; ++y)
			for (int x=0; x<8; ++x)
				state[y][x] = new Tile(new Location(x, 7-y));
	}

	public Board(Board org){
		for (int y=0; y<8; ++y)
			for (int x=0; x<8; ++x)
				this.state[y][x] = new Tile(org.state[y][x]);
	}

	public Tile[][] getBoardState(){
		return state;
	}

	public Tile getTile(Location l){
		return getTile(l.x, l.y);
	}
	public Tile getTile(int x, int y){
		return state[7-y][x];
	}

	// Returns whether or not a line of sight between two Locations is 'clear' 
	// (has no pieces between them) in the up, down, left, right directions
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
	// (has no pieces between them) in the up, down, left, right directions
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

	public void printSelf(Game g){
		boolean reversed = g.isCurrentPlayer(g.player2);

		int max = findMaxLen(getBoardState());
		int y = 8;
		writeColumnLabels(max, reversed);
		if (!reversed){
			for (Board.Tile[] row : getBoardState()){
				System.out.print(y);
				for (Board.Tile el : row){
					System.out.print(el);
					for (int k=0;k<((max-String.valueOf(el).length()));++k)	
						System.out.print(" ");
				}
				System.out.println(y--);
			}
		} else {
			for (int i = getBoardState().length-1; i >= 0; --i){
				System.out.print(y-i);
				for (int j = getBoardState()[0].length-1; j >= 0; --j){
					System.out.print(getBoardState()[i][j]);
					for (int k=0;k<((max-String.valueOf(getBoardState()[i][j]).length()));++k)	
						System.out.print(" ");
				}
				System.out.println(y-i);
			}
		}
		writeColumnLabels(max, reversed);
	}

	private void writeColumnLabels(int max, boolean reversed){
		for (int x = 0; x<9; ++x){
			if (x>0) System.out.print(" ABCDEFGH".charAt((reversed)?9-x:x));
			for (int k=0;k<(max-1);++k)	
				System.out.print(" ");
		}
		System.out.println("");
	}
	private static <T> int findMaxLen(T[][] arr){
		int max=0;
		for (T[] row : arr)
			for (T el : row)
		 max = (String.valueOf(el).length() > max)?String.valueOf(el).length():max;
		return max;
    }


	public class Tile {
		private Piece occupator;
		private Location locale;
		private Game.Color color;

		public Tile(Location loc){
			locale = loc;
			occupator = null;
			color = ((loc.x+loc.y)%2==0)?Game.Color.BLACK:Game.Color.WHITE;
		}

		public Tile(Tile org){
			this.locale = org.locale;
			this.occupator = org.occupator;
			this.color = org.color;
		}

		public Location getLocale(){	
			return locale;
		}
		public Piece getOccupator(){
			return occupator;
		}

		public boolean containsEnemy(Team team){
			return (occupator!=null && !occupator.getTeam().equals(team));
		}

		public boolean containsEnemyOfType(Team team, Game.PieceType eType){
			return (occupator!=null && !occupator.getTeam().equals(team) && occupator.ofType(eType));
		}

		public boolean containsFriendly(Piece p){
			return (!locale.equals(p.getLocation()) && occupator!=null && occupator.getTeam().equals(p.getTeam()));
		}

		public boolean available(Piece p){
			return (occupator==null || !occupator.getTeam().equals(p.getTeam()));
		}
		public void setOccupator(Piece p){
			occupator = p;
		}

		public boolean attemptMove(Piece p){
			if (occupator != null){
				if (!occupator.getTeam().equals(p.getTeam())){
					occupator.kill();
				}
				else return false;
			}
			p.setLocation(locale);
			setOccupator(p);
			return true;
		}

		@Override
		public String toString(){
			return "|"+(((occupator!=null)?occupator.getSymbol():(color.equals(Game.Color.BLACK))?"\u00b7":" "))+"|";
		}
	}
}