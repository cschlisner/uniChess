import uniChess.*;

import java.util.*;
import java.io.*;

class ConsoleChess {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		Player<String> human = new Player<>("Human",Color.WHITE);
		Player<String> c3p0 = new C3P0<>("C3P0",Color.BLACK);

		Game chessGame = new Game(human, c3p0);
		
		chessGame.getCurrentBoard().print(human,c3p0);
		
		while (true){

				Player currentPlayer = chessGame.getCurrentPlayer();
				//in.nextLine();
				String input = ((currentPlayer instanceof C3P0) ? (currentPlayer).getMove() : in.nextLine());
				
				if (input.equals("gametext")){
					System.out.println(chessGame.getGameString());
					continue;
				}
				
				Game.GameEvent gameResponse = chessGame.advance(input);

				chessGame.getCurrentBoard().print(human,c3p0);
				
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
