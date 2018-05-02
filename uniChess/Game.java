package uniChess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
*	An object for creating, maintaining, and communicating with a chess game.
*
*/
public class Game implements Serializable {

	/** Type that the Game object will return on game advancement */
	public enum GameEvent {OK, AMBIGUOUS, INVALID, ILLEGAL, CHECK, CHECKMATE, STALEMATE, DRAW}
	
	/** Type of game pieces. */

	/** Enables unicode output in string representations. */
	public static boolean unicode = true;

	/** Restricts unicode characters to filled in type. */
	public static boolean useDarkChars = false;

	/** Outputs GameException stacktrace to stdout. */
	public static boolean logging = true;

	private boolean whiteMove = true;

	/** Game Identifier */
	public String ID;

	private String gameString = "";

	private List<Board> boards = new ArrayList<>();

	private Player white, black;

	private Move lastMove;

	/** 
	*	Creates a new Game between the supplied players starting from the gamestate
	*	supplied through the gameString. This allows a game to be continued from 
	*	a specific state. (See {@link #getGameString() getGameString} method).
 	*
	*	Note: this will not check the legality of these moves. If an invalid move
	*	is supplied in the gamestring then it will be made regardless. If a parsing error
 	*	occurs, the game will stop at the move previous to the inparsable move.
 	*
	*	@param player1 The first player
	*	@param player2 The second player
	*	@param gameString The series of moves to be performed
	*	before the game starts.
	*/
	public Game(Player player1, Player player2, String gameString){
		white = (player1.color == Color.WHITE) ? player1 : player2;
		black = (player1.color == Color.WHITE) ? player2 : player1;
		white.registerGame(this);
		black.registerGame(this);

		boards.add(new Board());


		for (String in : gameString.split(",")){
			Move move;
			try {
				move = Move.parseMove(getCurrentBoard(), getCurrentPlayer().color, in);
				lastMove = move;
			} catch (Exception e){
				return;
			}
			boards.add(move.getSimulation());

			whiteMove = !whiteMove;

			this.gameString += move.getANString()+",";
		}
	}
	
	/** 
	*	Creates a Game between the supplied players. One player must have a Color 
	*	of {@code Game.Color.WHITE} and one must have a Color of {@code Game.Color.BLACK}.
	*
	*	@param player1 The first player
	*	@param player2 The second player
	*/
	public Game(Player player1, Player player2){
		white = (player1.color == Color.WHITE) ? player1 : player2;
		black = (player1.color == Color.WHITE) ? player2 : player1;
		white.registerGame(this);
		black.registerGame(this);

		boards.add(new Board());
	}

	/**
	*	Returns the moves made (in algebraic notation) so far in the game, separated
	*	by commas. The resulting string can be used to make a new Game object that
	*	will repeat the moves outlined in this String upon creation.
	* 
	*	@return The game string
	*/
	public String getGameString(){
		if (gameString.isEmpty()) return "";
		return gameString.substring(0, gameString.length()-1);
	}

	/**
	*	Returns a List of all generated boards in the game. 
	*
	*	@return The list of boards.
	*/
	public List<Board> getBoardList(){
		return boards;
	}

	/**
	*	Sets the current game board to the supplied Board.  
	*
	*	@param board The board to be used.
	*/
	public void setcurrentBoard(Board board){
		boards.add(board);
	}

	/**
	*	Returns the current game board.  
	*
	*	@return The current board.
	*/
	public Board getCurrentBoard(){
		return boards.get(boards.size()-1);
	}

	/**
	*	Returns the Player-to-move.  
	*
	*	@return The current player.
	*/
	public Player getCurrentPlayer(){
		return whiteMove ? white : black;
	}

	/**
	*	Returns the Player who moved last.  
	*
	*	@return The player who moved last.
	*/
	public Player getDormantPlayer(){
		return whiteMove ? black : white;
	}

