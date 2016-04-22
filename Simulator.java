package uniChess;

public abstract class Simulator<T> {
	public static Board simBoard;
	public Move move;
	public Object[] data;

	public Simulator(Move m, Object... data){
		simBoard = new Board();

		Team alt1 = new Team(m.game, simBoard, m.game.player1.getTeam());
		Team alt2 = new Team(m.game, simBoard, m.game.player2.getTeam());
		
		move = getSimMove(m);

		this.data = data;
		for (Object o : data){
			if (o instanceof Piece)
				o = getSimPiece((Piece)o);
			else if (o instanceof Move)
				o = (Object)getSimMove((Move)o);
		}
	}

	public abstract T getData();

	public T simulate(){
		if (move.dest != null) move.piece.setLocation(move.dest);
		else move.piece.kill();

		T returnVal =  getData();
		
		return returnVal;
	}

	public T readAndSimulate(){
		System.out.println("___________________BEGINSIMULATION___________________");
		simBoard.printSelf(move.game);
		if (move.dest != null) move.piece.setLocation(move.dest);
		else move.piece.kill();
		simBoard.printSelf(move.game);
		System.out.println("___________________END  SIMULATION___________________");
		T returnVal =  getData();
		return returnVal;
	}

	public Piece getSimPiece(Piece p){
		return simBoard.getTile(p.getLocation()).getOccupator();
	}

	public Move getSimMove(Move m){
		return new Move(m.game, simBoard, simBoard.getTile(m.pieceLocale).getOccupator(), m.dest);
	}
}