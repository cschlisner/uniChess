package uniChess;

import org.json.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.awt.image.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Game {
	public static enum GameEvent {OK, AMBIGUOUS, INVALID, ILLEGAL, CHECK, CHECKMATE, STALEMATE, DRAW;}
	public static enum PieceType {PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING;}
	public static enum Color {WHITE, BLACK;}
	public static boolean unicode = true;
	public boolean whiteMove = true;
	private List<Board> boards = new ArrayList<>();

	public Player white, black;

	public Game(Player player1, Player player2){
		white = (player1.color.equals(Color.WHITE) ? player1 : player2);
		black = (player1.color.equals(Color.WHITE) ? player2 : player1); 

		boards.add(new Board());

		getCurrentBoard().printSelf(!whiteMove);
	}

	public List<Board> getBoardList(){
		return boards;
	}

	public void setcurrentBoard(Board board){
		boards.add(board);
	}

	public Board getCurrentBoard(){
		return boards.get(boards.size()-1);
	}

	public Player getCurrentPlayer(){
		return whiteMove ? white : black;
	}

	public Player getDormantPlayer(){
		return whiteMove ? black : white;
	}

	public GameEvent advance(String in){
		try {

			if (white.draw && black.draw)
				return GameEvent.DRAW;

			Move move = Move.parseMove(getCurrentBoard(), getCurrentPlayer().color, in);
			
			List<Move> legal = getLegalMoves();
			
			if (!legal.contains(move))
				return GameEvent.ILLEGAL;

			boards.add(getCurrentBoard().performMove(move));

			whiteMove = !whiteMove;

			if (dormantPlayerHasCheck() && getLegalMoves().isEmpty())
				return GameEvent.CHECKMATE;

			else if (getLegalMoves().isEmpty())
				return GameEvent.STALEMATE;

			else if (dormantPlayerHasCheck())
				return GameEvent.CHECK;

			return GameEvent.OK;

		} catch (GameException ge){
				
				switch (ge.getType()) {
					
					case GameException.INVALID_MOVE:
						return GameEvent.INVALID;
					
					case GameException.AMBIGUOUS_MOVE:
						return GameEvent.AMBIGUOUS;	

				}
		}

	}

	private boolean dormantPlayerHasCheck(Board board){
		// Tests for Move-into-Check. That is, a piece on the team-to-move is able to capture a king.
		Piece p;
		for (Move m : board.getValidMoves(getDormantPlayer().color)){
			p = board.getTile(m.destination).getOccupator();
			if (p != null && p.ofType(PieceType.KING))
				return true;
		}
		return false;
	}

	public List<Move> getLegalMoves() throws GameException{
		List<Move> validMoves = getCurrentBoard().getValidMoves(getCurrentPlayer().color);
		List<Move> legalMoves = new ArrayList<>();
		for (Move m : validMoves){
			
			if (!dormantPlayerHasCheck(getCurrentBoard().performMove(m)))
				legalMoves.add(m);

		}

		return legalMoves;
	}
}
