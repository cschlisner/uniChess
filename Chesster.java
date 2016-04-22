package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

public class Chesster {

	private Game game;
	private Team team;

	private List<SmartMove> currentMoves;

	public Chesster(Game g, Team t, int moveDepth){
		team = t;
		game = g;
		SmartMove.MoveDepth = moveDepth;
		currentMoves = new ArrayList<SmartMove>();
		updateCurrentMoves();
	} 

	private void updateCurrentMoves(){
		currentMoves.clear();

		Map<Piece, List<Move>> moveMap = team.getMoveMap();
		Iterator moveMapIterator = moveMap.keySet().iterator();

		while (moveMapIterator.hasNext()){
			Piece p = (Piece)moveMapIterator.next();
			for (Move m : moveMap.get(p))
				currentMoves.add(new SmartMove(m));
		}
	}

	public Move getBestMove(){

		updateCurrentMoves();

		Collections.sort(currentMoves);

		// for (SmartMove m : currentMoves)
		// 	System.out.println(m.toString() + " : "+((float) (m.apmr * (float)m.rating*0.1f)));

		return currentMoves.get(currentMoves.size()-1);
	}
	
	/*
	* TODO:
	* - Check whether move opens more moves for the piece and other pieces
	* - Check whether move closes moves for enemy
	* - Implement King moving strategies
	* - Pawn advancement
	*/

// Convert List<Location> potentialMoves to List<Move> and limit the amount of recursively generated objects to x generations depending on AI depth

public static class SmartMove extends Move implements Comparable<SmartMove>{

		public static int MoveDepth = 3;

		public List<Move> potentialMoves;

		public int rating, attackValue, protectValue, attackCount, protectCount, apmr = 1, skval;

		// this is a hefty operation so we really only want to do it once
		public boolean amprCalculated;
		
		public SmartMove(Move m){
			this(m.game, m.board, m.piece, m.dest, true);
		}
		public SmartMove(Move m, boolean gtmdv){
			this(m.game, m.board, m.piece, m.dest, gtmdv);
		}

		public SmartMove(Game g, Board b, Piece p, Location d){
			this(g, b, p, d, true);
		}

		// gtdv = getTeamDiffValue, if this is specified as true then the getTeamMoveDiffValue() will run (recursively)
		public SmartMove(Game g, Board b, Piece p, Location d, Boolean gtmdv){
			super(g, b, p, d);
			
			potentialMoves = p.getSimulatedMoves(d);
			
			attackValue = getAttackValue();
			protectValue = getProtectValue();

			rating = attackValue+protectValue+isCapture()+isDefend()+canUndermine()+canDiscoverAttack()+canBattery()+canSkewer()+canBeCaptured()+(gtmdv?getTeamMoveDiffValue():0);
		}

		public int getAveragePotentialMoveRating(int depth){
			// System.out.println("enter gapmr depth = "+depth+" | "+String.format("%s@%s > %s", piece.getName(), piece.getStartingPoint(), dest));
			// End of depth, return the average rating of all potential moves
			if (depth == 0){
				int avgPotentialRating = 0;
				for (Move potentialMove : potentialMoves){
						// gets the SmartMove Object for the potential move
						SmartMove potSMV = (new Simulator<Chesster.SmartMove>(this, potentialMove){
							@Override
							public Chesster.SmartMove getData(){
								return new Chesster.SmartMove((Move)data[0], false);
							}
						}).simulate();
					avgPotentialRating += potSMV.rating;
				}
				return (potentialMoves.size()>0)?(int)avgPotentialRating/potentialMoves.size():0;
			}

			// return the average of the averages gathered from potential move trees (generate a new Move for each potential move, then call this function on it and average the results)
			else {
				int avgPotentialTreeAverage = 0;

				int remainingDepth = depth - 1;
				
				for (Move potentialMove : potentialMoves)
					avgPotentialTreeAverage += (new Simulator<Chesster.SmartMove>(this, potentialMove){
													@Override
													public Chesster.SmartMove getData(){
														return new Chesster.SmartMove((Move)data[0], false);
													}
												}).simulate().getAveragePotentialMoveRating(remainingDepth);

				apmr = (potentialMoves.size()>0)?(int)avgPotentialTreeAverage/potentialMoves.size():0;
				amprCalculated = true;
				return apmr;
			}
		}

		// BOTH this AND other SmartMove need to have their apmrs calculated prior to comparison
		@Override 
		public int compareTo(SmartMove other){
			// if (!this.amprCalculated)
			// 	getAveragePotentialMoveRating(MoveDepth);
			// if (!other.amprCalculated)
			// 	other.getAveragePotentialMoveRating(MoveDepth);

			return ( ((float) (apmr * (float)rating*0.1f)) > ((float) (other.apmr * (float)other.rating*0.1f)))?1:-1;
		}

