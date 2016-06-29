import uniChess.*;

import java.util.*;
import java.io.*;

class Example {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		Player<String> p1 = new Player<>("player one", Game.Color.WHITE);
		Player<String> p2 = new Player<>("player two", Game.Color.BLACK);

		Game chessGame = new Game(p1, p2, args[0]);

		System.out.print(chessGame.getCurrentBoard().getBoardString(!chessGame.whiteMove));
		
		while (true){

				switch(chessGame.advance(in.nextLine())){

					case OK:
						break;
					case AMBIGUOUS:
						break;
					case INVALID:
						break; 
					case ILLEGAL:
						break;
					case CHECK:
						break;
					case CHECKMATE:
						break;
					case STALEMATE:
						break;
					case DRAW:
						break;

				}

				System.out.print(chessGame.getCurrentBoard().getBoardString(!chessGame.whiteMove));
				System.out.println(chessGame.getGameString());
		}

	}

}