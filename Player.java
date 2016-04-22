package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Player {
	public boolean forfeit, draw;
	private Game game;
	private Board board;
    private String[] cmdList = {"move", "get", "status", "forfeit", "help", "draw", "attack", "protect", "simPotential"};
    public boolean isHuman;
    private String name;
    public Team team;
    public Chesster chesster;

    public Player(Game game, String name, boolean yep, Game.Color c){
    	this.game = game;
    	board = game.getBoard();
    	this.name = name;
        isHuman = yep;
        team = new Team(game, c);

        if (!isHuman)
        	chesster = new Chesster(game, team, 3);
    }

    public void readTeamStatus(){
    	Game.gameLog.startBuffer();
    	Map<Piece, List<Move>> moveMap = team.getMoveMap();
		Iterator moveMapIterator = moveMap.keySet().iterator();

		while (moveMapIterator.hasNext()){
			Piece p = (Piece)moveMapIterator.next();
			Game.gameLog.bufferAppend(p+" : ");
			Game.gameLog.bufferAppendArray(moveMap.get(p).toArray());
		}
    	Game.gameLog.terminateBuffer();
    }

    public Team getTeam(){
    	return team;
    }

    public String getName(){
    	return name;
    }

    public String getBotMoveText(){
    	String mv = "move "+chesster.getBestMove().getANString();
    	Game.gameLog.writeBuffer(mv);
    	return mv;
    }

    public void readMove(String inStr) throws GameException{
    	String[] tokens = inStr.split(" ");

		if (tokens.length < 1)
			throw new GameException(GameException.INPUT, "Invalid move.");

    	String cmd = tokens[0];
		if (Arrays.asList(cmdList).contains(cmd)){
			int index = 1;
			Location select = null, dest = null;
			String[] msg;
			switch (cmd) {
				case "move":
					Move move = new Move(game, board, team, inStr.substring(inStr.indexOf(" ")+1));
					move.attempt();
					return;

				case "get":
					msg = new String[1];
					select = new Location(tokens[index++]);
					Piece selectedTilePiece = board.getTile(select).getOccupator();
					if (selectedTilePiece != null)
						msg[0] = Arrays.toString(selectedTilePiece.getMoves());
					else msg[0] = "Tile at "+select+" has no piece.";
					throw new GameException(GameException.INPUT, msg);

				case "status":
					readTeamStatus();
					throw new GameException(GameException.INPUT, "");

				case "forfeit": 
					throw new GameException(GameException.FORFEIT, "");
				
				case "draw":
					draw = true;
					throw new GameException(GameException.INPUT, "");

				case "help":
					 throw new GameException(GameException.INPUT, "Commands: "+Arrays.toString(cmdList));

				// case "attack":
				// 	Game.gameLog.startBuffer();
				// 	try {
				// 		select = new Location(tokens[index++]);
				// 	} catch (GameException e){}
				// 	selectedTilePiece = board.getTile(select).getOccupator();
				// 	if (selectedTilePiece != null)
				// 		Game.gameLog.bufferAppendArray(selectedTilePiece.attackedPieces.toArray());
				// 	else Game.gameLog.writeBuffer("Tile at "+select+" has no piece.");
				// 	Game.gameLog.terminateBuffer();
				// 	return;

				// case "protect":
				// 	Game.gameLog.startBuffer();
				// 	try {
				// 		select = new Location(tokens[index++]);
				// 	} catch (GameException e){}
				// 	selectedTilePiece = board.getTile(select).getOccupator();
				// 	if (selectedTilePiece != null)
				// 		Game.gameLog.bufferAppendArray(selectedTilePiece.protectedPieces.toArray());
				// 	else Game.gameLog.writeBuffer("Tile at "+select+" has no piece.");
				// 	Game.gameLog.terminateBuffer();
				// 	return;

				// case "getSim":
				// 	try {
				// 		select = new Location(tokens[index++]);
				// 		dest = new Location(tokens[index++]);
				// 	} catch (GameException e){}
				// 	if (board.getTile(select).getOccupator() != null){
				// 		Game.gameLog.startBuffer();
				// 		Game.gameLog.bufferAppend(board.getTile(select).getOccupator()+" > "+dest+": ");
				// 		Game.gameLog.bufferAppendArray(board.getTile(select).getOccupator().getSimulatedMoves(dest).toArray());
				// 		Game.gameLog.terminateBuffer();
				// 	}
				// 	else Game.gameLog.writeBuffer("No piece at selected location");
    			//	return;

			}
		}
		else throw new GameException(GameException.INPUT, "Invalid command.");
    }

    @Override
    public String toString(){
    	return name;
    }
}