		/**
		* Returns total value of attacks that can be made from dest
		*/
		private int getAttackValue(){
			attackCount = 0;
			int count = 0;
			for (Move m : potentialMoves){
				if (m.destTile.containsEnemy(piece.getTeam())){
					++attackCount;
					count += m.destTile.getOccupator().value;
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
			for (Move m : potentialMoves){
				if (m.destTile.containsFriendly(piece)){
					++protectCount;
					count += m.destTile.getOccupator().value;
				}
			}
			return count;
		}

		/*
		* Returns the total rating of moves gained [positive] or lost [negative] for the team 
		*/
		private int getTeamMoveDiffValue(){
			//System.out.println(String.format("calculating team move diff for %s > %s", piece, dest));

			List<Move> currentTeamMoves = piece.getTeam().getMoveList();

			List<Move> potentialTeamMoves = (new Simulator<List<Move>>(this, piece){
													@Override
													public List<Move> getData(){
														try {
															((Piece)data[0]).getTeam().updateStatus();
														} catch (GameException e){};
														return ((Piece)data[0]).getTeam().getMoveList();
													}
												}).simulate();

			Set<Move> teamMovesLoss = new HashSet<Move>(currentTeamMoves);
			List<Move> meme = new ArrayList<Move>();
			Iterator lmapIterator = teamMovesLoss.iterator();
			while (lmapIterator.hasNext()){
				Move m = (Move)lmapIterator.next();
				if (potentialTeamMoves.contains(m))
					meme.add(m);
			}
			teamMovesLoss.removeAll(meme);

			Set<Move> teamMovesGain = new HashSet<Move>(potentialTeamMoves);
			teamMovesGain.removeAll(currentTeamMoves);	 // now contains all moves newly available for the team if this move is acted out

			int totalRatingLoss = 0, totalRatingGain = 0;

			Iterator mapIterator = teamMovesLoss.iterator();
			while (mapIterator.hasNext()){
				Move m = (Move)mapIterator.next();
				totalRatingLoss += new SmartMove(m, false).rating;
			}

			mapIterator = teamMovesGain.iterator();
			while (mapIterator.hasNext()){
				Move m = (Move)mapIterator.next();
				totalRatingGain += new SmartMove(m, false).rating;
			}

			//System.out.println(String.format("Loss: %s | Gain: %s | Total: %s", totalRatingLoss, totalRatingGain, totalRatingGain-totalRatingLoss));

			// System.exit(0);
			return totalRatingGain-totalRatingLoss;
		}

		private void printMoveList(List<Move> list){
			for (Move m : list){
				System.out.println(m.piece+" : "+m.dest);
	    	 }
		}
		private void printMoveSet(Set<Move> set){
			Iterator mapIterator = set.iterator();
			while (mapIterator.hasNext()){
				Move m = (Move)mapIterator.next();
				System.out.println(m.piece+" : "+m.dest);
			}
		}
		
		/**
		* Returns total value of pieces that are defended by this move (recapture possible)
		*/
		public int isDefend(){
			int wght = 0;
			if (protectCount > 0){
				for (Move m : potentialMoves){
					if (m.destTile.containsFriendly(piece)){
						int potentialFriendDefendCount = (new Simulator<Integer>(this, m.destTile.getOccupator()){
															@Override
															public Integer getData(){
																try {
																	((Piece)data[0]).getTeam().updateStatus();
																} catch (GameException e){}
																return ((Piece)data[0]).defenderCount;
															}
														}).simulate();

						int currentFriendDefendCount = m.destTile.getOccupator().defenderCount;

						wght += (potentialFriendDefendCount - currentFriendDefendCount == 1)?m.destTile.getOccupator().value:0;
					}
				}
			}
			return wght;
		}

		/**
		* Returns value of piece occupating destination tile if this is move will caputre the piece
		*/
		public int isCapture(){
			if (destTile.containsEnemy(piece.getTeam()))
				return destTile.getOccupator().value;
			return 0;
		}

		/**
		* If the piee could be captured, this will negate the 'points' gained from potential attacks and captured piece
		*/
		public int canBeCaptured(){
			for (Piece p : piece.getOpponent().getPieceSet())
				if ((p.getType().equals(Game.PieceType.PAWN) && 
					(new Simulator<Boolean>(this, p){
						@Override
						public Boolean getData(){
							((Piece)data[0]).update();
							return ((Piece)data[0]).canMove(dest);
						}
					}).simulate()) ^ p.canMove(dest)){ // if the enemy piece is a pawn AND can caputure this move (hence the simulation) XOR the enemy piece can move here (a guaranteed capture)
						return -1 * (attackValue + isCapture() + piece.value + skval);
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
				for (Move m : potentialMoves){
					if (m.destTile.containsEnemy(piece.getTeam())){
						if (attackCount == (new Simulator<Integer>(new Move(game, board, m.destPiece, null), piece){ // remove piece that can be attacked from board
												@Override
												public Integer getData(){
													((Piece)data[0]).update();					    // re-calculate attackedPieces list	 
													return ((Piece)data[0]).attackedPieces.size(); // return amount of attacks that can be made with the piece at l removed
												}
											}).simulate()){
							wght += Piece.AVERAGE_PIECE_VAL;
						}
 					}
				}
			}
			skval = wght;
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

			int attackDiff = totalTeamAttacks - (new Simulator<Integer>(this, piece){
													@Override
													public Integer getData(){
														int tta = 0;

														for (Piece p : ((Piece)data[0]).getTeam().getPieceSet())
															tta += p.attackedPieces.size();

														tta -= ((Piece)data[0]).attackedPieces.size();
														return tta;
													}
												}).simulate();
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
				if (destTile.getOccupator().defending != null){
					wght += Piece.AVERAGE_PIECE_VAL/2;
					if (destTile.getOccupator().defending.defenderCount == 1)
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
	}	
}