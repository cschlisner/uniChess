package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.lang.Thread;

public class StrategyProcessorThread extends Thread {
    
    Game game;
    Chesster chesster;
    int cpuSave=0;

    public SmartMove sm;


    private int AI_DEPTH = 1;
    private int AI_COMPLEXITY = 4;
    private int OPPONENT_COMPLEXITY = 1;
    
    public StrategyProcessorThread(SmartMove sm, Chesster chesster){
        super(sm.getANString());
        this.sm = sm;
        this.chesster = chesster;
        this.game = chesster.getGame();
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
    public void run(){
        // System.out.println("start "+this.getName());

        AI_DEPTH = 1; 
        AI_COMPLEXITY = 4;
        OPPONENT_COMPLEXITY = 4; 

        processTacticalValue(sm, chesster, AI_COMPLEXITY);

        List<List<Move>> submoveTree = getMoveTree(sm, AI_DEPTH, 0);
        
        double[] avgTactical = new double[AI_DEPTH+1];

        avgTactical[0] = sm.tacticalValue;

        int listLength = 1, listIndex = 0;


        for (int i = 1; i < AI_DEPTH+1; ++i){
            int avg = 0;
            int combinedListLengths = 0;

            for (int j = listIndex; j < listIndex+listLength; ++j){
                if (j >= submoveTree.size()) break;
                for (Move move : submoveTree.get(j)){
                    SmartMove subMoveSM = new SmartMove(move);
                    //System.out.print("::Processing "+subMoveSM);
                    processTacticalValue(subMoveSM, chesster, AI_COMPLEXITY);
                    // System.out.println(": "+subMoveSM.tacticalValue);
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
        for (int i = 0; i < AI_DEPTH+1; ++i){
            double avgWeightedTreeVal = (double)(1 / (i+1))*(avgTactical[i]);
            sum += avgWeightedTreeVal;
        }
        

        // set the strategic value to the sum of average weighted tacticals of each tree depth
        sm.strategicValue = sum;
        // System.out.println("end "+this.getName());
    }

    public void printMoveTree(List<List<Move>> tree){
        int listLength = 1, listIndex = 0;
        // sysTime = System.currentTimeMillis();
        for (int i = 1; i < AI_DEPTH+1; ++i){
            
            int combinedListLengths = 0;
            
            for (int j = listIndex; j < listIndex+listLength; ++j){
                if (j == tree.size()) break;
                for (int k = 0 ; k < i; ++k)
                    System.out.print(":");

                for (Move move : tree.get(j))
                    System.out.println(move.getANString());
                
                combinedListLengths+=tree.get(j).size();
            }

            if (combinedListLengths == 0)
                break;

            listIndex += listLength;
            listLength = combinedListLengths;
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

        List<Move> opponentLegal = sim.getOpponentLegalMoves(chesster);

        // CHECKMATE BITCH
        if (opponentLegal.isEmpty()){
            m.CHECKMATE = true;
            return new ArrayList<Move>();
        }


        // perform the best possible move in response as opponent
        Board sim2 = sim.performMove(getBestTacticalOpponentMove(opponentLegal));

        List<Move> res = new ArrayList<>();
        for (Move legal : sim2.getLegalMoves(chesster))
           // if (legal.origin.equals(m.destination))
                res.add(legal);

        return res;
    }

    private Player getOpponent(){
        return game.getPlayer(Game.getOpposite(chesster.color));
    }

    private Move getBestTacticalOpponentMove(List<Move> moveList){
        SmartMove best = new SmartMove(moveList.get(0));
        
        for (Move m : moveList){
            SmartMove sm = new SmartMove(m);
            processTacticalValue(sm, getOpponent(), OPPONENT_COMPLEXITY);
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
    *   The above variables ordered in terms of importance is the following: 
    *   1. potential material
    *   2. discover
    *   3. skewer
    *   4. capturable / recapturable
    *   
    *   The amount of variables calculated is determined by the complexity variable. 
    *   The complexity variable is an integer ranging 1-4, specifying the amount of variables to calculate
    *   starting from the most important and ending at the least. For example, a complexity of 2 would calculate
    *   discover and potential material, while a complexity of 3 would calculate discover, potential material, and skewer.
    *   
    *   @param SmartMove to assign strategic value to
    */
    private void processTacticalValue(SmartMove sm, Player p, int complexity){
        // flags for calculating result
        int potentialMaterial = 0, skewer = 0, discover = 0, capturable = 0;

        // simulation of the move (the resulting board if the move were performed)
        Board sim = sm.board.performMove(sm);

        // legal moves in the simulated board
        List<Move> simLegal = sim.getLegalMoves(p);
        
        // list of legal simulated moves that result in a capture   
        List<SmartMove> killMoves = new ArrayList<>();

        if (complexity >= 4)
            capturable = calculateCapturable(sim, sm, p);

        if (complexity < 4 ^ capturable <= 0){
            if (complexity >= 3)
                skewer = calculateSkewer(sim, sm, p);
            
            if (complexity >= 2)
                discover = calculateDiscover(sim, sm, p);
            
            if (complexity >= 1)
                potentialMaterial = calculatePotentialMaterial(sim, sm, p);
        }

        //System.out.format("%s = p: %s | s: %s | d: %s | c: %s\n", sm, potentialMaterial, skewer, discover, capturable);

        int offensiveValue = (potentialMaterial + skewer + discover + sm.materialValue);
        
        int defensiveWeighted = (capturable > 0 ? (-1*sm.movingPiece.value) : (capturable < 0 ? (-1*capturable) : offensiveValue));
        
        sm.tacticalValue = ((complexity >= 4) ? defensiveWeighted : offensiveValue);
    }

    private int calculateCapturable(Board sim, SmartMove sm, Player p){
        int capturable = 0;
        //
        // is capturable
        //
        List<Move> legalSimEnemy = sim.getOpponentLegalMoves(p);
        
        for (Move lsem : legalSimEnemy){
            if (lsem.destination.equals(sm.destination)){
                capturable = sm.movingPiece.value;
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
                    List<Move> legalMovesAfterPieceCaptured = pieceCapturedBoard.getLegalMoves(p);
                    for (Move m : legalMovesAfterPieceCaptured){
                        if (m.destination.equals(sm.destination)){
                            int enemyVal = lsem.movingPiece.value;
                            int pieceVal = sm.movingPiece.value;
                            capturable = ( enemyVal > pieceVal ) ? -1*(enemyVal-pieceVal) : capturable;
                        }
                    }
                    break;
                }
            }
        }
        
        return capturable;
    }

    private int calculatePotentialMaterial(Board sim, SmartMove sm, Player p){
        List<Move> simLegal = sim.getLegalMoves(p);

        int potentialMaterial = 0;

        for (Move simMove : simLegal){
            if (simMove.origin.equals(sm.destination)){
                SmartMove simSmart = new SmartMove(simMove);
                if (simSmart.materialValue > 0){
                    potentialMaterial += simSmart.materialValue;
                    if (sim.getTile(simSmart.destination).getOccupator().ofType(Game.PieceType.KING))
                        potentialMaterial += 200; // check move
                    else if (sim.isValidMoveForKing(Game.getOpposite(p.color), simSmart.destination))
                        potentialMaterial += 100; // will block a king move, hopefully resulting in checkmate
                }
                
            }
        }

        return potentialMaterial;
    }

    private int calculateSkewer(Board sim, SmartMove sm, Player p){
        
        int skewer = 0;

        List<SmartMove> killMoves = new ArrayList<>();

        for (Move legalMove : sim.getLegalMoves(p)){
            SmartMove km = new SmartMove(legalMove);
            if (km.materialValue > 0)
                killMoves.add(km);
        }

        for (SmartMove killMove : killMoves){
            // remove the piece that would be captured from the board
            Board simBoardWithoutCapturePiece = new Board(sim);
            simBoardWithoutCapturePiece.getTile(killMove.destination).setOccupator(null);

            // recalculate killmoves
            List<SmartMove> altSimKillMoves = new ArrayList<>();
            List<Move> altSimLegalMoves = simBoardWithoutCapturePiece.getLegalMoves(p);
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

        return skewer;
    }

    private int calculateDiscover(Board sim, SmartMove sm, Player p){
        int discover = 0;
        //
        //  discovered value (difference between potential kill moves and current)
        // 
        //get current killMoves
        int currentKM = 0;
        int potentialKM = 0;
        for (Move clm : game.getCurrentBoard().getLegalMoves(p)){
            SmartMove sm2 = new SmartMove(clm);
            if (sm2.materialValue > 0)
                currentKM += sm2.materialValue;
        }

        for (Move sl : sim.getLegalMoves(p)){
            SmartMove slsm = (new SmartMove(sl));
            if (slsm.materialValue > 0)
                potentialKM += slsm.materialValue;
        }

        return potentialKM - currentKM;
    }
}