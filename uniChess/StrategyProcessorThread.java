package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.lang.Thread;

public class StrategyProcessorThread extends Thread {
    
    Game game;
    Chesster chesster;
    int cpuSave=0;
    public long runTime;
    public int treesize;

    public SmartMove sm;

    private int AI_DEPTH;
    
    public StrategyProcessorThread(SmartMove sm, Chesster chesster){
        super(sm.getANString());
        this.sm = sm;
        this.chesster = chesster;
        this.game = chesster.getGame();

        this.AI_DEPTH = chesster.AI_DEPTH;
    }


    public void run(){
        runTime = System.currentTimeMillis();

        sm.unWeightedTreeAverages = new double[AI_DEPTH];

        getMoveTreeVal(sm, AI_DEPTH, 0, sm.unWeightedTreeAverages);
        
        sm.strategicValue = sm.calculateStrategicValue();                
        runTime = System.currentTimeMillis() - runTime;
    }

    /**
    *   Returns all legal submoves of a move up to depth of max; populates given array with weighted average tactical val for each node depth.
    *   Weight applied is (1 / nodedepth) to account for loss in prediction accuracy over time. 
    *
    *   @return The tree of moves
    */
        double worstMove=100000.0, bestMove=-1000000.0;
    public void getMoveTreeVal(SmartMove m, int max, int depth, double[] vals){
        
        if (depth < max){

            List<SmartMove> submoves = getSubMoves(m);

            List<Double> tactVals = new ArrayList<>();

            for (SmartMove sub : submoves){
                if (sub.tacticalValue < worstMove) worstMove = sub.tacticalValue;
                else if (sub.tacticalValue > bestMove) bestMove = sub.tacticalValue;

                // Only explore tree if the tactical value of this move has not been found on this depth
                if (!tactVals.contains(sub.tacticalValue)) {
                    tactVals.add(sub.tacticalValue);
                    getMoveTreeVal(sub, max, depth+1, vals);
                }
            }
            treesize += submoves.size();
            vals[depth] = worstMove + bestMove;
        }   
    }

    /**
    *   Returns all legal submoves available if a given move by a given player were to be performed
    *   and the opponent were to perform their highest tactically rated move.
    *
    *   @return The list of possible submoves
    */
    public List<SmartMove> getSubMoves(SmartMove m){
        boolean debug = (sm.equals(m));

        double curCaptureVal = 0;
        for (Move olm : m.board.getOpponentLegalMoves(chesster)){
            SmartMove olsm = new SmartMove(olm);
            if ( olsm.materialValue > curCaptureVal )
                curCaptureVal = olsm.materialValue;
        }

        List<Move> potentialOpponentLegal = m.getSimulation().getOpponentLegalMoves(chesster);
        
        // CHECKMATE !
        if (potentialOpponentLegal.isEmpty()){
            m.CHECKMATE = true;
            return new ArrayList<SmartMove>();
        }
        
        double potCaptureVal = 0;
        List<SmartMove> opponentSorted = new ArrayList<>();
        for (Move olm : potentialOpponentLegal){
            SmartMove olsm = new SmartMove(olm);
            opponentSorted.add(olsm);
            if (olm.destination.equals(m.destination))
                m.tacticalValue = -1.0*m.movingPiece.value;
            if (olm.materialValue > potCaptureVal)
                potCaptureVal = olm.materialValue;
        }
        m.tacticalValue = chesster.MATERIAL_WEIGHT*(potCaptureVal - curCaptureVal);


        Collections.sort(opponentSorted);
        
        SmartMove opponentBest = opponentSorted.get(opponentSorted.size()-1);

        // perform the best possible move in response as opponent
        List<SmartMove> res = new ArrayList<>();
        for (Move legal : opponentBest.getSimulation().getLegalMoves(chesster))
            res.add(new SmartMove(legal));
        
        return res;
    }

    private Player getOpponent(){
        return game.getPlayer(Game.getOpposite(chesster.color));
    }
}