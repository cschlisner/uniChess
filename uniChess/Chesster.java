package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
*   An object representing a Simulated Player in a chess game. 
*/
public class Chesster <T> extends Player <T>{
    Game game;

    private int AI_DEPTH = 2;
    private boolean dynamic = true;

    public Chesster(T id, Game.Color c){
        super(id, c);
    }

    public void registerGame(Game g){
        this.game = g;
    }

    public void setStaticAIDepth(int val){
        AI_DEPTH = val;
        dynamic = false;
    }

    /**
    *   Returns the best possible legal move for the bot based on individual 
    *   tactics and strategy (logarithmic sum of average tactical value of future moves).
    *
    *   @return the best move
    */
    public String getMove(){
        List<Move> legal = game.getLegalMoves(game.getCurrentBoard(), this);
        
        List<SmartMove> smartMoves = new ArrayList<>();
        
        for (Move move : legal)
            smartMoves.add(new SmartMove(move));

        processStrategicValue(smartMoves);

        Collections.sort(smartMoves);

        return smartMoves.get(smartMoves.size()-1).toString();
    }


    /**
    *   Processes strategic value of each move in a list.
    *   
    *   This is done by calculating the sum of the average tactical values on each node depth 
    *   below the node being calculated for, multiplied by the inverse of the node depth, up until a certain depth.
    *   
    *   The multiplication by the inverse node depth accounts for loss in prediction accuracy over time.
    *   This means that the average tactical value of submoves of a move will be multiplied by 1/2, and the 
    *   average tactical value of submoves of those submoves will be multiplied by 1/3 due to the fact that
    *   the game will have probably changed drastically by then and the current move should factor more into the
    *   final decision.
    *
    *   @param moves The list of moves to process
    */
    public void processStrategicValue(List<SmartMove> moves){
        int prog = 0;
        if (dynamic){
            if (moves.size() >= 30)
                AI_DEPTH = 1;
            else if (moves.size() >= 10)
                AI_DEPTH = 2;
            else AI_DEPTH = 3;
        }
        for (SmartMove sm : moves){
            printProgress(prog++, moves.size(), sm);
            processTacticalValue(sm, this);

            List<List<Move>> submoveTree = getMoveTree(sm, AI_DEPTH, 0);

            // calculate average tactical values for each node level
            double[] avgTactical = new double[AI_DEPTH+1];

            avgTactical[0] = sm.tacticalValue;

            int listLength = 1, listIndex = 0;

            for (int i = 1; i < AI_DEPTH+1; ++i){
                int avg = 0;
                int combinedListLengths = 0;

                for (int j = listIndex; j < listIndex+listLength; ++j){
                    if (j == submoveTree.size()) break;
                    for (Move move : submoveTree.get(j)){
                        SmartMove subMoveSM = new SmartMove(move);
                        processTacticalValue(subMoveSM, this);
                        avg += subMoveSM.tacticalValue;
                    }
                    combinedListLengths+=submoveTree.get(j).size();
                }

                if (combinedListLengths == 0)
                    break;

                avgTactical[i] = avg / combinedListLengths;
                listIndex += listLength;
                listLength = combinedListLengths;
            }

            // apply a weight based on inverse tree depth
            double sum = 0;
            for (int i = 0; i < AI_DEPTH+1; ++i)
                sum += (double)(1 / (i+1))*(avgTactical[i]);
            
            // set the strategic value to the sum of average weighted tacticals of each tree depth
            sm.strategicValue = sum;
        }
        System.out.println("");
    }

    public void printProgress(int prog, int total, Move m){
        double percent = (double)prog/total;
        double percentFrom20 = 20 * percent;
        System.out.print("\rThinking [");
        for (int i = 0; i < 20; ++i){
            if (i <= (int)percentFrom20)
                System.out.print("=");
            else System.out.print(" ");
        }
        System.out.print("] "+m+"\r");
    }

    public void printMoveTree(List<List<Move>> tree){
        int depth = 0;
        for (List<Move> list : tree){
            for (Move move : list ){
                for (int i = 0; i < depth; ++i)
                    System.out.print("::");  
                System.out.println(move.getANString());
            }
            ++depth;
        }
    }

