package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

public class Chesster {
	
	private Game game;
	private Team team;

	private List<Move> currentMoves;

	public Chesster(Game g, Team t){
		team = t;
		game = g;
		currentMoves = new ArrayList<Move>();
		updateCurrentMoves();
	} 

	private void updateCurrentMoves(){
		currentMoves.clear();

		Map<Piece, List<Location>> moveMap = team.getAllMoves();
		Iterator moveMapIterator = moveMap.keySet().iterator();

		while (moveMapIterator.hasNext()){
			Piece p = (Piece)moveMapIterator.next();
			for (Location l : moveMap.get(p))
				currentMoves.add(new Move(game, p, l));
		}
	}

	public String getBestMove(){
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

		updateCurrentMoves();

		Collections.sort(currentMoves);

		// for (Move m : currentMoves)
		// 	System.out.println(m.toString() + " : "+m.rating+" | av: "+m.attackValue+"| pv: "+m.protectValue+"| id: "+m.isDefend()+"| ic: "+m.isCapture()+"| cu: "+m.canUndermine()+"| da: "+m.canDiscoverAttack()+"| cb: "+m.canBattery()+"| cs: "+m.canSkewer()+"| cbc:"+m.canBeCaptured());

		Move bestMove = currentMoves.get(currentMoves.size()-1);

		List<Move> M1 = new ArrayList<Move>();
		for (Move m : currentMoves)				// check for multiple 'best moves' [m1]
			if (m.rating == bestMove.rating)
				M1.add(m);
		if (!M1.isEmpty())
			for (Move m : M1)
				if (m.getAveragePotentialMoveRating() > bestMove.getAveragePotentialMoveRating())
					bestMove = m;				// set the bestMove to the move with the highest average of potential move ratings

		return bestMove.toString();
	}
	
	/*
* TODO:
* - Check whether move opens more moves for the piece and other pieces
* - Check whether move closes moves for enemy
* - Implement King moving strategies
* - Pawn advancement
*/

// Convert List<Location> potentialMoves to List<Move> and limit the amount of recursively generated objects to x generations depending on AI depth

public static class Move implements Comparable<Move>{
		private Game game;
		private Board board;
		
		public Piece piece;
		public Location dest;

		public List<Location> potentialMoves;

		public int rating, attackValue, protectValue, attackCount, protectCount;
		
		public Move(Game g, Piece p, Location d){
			game = g;
			board = g.getBoard();
			piece = p;
			dest = d;
			
			potentialMoves = p.getSimulatedMoves(d);

			if (potentialMoves == null){
				System.out.println(this.toString());
			}
			
			attackValue = getAttackValue();
			protectValue = getProtectValue();

			rating = getRating();
		}

		//
		// probably delete this 
		//
		public int getAveragePotentialMoveRating(){
			List<Move> M2 = new ArrayList<Move>();

			int avgPotentialRating = 0;

			// calculating average of [M2]
			for (Location potentialMoveLocation : potentialMoves){
				Move potentialMove = 
					board.runMoveSimulation(new Board.MoveSimulation<Chesster.Move>(potentialMoveLocation, piece.getLocation(), dest){
						@Override
						public Chesster.Move getSimulationData(){
							return new Chesster.Move(game, board.getTile(dest).getOccupator(), dataLocation);
						}
					});

				// to calculate for [M3]:
				//  m2avgrating; 
				//	for (Location potentialM3 : potentialMove.potentialMoves){
				//	 Move potentialMoveM3 = run simulation( > generate move object)
				//  }

				avgPotentialRating += potentialMove.rating;
			}

			return (potentialMoves.size()>0)?(int)avgPotentialRating/potentialMoves.size():0;
		}

		@Override 
		public int compareTo(Move other){
			return (this.rating > other.rating)?1:-1;
		}

		@Override
		public String toString(){
			return String.format("move %s %s", piece.getName(), dest);
		}

		/**
		* Returns total value of attacks that can be made from dest
		*/
		private int getAttackValue(){
			attackCount = 0;
			int count = 0;
			for (Location l : potentialMoves){
				if (board.getTile(l).containsEnemy(piece)){
					++attackCount;
					count += board.getTile(l).getOccupator().value;
				}
			}
			return count;
		}
		

		/**
		* Returns total value of pieces that are protected from dest
		*/
		private int getProtectValue(){
			protectCount = 0;
			int count = 0;
			for (Location l : potentialMoves){
				if (board.getTile(l).containsFriendly(piece)){
					++protectCount;
					count += board.getTile(l).getOccupator().value;
				}
			}
			return count;
		}
		
