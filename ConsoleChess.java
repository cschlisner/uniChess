import uniChess.*;

import java.util.*;
import java.io.*;

class ConsoleChess {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		Player<String> p1 = new Chesster<>("EXP2", Game.Color.WHITE);
		Player<String> p2 = new Chesster<>("Dynamic", Game.Color.BLACK);

		((Chesster)p1).STRATEGY = Chesster.StrategyType.EXP2;
		((Chesster)p1).dynamic = true;

		Game chessGame = new Game(p1, p2);
		
		chessGame.getCurrentBoard().print(p1,p2);
		
		while (true){

				Player currentPlayer = chessGame.getCurrentPlayer();
				//in.nextLine();
				String input = ((currentPlayer instanceof Chesster) ? ((Chesster)currentPlayer).getMove().getANString() : in.nextLine()); 
				
				if (input.equals("gametext")){
					System.out.println(chessGame.getGameString());
					continue;
				}
				
				Game.GameEvent gameResponse = chessGame.advance(input);

				chessGame.getCurrentBoard().print(p1,p2);
				
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