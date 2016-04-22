package uniChess;

public class Location {
	int x, y;

	public Location(int x, int y){
		this.x = x;
		this.y = y;
	}

	public Location(String in) throws GameException {
		try {
			in = in.toLowerCase();
			final String col = "abcdefgh";
			final String row = "12345678";
			this.x = col.indexOf(in.charAt(0));
			this.y = row.indexOf(in.charAt(1));
		} catch (IndexOutOfBoundsException e){
			throw new GameException(GameException.INPUT, "Could not parse location from '"+in+"'");
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
}