package uniChess;

public class Move {
	public Game game;
	public Board board;
	
	public Piece piece;
	public Location dest;
	
	public Move(Game g, Piece p, Location d){
		game = g;
		board = g.getBoard();
		piece = p;
		dest = d;
	}

	@Override 
	public boolean equals(Object o){
		if (o instanceof Move){
			Move om = (Move)o;
			return (this.game == om.game && this.piece == om.piece && this.dest.equals(om.dest));
		}
		else return false;
	}
}