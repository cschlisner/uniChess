package uniChess;

import org.json.*;
import java.util.List;
import java.awt.image.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Game {
	public static Log gameLog;

	private static Board board;
	
	public Player player1, player2;

	public static enum PieceType {PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING;}
	public static enum Color {WHITE, BLACK;}

	private static boolean whiteTurn = true;
	private static boolean dead = false; 
	
	private static int turnCount = 0, drawOfferTurn = -1;
	
	private static String gameId;

	public Game(String p1Name, String p2Name){
		this(p1Name, p2Name, false, null);
	}
	
	public Game(String p1Name, String p2Name, boolean imageOut, String imageFileOut){

		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("kms");

		gameId = String.format("%c%c%s",((p1Name!=null)?p1Name.charAt(0):'C'), ((p2Name!=null)?p2Name.charAt(0):'C'), sdf.format(now));

		board = new Board();

		gameLog = new Log(this, imageOut, imageFileOut);

		player1 = new Player(this, (p1Name!=null)?p1Name:"Chesster", (p1Name!=null), Color.WHITE);
		player2 = new Player(this, (p2Name!=null)?p2Name:"Chesster", (p2Name!=null), Color.BLACK);

		gameLog.logBoard();
		gameLog.writeBuffer(String.format("New game started between %s and %s.", player1, player2));
		if (!imageOut) gameLog.writeBuffer("Turn: "+getCurrentPlayer().toString());

		getCurrentPlayer().getTeam().updateStatus();

	}

	public Game(String gameFile){
		board = new Board();
		gameFile+=((gameFile.contains(".chess"))?"":".chess");
		try {
			JSONObject gameData = Log.importGame(gameFile);

			player1 = new Player(this, gameData.getString("player1"), gameData.getBoolean("player1Human"), Color.WHITE);
			player2 = new Player(this, gameData.getString("player2"), gameData.getBoolean("player2Human"), Color.BLACK);
			
			gameId = gameData.getString("id");

			gameLog = new Log(this, gameData.getBoolean("imageOutput"), gameData.optString("imageExportFile"));
			
			JSONArray jsonMoveArray = gameData.getJSONArray("moves");

		    for (int i = 0; i < jsonMoveArray.length(); ++i)
		    	gameLog.appendMoveHistory(jsonMoveArray.getString(i));
		    performMoves(gameLog.getMoveHistory());
		} catch (Exception e){
			System.out.println("Error importing game file: "+gameFile);
			e.printStackTrace();
			return;
		}
	    gameLog.logBoard();
		gameLog.writeBuffer(String.format("Game %s continued between %s and %s.", gameId, player1.toString(), player2.toString()));
		if (!gameLog.isImageOut()) gameLog.writeBuffer("Turn: "+getCurrentPlayer().toString());
	}

	public String getId(){
		return gameId;
	}

	public void input(String in){
		input(in, true);
	}


	boolean endTurn = true;

	public void input(String in, boolean logMove){
		endTurn = getCurrentPlayer().readMove(in);
		
		getDormantPlayer().getTeam().updateStatus();

		if (endTurn){
			if (logMove) gameLog.appendMoveHistory(in);
			++turnCount;
		}

		if (player1.draw && player2.draw){
			endGame(null, "Draw");
			return;
		}


		if (drawOfferTurn > 0 && getCurrentPlayer().draw && endTurn){
			gameLog.writeBuffer("Draw offer from "+getCurrentPlayer()+" has expired.");
			getCurrentPlayer().draw = false;
			drawOfferTurn = -1;
		}

		if (getCurrentPlayer().draw && (drawOfferTurn < 0) && endTurn){
			drawOfferTurn = turnCount;
			gameLog.writeBuffer(getCurrentPlayer()+" has offered a draw. Input draw to accept.");
		}

		if (getDormantPlayer().getTeam().inCheck()){
			gameLog.writeBuffer(getDormantPlayer()+" is in check!\nAvailable moves:");
			getDormantPlayer().readTeamStatus();
		}

		if (getDormantPlayer().getTeam().inCheckMate()){
			endGame(getCurrentPlayer(), "Checkmate");
			return;
		}

		if (gameInStaleMate(getCurrentPlayer())){
			endGame(null, "Stalemate");
			return;
		}

		if (getCurrentPlayer().forfeit){
			endGame(getDormantPlayer(), "Forfeit");
			return;
		}

		whiteTurn = (endTurn)?!whiteTurn:whiteTurn;

		// getCurrentPlayer().readTeamStatus();
		gameLog.logBoard();
		if (!gameLog.isImageOut()) gameLog.writeBuffer("Turn: "+getCurrentPlayer().toString());

		if (!getCurrentPlayer().isHuman && logMove)
			input(getCurrentPlayer().getBotMove(), true);
	}

	public BufferedImage getBoardImage(){
		return gameLog.getBoardImage();
	}

	public String getInfoOutput(){
		return gameLog.getUnreadBuffer();
	}

	public boolean isCurrentPlayer(Player p){
		return isCurrentPlayer(p.toString());
	}

	public boolean isCurrentPlayer(String user){
		return (getCurrentPlayer().toString().equals(user));
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
		return (!p.getTeam().inCheck() && p.getTeam().getMoveMap().size()==0);
	}

	private static void endGame(Player winner, String result){
		gameLog.logBoard();
		gameLog.writeBuffer("Game ended in "+result);
		if (winner != null) 
			gameLog.writeBuffer(winner+" wins!");
		dead = true;
	}

	public static boolean isDead(){
		return dead;
	}

	public boolean saveGame(){
		return gameLog.saveGame();
	}

	private void performMoves(List<String> moves){
		if (moves != null){
			for (String m : moves){
				//getInfoOutput();
				input(m, false);
			}
		}
	}
}
