import uniChess.*;

import java.util.*;
import java.io.*;

class ConsoleChess {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		Player<String> p1 = new Player<>("player one", Game.Color.WHITE);
		Player<String> p2 = new Player<>("player two", Game.Color.BLACK);

		Game chessGame = new Game(p1, p2);
		Game.unicode = false;
		System.out.print(chessGame.getCurrentBoard().getBoardString(!chessGame.whiteMove));
		
		while (true){

				Game.GameEvent gameResponse = chessGame.advance(in.nextLine());

				System.out.print(chessGame.getCurrentBoard().getBoardString(!chessGame.whiteMove));
				
				switch(gameResponse){

					case OK:
						break;
					case AMBIGUOUS:
						System.out.println("Ambiguous Move.");
						break;
					case INVALID:
						System.out.println("Invalid Move.");
						break; 
					case ILLEGAL:
						System.out.println("Illegal Move.");					
						break;
					case CHECK:
						System.out.println("You are in check!");
						break;
					case CHECKMATE:
						System.out.println("Checkmate. "+chessGame.getDormantPlayer().getID()+" wins!");
						System.exit(0);
						break;
					case STALEMATE:
						System.out.println("Stalemate. "+chessGame.getDormantPlayer().getID()+" wins!");
						break;
					case DRAW:
						System.out.println("Draw!");
						break;

				}

		}

	}

}