package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Chesster {
	
	private Game game;
	private Team team;

	private List<Move> currentMoves;

	public Chesster(Game g, Team t){
		team = t;
		game = g;
	} 

	private void updateCurrentMoves(){
		Map<Piece, List<Location>> moveMap = team.getAllMoves();
		Iterator moveMapIterator = moveMap.keySet().iterator();

		while (moveMapIterator.hasNext()){
			Piece p = (Piece)moveMapIterator.next();
			for (Location l : moveMap.get(p))
				currentMoves.add(new Move(game, p, l));
		}
	}

	public boolean getBestMove(){
		return false;
		// sort currentMoves based upon ratings
		//
		// if 2 or more moves have the same rating (at top of list, meaning they are both the best possible move)
		// then for each of those moves (m1), make a new list of Move objects representing
		// the possible moves (m2) for the piece if m1 was chosen. Then get the average rating for [m2]
		// and return the m1 with the highest average rating of [m2] moves.
		//
		// If you still have two competing "best moves" with the same average rating of [m2] 
		// then repeat the process for a set of [m3]
		//
		// the depth of play of the AI (how may moves ahead it calculates for) can be set by calculating for all of [m2] or [m3] 
		// and not just the average scores. Then if there are still competing moves, [m4] or [m5] will be calculated

	}
	
}

class Move {
		private Game game;
		private Board board;
		
		public Piece piece;
		public Location dest;

		private List<Location> potentialMoves;

		private int rating, attackCount, protectCount;
		
		public Move(Game g, Piece p, Location d){
			game = g;
			board = g.getBoard();
			piece = p;
			dest = d;
			
			potentialMoves = p.getSimulatedMoves(d);
			
			attackCount = getAttackCount();
			protectCount = getProtectCount();

			rating = getRating();

		}

		private int getAttackCount(){
			int count = 0;
			for (Location l : potentialMoves)
				if (board.getTile(l).containsEnemy(piece))
					++count;
			return count;
		}
		private int getProtectCount(){
			int count = 0;
			for (Location l : potentialMoves)
				if (board.getTile(l).containsFriendly(piece))
					++count;
			return count;
		}

		public int canAttack(){
			if (attackCount>0)
				return 20;
			return 0;
		}

		public int canBeCaptured(){
			for (Piece p : piece.getOpponent().getPieceSet())
				if ((p.getType().equals(Game.PieceType.PAWN) && p.canMove(dest) && dest.x != p.getLocation().x) ^ p.canMove(dest)) 
					return -20;
			return 0;
		}

		public int canFork(){
			return (attackCount >= 2)?20:0;
		}

		public int canSkewer(){
			int wght = 0;
			if ((piece.ofType(Game.PieceType.ROOK) || piece.ofType(Game.PieceType.BISHOP) || piece.ofType(Game.PieceType.QUEEN)) && attackCount > 0){
				for (Location l : potentialMoves){
					if (board.getTile(l).containsEnemy(piece)){

						Piece tmp = board.getTile(l).getOccupator();

						board.getTile(l).setOccupator(null); // remove piece that can be attacked from board

						piece.update();						 // re-calculate valid moves

						if (attackCount == piece.attackedPieces.size()) // if the amount of pieces this piece can attack stays constant, then there is a skewer
							wght += 40;

						board.getTile(l).setOccupator(tmp); // put the removed enemy back on the board.

 					}
				}
			}
			return wght;
		}

		public int canBattery(){
			return 0;
			
		}

		public int canDiscoverAttack(){
			return 0;
			
		}
		public int canUndermine(){
			return 0;
			
		}
		public int canOverload(){
			return 0;
			
		}
		public int canDeflect(){
			return 0;
			
		}
		public int canPin(){
			return 0;
			
		}
		public int canInterfere(){
			return 0;
			
		}
		public int canProtect(){
			return 0;
		}

		/**
		* Give a rating 0-100 to a this move based upon: 
		* forks, skewers, batteries, discovered attacks, undermining, overloading, deflection, pins, or interference
		* depending on the difficulty
		*
		* @return move rating
		*/
		private int getRating(){

			return 0;
		}	
	}