		/**
		* Returns total value of pieces that are defended by this move (recapture possible)
		*/
		public int isDefend(){
			int wght = 0;
			if (protectCount > 0){
				for (Location l : potentialMoves){
					if (board.getTile(l).containsFriendly(piece)){

						int potentialFriendDefendCount = board.runMoveSimulation(new Board.MoveSimulation<Integer>(board.getTile(l).getOccupator(), piece.getLocation(), dest){
							@Override
							public Integer getSimulationData(){
								board.getTile(dest).getOccupator().update();
								dataPiece.update();
								return dataPiece.defenderCount;
							}
						});

						int currentFriendDefendCount = board.getTile(l).getOccupator().defenderCount;

						wght += (potentialFriendDefendCount - currentFriendDefendCount == 1)?board.getTile(l).getOccupator().value:0;
					}
				}
			}
			return wght;
		}

		/**
		* Returns value of piece occupating destination tile if this is move will caputre the piece
		*/
		public int isCapture(){
			if (board.getTile(dest).containsEnemy(piece))
				return board.getTile(dest).getOccupator().value;
			return 0;
		}

		/**
		* If the piee could be captured, this will negate the 'points' gained from potential attacks and captured piece
		*/
		public int canBeCaptured(){
			for (Piece p : piece.getOpponent().getPieceSet())
				if ((p.getType().equals(Game.PieceType.PAWN) && 
					board.runMoveSimulation(new Board.MoveSimulation<Boolean>(p, piece.getLocation(), dest){
						@Override
						public Boolean getSimulationData(){
							dataPiece.update();
							return dataPiece.canMove(dest);
						}
					}))	^ p.canMove(dest)){ // if the enemy piece is a pawn AND can caputure this move (hence the simulation) XOR the enemy piece can move here (a guaranteed capture)
						return -1 * (attackValue + isCapture() + piece.value);
					}
			return 0;
		}

		/**
		* In chess, a skewer is an attack upon two pieces in a line and is similar to a pin.
		* This will return 4 points (average[ish] piece value) for every skewer. 
		*/
		public int canSkewer(){
			int wght = 0;
			if ((piece.ofType(Game.PieceType.ROOK) || piece.ofType(Game.PieceType.BISHOP) || piece.ofType(Game.PieceType.QUEEN)) && attackValue > 0){
				for (Location l : potentialMoves){
					if (board.getTile(l).containsEnemy(piece)){
						if (attackCount == board.runMoveSimulation(new Board.MoveSimulation<Integer>(piece, l, null){ // remove piece that can be attacked from board
												@Override
												public Integer getSimulationData(){
													dataPiece.update();					    // re-calculate attackedPieces list	 
													return dataPiece.attackedPieces.size(); // return amount of attacks that can be made with the piece at l removed
												}
											})){
							wght += Piece.AVERAGE_PIECE_VAL;
						}
 					}
				}
			}
			return wght;
		}

		/**
		* A battery in chess is a formation that consists of two or more pieces on the same rank, file, or diagonal.
		* This will return 1 point if it is a battery.
		*/
		public int canBattery(){
			for (int x = 0; x < 8; ++x){
				for (int y = 0; y < 8; ++y){
					if (board.getTile(x, dest.y).containsFriendly(piece) // same file
						|| board.getTile(dest.x, y).containsFriendly(piece) // same rank
						|| board.getTile(x, y).containsFriendly(piece) // y = x diagonal
						|| board.getTile(x, 7-y).containsFriendly(piece)) // y = -x diagonal
						return 1;
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
			return Piece.AVERAGE_PIECE_VAL*attackDiff;
		}
		
		/**
		* Undermining is a chess tactic in which a defensive piece is captured, 
		*leaving one of the opponent's pieces undefended or under-defended.
		* 
		* Will return (average_piece_val / 2) points if it leaves the defended piece under-defended 
		* (more than one defender left) or average_piece_val points if it leave the piece undefended
		*/
		public int canUndermine(){
			int wght = 0;
			if (isCapture()>0){
				if (board.getTile(dest).getOccupator().defending != null){
					wght += Piece.AVERAGE_PIECE_VAL/2;
					if (board.getTile(dest).getOccupator().defending.defenderCount == 1)
						wght += Piece.AVERAGE_PIECE_VAL/2;
				}
			}
			return wght;
		}

		/**
		* idfk
		*/
		public int canOverload(){

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
		* Give a rating 0-100 to a this move based upon: 
		* forks, skewers, batteries, discovered attacks, undermining, overloading, deflection, pins, or interference
		* depending on the difficulty
		*
		* @return move rating
		*/
		private int getRating(){
			return attackValue+protectValue+isCapture()+isDefend()+canUndermine()+canDiscoverAttack()+canBattery()+canSkewer()+canBeCaptured();
		}	
	}	
}