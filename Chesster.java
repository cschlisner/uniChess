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
		// and not just the average scores. Then if there are still competing moves, averages for [m4] or [m5] will be calculated

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


		/**
		* Returns amount of attacks that can be made from dest
		*/
		private int getAttackCount(){
			int count = 0;
			for (Location l : potentialMoves)
				if (board.getTile(l).containsEnemy(piece))
					++count;
			return count;
		}

		/**
		* Returns number of protects that are enabled from dest
		*/
		private int getProtectCount(){
			int count = 0;
			for (Location l : potentialMoves)
				if (board.getTile(l).containsFriendly(piece))
					++count;
			return count;
		}

		/**
		* Returns 50 'points' if this is move will caputre a piece
		*/
		private int isCapture(){
			if (board.getTile(dest).containsEnemy(piece))
				return 50;
			return 0;
		}

		/**
		* Returns 20 'points' for each piece which can be attacked
		*/
		public int canAttack(){
			return attackCount*20;
		}

		/**
		* If the piee could be captured, this will negate the 'points' gained from potential attacks and captured piece
		*/
		public int canBeCaptured(){
			for (Piece p : piece.getOpponent().getPieceSet())
				if ((p.getType().equals(Game.PieceType.PAWN) && p.canMove(dest) && dest.x != p.getLocation().x) ^ p.canMove(dest)) 
					return (-1 * (attackCount * 20)) + isCapture();
			return 0;
		}

		/**
		* In chess, a skewer is an attack upon two pieces in a line and is similar to a pin.
		* This will return 10 points for every skewer. 
		*/
		public int canSkewer(){
			int wght = 0;
			if ((piece.ofType(Game.PieceType.ROOK) || piece.ofType(Game.PieceType.BISHOP) || piece.ofType(Game.PieceType.QUEEN)) && attackCount > 0){
				for (Location l : potentialMoves){
					if (board.getTile(l).containsEnemy(piece)){
						if (attackCount == board.runMoveSimulation(new Board.MoveSimulation<Integer>(piece, l, null){ // remove piece that can be attacked from board
							@Override
							public Integer getSimulationData(){
								dataPiece.update();					    // re-calculate attackedPieces list	 
								return dataPiece.attackedPieces.size(); // return amount of attacks that can be made with the piece at l removed
							}
						}))
							wght += 10;

 					}
				}
			}
			return wght;
		}

		/**
		* A battery in chess is a formation that consists of two or more pieces on the same rank, file, or diagonal.
		* This will return 2 points if it is a battery.
		*/
		public int canBattery(){
			for (int x = 0; x < 8; ++x){
				for (int y = 0; y < 8; ++y){
					if (board.getTile(x, dest.y).containsFriendly(piece) // same file
						|| board.getTile(dest.x, y).containsFriendly(piece) // same rank
						|| board.getTile(x, y).containsFriendly(piece) // y = x diagonal
						|| board.getTile(x, 7-y).containsFriendly(piece)) // y = -x diagonal
						return 2;
				}		
			}
			return 0;
		}

		/**
		* In chess, a discovered attack is an attack revealed when one piece moves out of the way of another.
		* This will return 20 points per net difference in total team attacks. If there is a net loss of 2 moves, 
		* then this will return -40. (same point value as an attack, however this will not be cancelled out if this move can be captured)
		*/
		public int canDiscoverAttack(){
			int totalTeamAttacks = 0;
			
			for (Piece p : piece.getTeam().getPieceSet())
				totalTeamAttacks += p.attackedPieces.size();
			totalTeamAttacks -= piece.attackedPieces.size();

			int attackDiff = totalTeamAttacks - board.runMoveSimulation(new Board.MoveSimulation<Integer>(piece, piece.getLocation(), dest){
													@Override
													public Integer getSimulationData(){
														int tta = 0;

														for (Piece p : dataPiece.getTeam().getPieceSet())
															tta += p.attackedPieces.size();

														tta -= dataPiece.attackedPieces.size();
														return tta;
													}
												});
			return 20*attackDiff;
		}
		
		/**
		* Undermining is a chess tactic in which a defensive piece is captured, 
		*leaving one of the opponent's pieces undefended or under-defended.
		* 
		* Will return 5 points if it leaves the defended piece under-defended 
		* (more than one defender left) or 7 points if it leave the piece undefended
		*/
		public int canUndermine(){
			int wght = 0;
			if (isCapture()>0){
				if (board.getTile(dest).getOccupator().defending != null){
					wght += 5;
					if (board.getTile(dest).getOccupator().defending.defenderCount == 1)
						wght += 2;
				}
			}
			return wght;
		}

		/**
		* 
		*/
		public int canOverload(){
			return 0;
			
		}
		
		/**
		* 
		*/
		public int canDeflect(){
			return 0;
			
		}
		
		/**
		* 
		*/
		public int canPin(){
			return 0;
			
		}
		
		/**
		* 
		*/
		public int canInterfere(){
			return 0;
			
		}
		
		/**
		* 
		*/
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