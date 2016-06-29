import uniChessBeta.*;

import java.util.*;
import java.io.*;

class Example {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		Player<String> p1 = new Player<>("player one", Game.Color.WHITE);
		Player<String> p2 = new Player<>("player two", Game.Color.BLACK);

		Game chessGame = new Game(p1, p2);

		System.out.print(chessGame.getCurrentBoard().getBoardString(!chessGame.whiteMove));
		
		while (true){

				switch(chessGame.advance(in.nextLine())){

					case Game.GameEvent.OK:
						break;
					case Game.GameEvent.AMBIGUOUS:
						break;
					case Game.GameEvent.INVALID:
						break; 
					case Game.GameEvent.ILLEGAL:
						break;
					case Game.GameEvent.CHECK:
						break;
					case Game.GameEvent.CHECKMATE:
						break;
					case Game.GameEvent.STALEMATE:
						break;
					case Game.GameEvent.DRAW:
						break;

				}

				System.out.print(chessGame.getCurrentBoard().getBoardString(!chessGame.whiteMove));
		}

	}

}