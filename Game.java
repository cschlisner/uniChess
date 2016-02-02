package uniChess;

import org.json.*;
import java.util.Random;
import java.util.List;

public class Game {
	public static Log gameLog;

	private static Board board;
	
	public Player player1, player2;

	public static enum PieceType {PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING;}
	public static enum Color {WHITE, BLACK;}

	private static boolean whiteTurn = true;
	private static boolean dead = false; 
	private static boolean imageOutput;
	
	private static int turnCount = 0, drawOfferTurn = -1;
	
	public static String defaultFileOut = "currBoard.png";
	private static String gameId;

	public Game(String p1Name, String p2Name, boolean imageOut){

		Random r = new Random();

		gameId = p1Name.substring(r.nextInt(p1Name.length()))+p2Name.substring(r.nextInt(p2Name.length()))+String.valueOf(r.nextInt(9000)+1000);

		imageOutput = imageOut;

		board = new Board(this);

		gameLog = new Log(this);

		player1 = new Player(this, p1Name, true, Color.WHITE);
		player2 = new Player(this, p2Name, true, Color.BLACK);

		gameLog.logBoard(imageOut);
		gameLog.writeBuffer(String.format("New game started between %s and %s.", p1Name, p2Name));
		// gameLog.writeBuffer("Turn: "+getCurrentPlayer().getName());
	}

	public Game(String p1Name, String p2Name){
		this(p1Name, p2Name, false);
	}

	public Game(String gameFile){
		board = new Board(this);
		gameLog = new Log(this);
		try {
			JSONObject gameData = gameLog.importGame(gameFile+".chess");
			
			player1 = new Player(this, gameData.getString("player1"), true, Color.WHITE);
			player2 = new Player(this, gameData.getString("player2"), true, Color.BLACK);
			
			gameId = gameData.getString("id");
			imageOutput = gameData.getBoolean("imageOutput");

			JSONArray jsonMoveArray = gameData.getJSONArray("moves");
		    for (int i = 0; i < jsonMoveArray.length(); ++i)
		    	gameLog.appendMoveHistory(jsonMoveArray.getString(i));
		    performMoves(gameLog.getMoveHistory());
		} catch (Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	    gameLog.logBoard(imageOutput);
		gameLog.writeBuffer(String.format("Game %s continued between %s and %s.", gameId, player1.getName(), player2.getName()));
	}

	public boolean getImageOutput(){
		return imageOutput;
	}

	public String getId(){
		return gameId;
	}

	public void input(String in){
		input(in, true);
	}

	public void input(String in, boolean logMove){
		getCurrentPlayer().getTeam().updateStatus();

		boolean endTurn = getCurrentPlayer().readMove(in);

		if (endTurn){
			if (logMove) gameLog.appendMoveHistory(in);
			++turnCount;
		}

		if (player1.draw && player2.draw){
			endGame("Draw");
			return;
		}


		if (drawOfferTurn > 0 && getCurrentPlayer().draw && endTurn){
			gameLog.writeBuffer("Draw offer from "+getCurrentPlayer().getName()+" has expired.");
			getCurrentPlayer().draw = false;
			drawOfferTurn = -1;
		}

		if (getCurrentPlayer().draw && (drawOfferTurn < 0) && endTurn){
			drawOfferTurn = turnCount;
			gameLog.writeBuffer(getCurrentPlayer()+" has offered a draw. Input draw to accept.");
		}

		if (getCurrentPlayer().getTeam().inCheck())
			gameLog.writeBuffer("You are in check!");

		if (getDormantPlayer().getTeam().inCheckMate())
			endGame(getCurrentPlayer());

		if (gameInStaleMate(getCurrentPlayer())){
			endGame("Stalemate");
			return;
		}

		if (getCurrentPlayer().forfeit){
			endGame(getDormantPlayer());
			return;
		}

		whiteTurn = (endTurn)?!whiteTurn:whiteTurn;

		// gameLog.writeBuffer("Turn: "+getCurrentPlayer().getName());
		gameLog.logBoard(imageOutput);
	}

	public String getOutput(){
		return gameLog.getUnreadBuffer();
	}

	public boolean isCurrentPlayer(Player p){
		return isCurrentPlayer(p.getName());
	}

	public boolean isCurrentPlayer(String user){
		return (getCurrentPlayer().getName().equals(user));
	}

	public Player getCurrentPlayer(){
		return (whiteTurn)?player1:player2;
	}

	public Player getDormantPlayer(){
		return (!whiteTurn)?player1:player2;
	}

	public int getTurnCount(){
		return turnCount;
	}

	public Board getBoard(){
		return board;
	}

	private static boolean gameInStaleMate(Player p){
		return (!p.getTeam().inCheck() && p.getTeam().getAllMoves().size()==0);
	}

	private static void endGame(String gameResult){
		gameLog.logBoard(imageOutput);
		gameLog.writeBuffer("Game ended in "+gameResult);
		dead = true;
	}

	private static void endGame(Player winner){
		gameLog.logBoard(imageOutput);
		gameLog.writeBuffer("\n"+((winner!=null)?(winner.getTeam().getColor()+" wins!"):"Game has reached Stalemate"));
		dead = true;
	}

	public static boolean isDead(){
		return dead;
	}

	// saved games

	public boolean saveGame(){
		return gameLog.saveGame();
	}

	private void performMoves(List<String> moves){
		if (moves != null)
			for (String m : moves)
				input(m, false);
	}
}