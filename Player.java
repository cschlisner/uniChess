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
        	chesster = new Chesster(game, team);
    }

    public void readTeamStatus(){
    	Game.gameLog.startBuffer();
    	Map<Piece, List<Location>> moveMap = team.getAllMoves();
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

    public String getBotMove(){
    	String mv = chesster.getBestMove();
    	Game.gameLog.writeBuffer(mv);
    	return mv;
    }

    public boolean readMove(String inStr){
    	String[] tokens = inStr.split(" ");

    	String cmd = tokens[0];
    	try {
			if (Arrays.asList(cmdList).contains(cmd)){
				int index = 1;
				switch (cmd) {
					case "move":
						String pieceName = tokens[index++];
						Location move = new Location(tokens[index++]);
						List<Piece> moveCanidates = new ArrayList<Piece>();
						for (Piece p : team.getPieceSet()){
	    					if (p.getName().equalsIgnoreCase(pieceName) && p.canMove(move))
	    						moveCanidates.add(p);
	    				}
	    				if (moveCanidates.size()==1){
	    					moveCanidates.get(0).moveTo(move);
	    					return true;
	    				}
	    				else if (moveCanidates.size()>1){
	    					if (index == tokens.length){
	    						Game.gameLog.startBuffer();
	    						Game.gameLog.bufferAppendln("Ambiguous move. Options:");
	    						for (int i = 0; i<moveCanidates.size(); ++i)
	    							Game.gameLog.bufferAppendln(String.format("%d: %s at %s", i+1, moveCanidates.get(i).getName(), moveCanidates.get(i).getLocation()));
	    						Game.gameLog.bufferAppendln("Please move again and specify option: \"move "+pieceName+" "+move+" [option]\"");
	    						Game.gameLog.terminateBuffer();
	    						return false;
							}
							int canidate = Integer.parseInt(tokens[index++].replaceAll("[\\D]", ""))-1;
							if (canidate >=0 && canidate < moveCanidates.size()) {
								moveCanidates.get(canidate).moveTo(move);
	    						return true;
							} 
							else {
								Game.gameLog.startBuffer();
	    						Game.gameLog.bufferAppendln("Invalid move option. Options:");
	    						for (int i = 0; i<moveCanidates.size(); ++i)
	    							Game.gameLog.bufferAppendln(String.format("%d: %s at %s", i+1, moveCanidates.get(i).getName(), moveCanidates.get(i).getLocation()));
	    						Game.gameLog.bufferAppendln("Please move again and specify option: \"move "+pieceName+" "+move+" [option]\"");
	    						Game.gameLog.terminateBuffer();
	    						return false;
							}
	    				}
	    				Game.gameLog.writeBuffer("Invalid move");
	    				return false;

					case "get":
						Game.gameLog.startBuffer();
						Location select = new Location(tokens[index++]);
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
						select = new Location(tokens[index++]);
						selectedTilePiece = board.getTile(select).getOccupator();
						if (selectedTilePiece != null)
							Game.gameLog.bufferAppendArray(selectedTilePiece.attackedPieces.toArray());
						else Game.gameLog.writeBuffer("Tile at "+select+" has no piece.");
						Game.gameLog.terminateBuffer();
						return false;

					case "protect":
						Game.gameLog.startBuffer();
						select = new Location(tokens[index++]);
						selectedTilePiece = board.getTile(select).getOccupator();
						if (selectedTilePiece != null)
							Game.gameLog.bufferAppendArray(selectedTilePiece.protectedPieces.toArray());
						else Game.gameLog.writeBuffer("Tile at "+select+" has no piece.");
						Game.gameLog.terminateBuffer();
						return false;

					case "getSim":
						select = new Location(tokens[index++]);
						Location dest = new Location(tokens[index++]);
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