    /**
    *   Returns all legal submoves recursively to a given depth as a list of lists of submoves
    *   Tree format: 
    *    { 
    *       [A, B, C]
    *           [AA, AB, AC ... ]
    *           [BA, BB, BC ... ]
    *           [CA, CB, CC ... ]
    *               [AAA, AAB, AAC ... ]
    *               [ABA, ABB, ABC ... ]
    *                   ....
    *                   ....
    *                   ....
    *    }
    *
    *   @return The tree of moves
    */
    public List<List<Move>> getMoveTree(Move m, int max, int depth){
        List<List<Move>> res = new ArrayList<List<Move>>();
        
        if (depth < max){
            List<Move> submoves = getSubMoves(m);
            res.add(submoves);

            for (Move sub : submoves)
                res.addAll(getMoveTree(sub, max, depth+1));
        }

        return res;
    }

    /**
    *   Returns all legal submoves available if a given move by a given player were to be performed
    *   and the opponent were to perform their highest tactically rated move.
    *
    *   @return The list of possible submoves
    */
    public List<Move> getSubMoves(Move m){
        // perform the move
        Board sim = m.board.performMove(m);

        List<Move> opponentLegal = game.getLegalMoves(sim, game.getOpponent(this));

        // CHECKMATE BITCH
        if (opponentLegal.isEmpty()){
            m.CHECKMATE = true;
            return new ArrayList<Move>();
        }

        // perform the best possible move in response as opponent
        Board sim2 = sim.performMove(getBestTacticalOpponentMove(opponentLegal));
        
        List<Move> res = new ArrayList<>();
        for (Move legal : game.getLegalMoves(sim2, this)){
            if (legal.origin.equals(m.destination))
                res.add(legal);
        }
        return res;
    }

    private Move getBestTacticalOpponentMove(List<Move> moveList){
        SmartMove best = new SmartMove(moveList.get(0));
        
        for (Move m : moveList){
            SmartMove sm = new SmartMove(m);
            processTacticalValue(sm, game.getOpponent(this));
            if (sm.tacticalValue > best.tacticalValue)
                best = sm;
        }

        return best;
    }

