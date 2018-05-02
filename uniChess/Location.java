package uniChess;

/**
*	An object representing a point in a two dimensional grid. This is used for the internal coordinate system of the
*	Board, as well as parsing algebraic locations to integer format. 
*/
public class Location {
	public int x, y;

	public Location(int x, int y){
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns location translated from state array coordinates i.e. x->x y->7-y
	 */
	public static Location fromState(int x, int y){
		return new Location(x, 7-y);
	}

	public Location(String in) throws GameException {
		try {
			in = in.toLowerCase();
			final String col = "abcdefgh";
			final String row = "12345678";
			this.x = col.indexOf(in.charAt(0));
			this.y = row.indexOf(in.charAt(1));

			if (x<0 || y<0) throw new IndexOutOfBoundsException("ayylmao");
		} catch (IndexOutOfBoundsException e){
			throw new GameException(GameException.INVALID_MOVE, "Could not parse location from '"+in+"'");
		}
	}

	public boolean equals(Location l){
		return (x == l.x && y == l.y);
	}

	@Override
	public String toString(){
		final String col = "abcdefgh";
		final String row = "12345678";
		return String.format("%c%c",col.charAt(x),row.charAt(y));
	}

	public boolean onBoard() {
		return (x >= 0 && y >= 0 && y < 8 && x < 8);
	}
}