import uniChess.*;

import java.util.*;
import java.io.*;

class ConsoleChess {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		Chesster<String> p1 = new Chesster<>("Human", Game.Color.WHITE);
		Chesster<String> p2 = new Chesster<>("Chesster", Game.Color.BLACK);

		Game chessGame = new Game(p1, p2);
		System.out.print(chessGame.getCurrentBoard());
		
		while (true){

				Game.GameEvent gameResponse = chessGame.advance((chessGame.getCurrentPlayer().equals(p1) ? p1.getMove().getANString() : p2.getMove().getANString()));

				System.out.print(chessGame.getCurrentBoard());
				
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
						System.out.println(chessGame.getGameString());
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