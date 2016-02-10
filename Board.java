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

	/**
	* This will accept a MoveSimulation object which is specified with a selection Location and a destination Location
	* and will replace the occupator at the destination with the occupator at the selection, run the abstract method 
	* getSimulationData() that is outlined when the MoveSimulation object is supplied, store the result (of Type T) 
	* in a variable, then relocate the now switched occupators to their original locations. 
	*
	* This method will not account for valid moves. 
	*
	* @param MoveSimulation<T> m
	* @return <T> result of m.getSimulation data after moveSimulation
	*
	*/
	public <T> T runMoveSimulation(MoveSimulation<T> m){
		if (m.dest.equals(m.select)) 
			return m.getSimulationData();

		Piece destinationPiece = (m.dest!=null)?getTile(m.dest).getOccupator():null;
		Piece selectPiece = getTile(m.select).getOccupator();

		if (m.dest!=null) getTile(m.dest).setOccupator(selectPiece);
		getTile(m.select).setOccupator(null);

		T returnVal =  m.getSimulationData();
		
		if (m.dest!=null) getTile(m.dest).setOccupator(destinationPiece);
		getTile(m.select).setOccupator(selectPiece);

		if (m.dataPiece != null) m.dataPiece.update(); // refresh piece that might have been modified
		
		return returnVal;
	}

	public static abstract class MoveSimulation<T> {
		public Location select;
		public Location dest;
		public Piece dataPiece;

		public MoveSimulation(Location select, Location dest){
			this.select = select;
			this.dest = dest;
		}
		public MoveSimulation(Piece dataPiece, Location select, Location dest){
			this.dataPiece = dataPiece;
			this.select = select;
			this.dest = dest;
		}
		public abstract T getSimulationData();
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

		public boolean containsEnemy(Piece p){
			return (occupator!=null && !occupator.getTeam().equals(p.getTeam()));
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