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
		public Piece piece;
		public Location dest;
		private int rating, attackCount, protectCount;
		
		public Move(Game g, Piece p, Location d){
			game = g;
			piece = p;
			dest = d;
			rating = getRating();

			attackCount = p.attackedPieces.size();
			protectCount = p.protectedPieces.size();
		}

		public boolean canAttack(){
			return (attackCount>0);
		}

		public boolean canBeCaptured(){
			for (Piece p : piece.getOpponent().getPieceSet())
				if ((p.getType().equals(Game.PieceType.PAWN) && p.canMove(dest) && dest.x != p.getLocation().x) ^ p.canMove(dest)) 
					return true;
			return false;
		}

		public boolean canFork(){
			return (attackCount >= 2);
		}

		public boolean canSkewer(){
			return false;
		}

		public boolean canBattery(){
			return false;
			
		}

		public boolean canDiscoverAttack(){
			return false;
			
		}
		public boolean canUndermine(){
			return false;
			
		}
		public boolean canOverload(){
			return false;
			
		}
		public boolean canDeflect(){
			return false;
			
		}
		public boolean canPin(){
			return false;
			
		}
		public boolean canInterfere(){
			return false;
			
		}

		/**
		* Give a rating 0-100 to a this move based upon: 
		* forks, skewers, batteries, discovered attacks, undermining, overloading, deflection, pins, or interference
		* depending on the difficulty
		*
		* @return move rating
		*/
		private int getRating(){
			
			// return (int) (methods true / total methods) * 100.0f

			return 0;
		}	
	}