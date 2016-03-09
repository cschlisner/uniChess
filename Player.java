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
    	Map<Piece, List<Location>> moveMap = team.getMoveMap();
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
    	String mv = "move "+chesster.getBestMove().toString();
    	Game.gameLog.writeBuffer(mv);
    	return mv;
    }

    public boolean readMove(String inStr){
    	String[] tokens = inStr.split(" ");

    	String cmd = tokens[0];
    	try {
			if (Arrays.asList(cmdList).contains(cmd)){
				int index = 1;
				Location select = null, dest = null;
				switch (cmd) {
					case "move":
						try {
							Move move = new Move(game, team, inStr.substring(inStr.indexOf(" ")+1));
							move.attempt();
							return true;
						} catch(GameException e){
							e.writeMessagesToLog();
							return false;
						}
					case "get":
						Game.gameLog.startBuffer();
						try {
							select = new Location(tokens[index++]);
						} catch (GameException e){}
						Piece selectedTilePiece = board.getTile(select).getOccupator();
						if (selectedTilePiece != null)
							Game.gameLog.bufferAppendArray(selectedTilePiece.getMoves());
						else Game.gameLog.writeBuffer("Tile at "+select+" has no piece.");
						Game.gameLog.terminateBuffer();
						return false;

					case "status":
						readTeamStatus();
						return false;

					case "forfeit": 
						forfeit = true;
						return true;
					
					case "draw":
						draw = true;
						return false;

					case "help":
						Game.gameLog.startBuffer();
						Game.gameLog.bufferAppend("Commands: ");
						Game.gameLog.bufferAppendArray(cmdList);
						Game.gameLog.terminateBuffer();
						return false;

					case "attack":
						Game.gameLog.startBuffer();
						try {
							select = new Location(tokens[index++]);
						} catch (GameException e){}
						selectedTilePiece = board.getTile(select).getOccupator();
						if (selectedTilePiece != null)
							Game.gameLog.bufferAppendArray(selectedTilePiece.attackedPieces.toArray());
						else Game.gameLog.writeBuffer("Tile at "+select+" has no piece.");
						Game.gameLog.terminateBuffer();
						return false;

					case "protect":
						Game.gameLog.startBuffer();
						try {
							select = new Location(tokens[index++]);
						} catch (GameException e){}
						selectedTilePiece = board.getTile(select).getOccupator();
						if (selectedTilePiece != null)
							Game.gameLog.bufferAppendArray(selectedTilePiece.protectedPieces.toArray());
						else Game.gameLog.writeBuffer("Tile at "+select+" has no piece.");
						Game.gameLog.terminateBuffer();
						return false;

					case "getSim":
						try {
							select = new Location(tokens[index++]);
							dest = new Location(tokens[index++]);
						} catch (GameException e){}
						if (board.getTile(select).getOccupator() != null){
							Game.gameLog.startBuffer();
							Game.gameLog.bufferAppend(board.getTile(select).getOccupator()+" > "+dest+": ");
							Game.gameLog.bufferAppendArray(board.getTile(select).getOccupator().getSimulatedMoves(dest).toArray());
							Game.gameLog.terminateBuffer();
						}
						else Game.gameLog.writeBuffer("No piece at selected location");
	    				return false;

				}
			}
			Game.gameLog.writeBuffer("Invalid input.");
			return false;
		} catch (IndexOutOfBoundsException e){
			Game.gameLog.writeBuffer("Invalid location");
			return false;
		}
    }

    @Override
    public String toString(){
    	return name;
    }
}
