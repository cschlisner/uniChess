package uniChess;

import java.util.List;
import java.util.ArrayList;

public class Move {
	public Game game;
	public Board board;
	
	public Piece piece;
	public Location dest;
	
	public Move(Game g, Team t, String in) throws GameException {
		String[] tokens = in.split("");

		String pieceSymbol = tokens[0];

		int rank = -1, file = -1;

		if (tokens.length == 4){
			char mysteryToken = tokens[1].toLowerCase().charAt(0);
			if (Character.isLetter(mysteryToken)){
				file = "abcdefgh".indexOf(mysteryToken);
				if (file < 0)
					throw new GameException("Invalid move.");
			}
			else if (Character.isDigit(mysteryToken)){
				rank = Character.getNumericValue(mysteryToken)-1;
				if (rank < 0 || rank > 7)
					throw new GameException("Invalid move.");
			}
		}

		dest = new Location(tokens[tokens.length-2]+tokens[tokens.length-1]);

		List<Piece> potentialPieces = new ArrayList<Piece>();

		for (Piece p : t.getPieceSet())
			if (p.ofType(pieceSymbol) && p.canMove(dest))
				if (((rank < 0 ^ p.getLocation().y == rank)) && ((file < 0 ^ p.getLocation().x == file))) // if the rank or file have been specified, add only matching pieces
					potentialPieces.add(p);

		if (potentialPieces.size() == 1){
			game = g;
			board = g.getBoard();
			piece = potentialPieces.get(0);
		}

		if (potentialPieces.size() < 1)
			throw new GameException("Invalid move.");

		if (potentialPieces.size() > 1){
			boolean specF=false, specR=false;
			for (Piece p : potentialPieces){
				for (Piece q : potentialPieces){
					if (p.getLocation().x != q.getLocation().x)
						specF = true;
					if (!specF && p.getLocation().y != q.getLocation().y)
						specR = true;
				}
			}

			String[] msg = new String[potentialPieces.size()+1];
			int i = 0;
			msg[i++] = "Ambiguous move. Options:";
			for (Piece p : potentialPieces)
				msg[i++] = String.format("%d. %s%s%s%s",i-1,p.getSymbol(),(specF?p.getLocation().toString().charAt(0):""),(specR?p.getLocation().y:""),dest.toString());
			throw new GameException(msg);
		}
	}

	public Move(Game g, Piece p, Location d){
		game = g;
		board = g.getBoard();
		piece = p;
		dest = d;
	}

	public void attempt() throws GameException{
		if (!piece.moveTo(dest))
			throw new GameException("Invalid move.");
	}

	@Override 
	public boolean equals(Object o){
		if (o instanceof Move){
			Move om = (Move)o;
			return (this.game == om.game && this.piece == om.piece && this.dest.equals(om.dest));
		}
		else return false;
	}

	@Override
	public String toString(){
		return String.format("%s%s", piece.getSymbol(), dest);
	}
}