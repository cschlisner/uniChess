package uniChess;

public class Location {
	int x, y;

	public Location(int x, int y){
		this.x = x;
		this.y = y;
	}

	public Location(String in){
		in = in.toLowerCase();
		final String col = "ABCDEFGH";
		final String row = "12345678";
		this.x = col.toLowerCase().indexOf(in.charAt(0));
		this.y = row.indexOf(in.charAt(1)); 
	}

	public boolean equals(Location l){
		return (x == l.x && y == l.y);
	}

	@Override
	public String toString(){
		final String col = "ABCDEFGH";
		final String row = "12345678";
		return String.format("%s%s",col.charAt(x),row.charAt(y));
	}
}