	/**
	*	Returns the Player controlling a given color. 
	*	
	*	@param color The color to get the player for
	*	@return The player controlling the given color.
	*/
	public Player getPlayer(int color){
		return (color == Color.BLACK) ? black : white;
	}

	/**
	 * Returns the last move made in the game
	 * @return
	 */
	public Move getLastMove(){
		return lastMove;
	}

	/**
	 * Converts single character representations of pieces to unicode representation.
	 * lowercase is mapped to white pieces and uppercase is mapped to black pieces.
	 * i.e.
	 * "p" -> ♙
	 * "Q" -> ♛
	 * @param p letter to convert
	 * @return unicode representation of p, null if character is not one of [p,q,r,n,b,k]
	 */
	public static String getUnicode(char p){
		int[] unicodeChars;
		if (Character.isUpperCase(p))
			unicodeChars = new int[]{9823,9820,9822,9821,9819,9818};
		else unicodeChars = new int[]{9817,9814,9816,9815,9813,9812};

		switch (Character.toLowerCase(p)){
			case 'p':
				return new String(Character.toChars(unicodeChars[0]));
			case 'r':
				return new String(Character.toChars(unicodeChars[1]));
			case 'n':
				return new String(Character.toChars(unicodeChars[2]));
			case 'b':
				return new String(Character.toChars(unicodeChars[3]));
			case 'q':
				return new String(Character.toChars(unicodeChars[4]));
			case 'k':
				return new String(Character.toChars(unicodeChars[5]));
			default:
				return null;
		}
	}

	/**
	*	<p>
	*	Attempts to advance the game by one move specified by the supplied algebraic
	*	notation string. If the move is valid, legal, and does not put the other 
	*	player in check, checkmate, or stalemate, then this method will perform the
	*	move, add the newly generated board the the board list, and switch the current 
	*	player, and return {@code GameEvent.OK}. Otherwise, it will return the 
	*	{@code GameEvent} type associated with the corresponding event.  
	*	<p>
	*	If the returned value is {@code GameEvent.ILLEGAL},
	*	{@code GameEvent.INVALID}, or {@code GameEvent.AMBIGUOUS} then the game 
	*	was not successfully advanced and the turn still needs to be completed with
	*	a valid move. 
	*	<p>
	*	{@code GameEvent.DRAW} will be returned only when the draw field of each 
	*	Player is true. Since there is no standard convention for Draws, how this 
	*	is implemented is up to the developer.
	*	
	*	@param in The algebraic notation move
	*	@return The player who moved last.
	*/
	public GameEvent advance(String in){
		Move move;
		try {
			if (white.draw && black.draw) {
				return GameEvent.DRAW;
			}

			move = Move.parseMove(getCurrentBoard(), getCurrentPlayer().color, in);

//			Board cb = getCurrentBoard();
//			List<Move> legal = cb.getLegalMoves(getCurrentPlayer());
//
//            if (!legal.contains(move))
//				return GameEvent.ILLEGAL;

			boards.add(move.getSimulation());
			getCurrentBoard().processLegal();

			whiteMove = !whiteMove;

			gameString += move.getANString()+",";

			lastMove = move;

			if (getCurrentBoard().playerHasCheck(getDormantPlayer()) && getCurrentBoard().getLegalMoves(getCurrentPlayer()).isEmpty())
				return GameEvent.CHECKMATE;

			else if (getCurrentBoard().getLegalMoves(getCurrentPlayer()).isEmpty()) // This is a stalemate, which results in Draw
				return GameEvent.DRAW;

			else if (getCurrentBoard().playerHasCheck(getDormantPlayer()))
				return GameEvent.CHECK;
		} catch (GameException ge){
				if (logging)
					ge.printStackTrace();
				switch (ge.getType()) {
					
					case GameException.INVALID_MOVE:
						return GameEvent.INVALID;
					
					case GameException.AMBIGUOUS_MOVE:
						return GameEvent.AMBIGUOUS;	

				}
		}
		return GameEvent.OK;
	}
}