    /**
    *   Assigns tactical value to each move in a list.
    *   
    *   Calculates potential material (total value of material on the enemy team that would be under attack if this move were to be made), 
    *              skewer (total value of skewered material if this move were to be made and happened to skewer any pieces)
    *              discover (difference in total material value of currently attacked pieces and total material that would be attacked)
    *
    *
    *   The method combines these three amounts into a single OffensiveValue variable. 
    *   
    *   If the move is incapturable: the resulting tactic value is set to the Offensive variable.
    *   If the move can be captured and a recapture is impossible: the offensive value is negated and the resulting tactic value is 0.
    *   If the move can be captured, but a recapture is possible: a sacrifice will be made if the piece being captured is of less material
    *                                                             value than the piece being counter-captured. The resulting tactic value is
    *                                                             the net difference in material value between the pieces. If the piece being 
    *                                                             captured is of more value than the recapture, the tactic value is 0.
    *   
    *   @param SmartMove to assign strategic value to
    */
    private void processTacticalValue(SmartMove sm, Player p){
        // flags for calculating result
        int potentialMaterial = 0, skewer = 0, discover = 0, capturable = 0;
        boolean checkMove = false;

        // simulation of the move (the resulting board if the move were performed)
        Board sim = sm.board.performMove(sm);

        // legal moves in the simulated board
        List<Move> simLegal = game.getLegalMoves(sim, p);
        
        // list of legal simulated moves that result in a capture   
        List<SmartMove> killMoves = new ArrayList<>();

        //
        // is capturable
        //
        List<Move> legalSimEnemy = game.getLegalMoves(sim, game.getOpponent(p));
        
        for (Move lsem : legalSimEnemy){
            if (lsem.destination.equals(sm.destination)){
                capturable = sm.board.getTile(sm.origin).getOccupator().value;
                break;
            }
        }

        //
        // is recapturable
        //
        if (capturable > 0){
            for (Move lsem : legalSimEnemy){
                if (lsem.destination.equals(sm.destination)){
                    Board pieceCapturedBoard = sm.board.performMove(sm);
                    List<Move> legalMovesAfterPieceCaptured = game.getLegalMoves(pieceCapturedBoard, p);
                    for (Move m : legalMovesAfterPieceCaptured){
                        if (m.destination.equals(sm.destination)){
                            int enemyVal = sim.getTile(lsem.origin).getOccupator().value;
                            int pieceVal = sm.board.getTile(sm.origin).getOccupator().value;
                            capturable = ( enemyVal > pieceVal ) ? -1*(enemyVal-pieceVal) : capturable;
                        }
                    }
                    break;
                }
            }
        }

        if (capturable <= 0){
            //
            // -Calculate potential material value (total value of each attacked piece)
            // -Populate killmoves list
            //
            for (Move simMove : simLegal){
                if (simMove.origin.equals(sm.destination)){
                    SmartMove simSmart = new SmartMove(simMove);
                    if (simSmart.materialValue > 0){
                        killMoves.add(simSmart);
                        if (sim.getTile(simSmart.destination).getOccupator().ofType(Game.PieceType.KING))
                            checkMove = true;
                    }
                    
                }
            }

            for (SmartMove simsm : killMoves)
                potentialMaterial += simsm.materialValue;

            //
            // Calculate skewer value (difference in value between current attacked pieces and potential)
            //
            for (SmartMove killMove : killMoves){

                // remove the piece that would be captured from the board
                Board simBoardWithoutCapturePiece = new Board(sim);
                simBoardWithoutCapturePiece.getTile(killMove.destination).setOccupator(null);

                // recalculate killmoves
                List<SmartMove> altSimKillMoves = new ArrayList<>();
                List<Move> altSimLegalMoves = game.getLegalMoves(simBoardWithoutCapturePiece, p);
                for (Move simMove : altSimLegalMoves){
                    if (simMove.origin.equals(sm.destination)){
                        SmartMove simSmart = new SmartMove(simMove);
                        if (simSmart.materialValue > 0)
                            altSimKillMoves.add(simSmart);
                    }
                }

                // compare current killmoves to killmoves without the piece to be caputred
                if (altSimKillMoves.size() == killMoves.size()){
                    for (SmartMove askm : altSimKillMoves){
                        if (!killMoves.contains(askm)){
                            skewer = askm.materialValue;
                            break;
                        }
                    }
                }
            }

            //
            //  discovered value (difference between potential kill moves and current)
            // 
            //get current killMoves
            int currentKM = 0;
            int potentialKM = 0;
            for (Move clm : game.getLegalMoves(game.getCurrentBoard(), p)){
                SmartMove sm2 = new SmartMove(clm);
                if (sm2.materialValue > 0)
                    currentKM += sm2.materialValue;
            }

            for (Move sl : simLegal){
                SmartMove slsm = (new SmartMove(sl));
                if (slsm.materialValue > 0)
                    potentialKM += slsm.materialValue;
            }

            discover = potentialKM - currentKM;
        }



        // System.out.format("%s = p: %s | s: %s | d: %s | c: %s\n", sm, potentialMaterial, skewer, discover, capturable);

        int offensiveValue = (potentialMaterial + skewer + discover) + (checkMove ? 100 : 0);
        
        sm.tacticalValue = (capturable > 0 ? 0 : (capturable < 0 ? (-1*capturable) : offensiveValue));
    }

}

class SmartMove extends Move implements Comparable<SmartMove>{

        public double strategicValue;
        public double tacticalValue;

        public int materialValue;

        public SmartMove(Move move){
            super(move.origin, move.destination, move.board);

            this.materialValue = getMaterialValue();
        }

        private int getMaterialValue(){
            return (this.board.getTile(destination).getOccupator() != null ? this.board.getTile(destination).getOccupator().value : 0);
        }

        @Override 
        public int compareTo(SmartMove other){
            if (this.CHECKMATE) return 1;
            return (this.strategicValue > other.strategicValue) ? 1 : (this.strategicValue < other.strategicValue) ? -1 : 0;
        }
}