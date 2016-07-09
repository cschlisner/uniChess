package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import java.lang.Thread;


/**
*   An object representing a Simulated Player in a chess game. 
*/
public class Chesster <T> extends Player <T>{
    public enum StrategyType {LOG, LINEAR, EXP2, EXP4, EXP10}

    private Game game;

    /** Determines amount of layers to calculate */
    public int AI_DEPTH = 4;

    /** Determines relative weight of piece values */
    public int MATERIAL_WEIGHT = 3;

    public StrategyType STRATEGY = StrategyType.EXP4;
    public boolean dynamic=true;

    public Chesster(T id, Game.Color c){
        super(id, c);
    }

    public void registerGame(Game g){
        this.game = g;
    }

    public Game getGame(){
        return this.game;
    }

    long sysTime;
    long avgThreadTime=0;
    int threads;

    /**
    *   Returns the best possible legal move for the bot based on individual 
    *   tactics and strategy (logarithmic sum of average tactical value of future moves).
    *
    *   @return the best move
    */
    public Move getMove(){
        SmartMove.MATERIAL_WEIGHT = this.MATERIAL_WEIGHT;
        if (Board.playerHasCheck(game.getCurrentBoard(), game.getDormantPlayer()))
            STRATEGY = StrategyType.LOG;
        else STRATEGY = StrategyType.EXP4;
        Move best;

        List<Move> legal = game.getCurrentBoard().getLegalMoves(this);
        
        List<SmartMove> smartMoves = new ArrayList<>();
        
        for (Move move : legal)
            smartMoves.add(new SmartMove(move));

        System.out.println("# Moves: "+smartMoves.size());
        // System.out.println("# Using: "+STRATEGY);

        sysTime = System.currentTimeMillis();

        List<StrategyProcessorThread> threadPool = new ArrayList<>();

        for (SmartMove sm : smartMoves)
            threadPool.add(new StrategyProcessorThread(sm, this));
        

        for (int i = 0; i < smartMoves.size(); ++i){
            threadPool.get(i).start();
            printProgress(i, smartMoves.size(), threadPool.get(i).sm.toString());
            try{
                threadPool.get(i).join();
            } catch (Exception e) {}
        }

        Collections.sort(smartMoves);
        long processTime = (System.currentTimeMillis() - sysTime);
        
        int treesize = 0;
        for (StrategyProcessorThread t : threadPool){
            treesize += t.treesize;
            avgThreadTime += t.runTime;
            ++threads;
        }

        System.out.format("\n# Time : %sms | Avg Move Process Time: %sms\n", processTime, (avgThreadTime / threads));
        System.out.format("# Total Sub Move Tree Size: %s | Avg Sub Move Process Time: %sms\n\n", treesize, (processTime / treesize));


        // if (smartMoves.get(smartMoves.size()-1).strategicValue == smartMoves.get(smartMoves.size()-2).strategicValue)
        //     best = smartMoves.get(smartMoves.size()-((new Random()).nextInt(1)+1));
            
        best = smartMoves.get(smartMoves.size()-1);

        // for (SmartMove saasd : smartMoves)
        //     System.out.println(saasd.getDataSring());

        System.out.println(((SmartMove)best).getDataSring());

        return best;
    }


    public void printProgress(int prog, int total, String sm){
        double percent = (double)prog/total;
        double percentFrom20 = 20 * percent;
        System.out.print("\rThinking [");
        for (int i = 0; i < 20; ++i){
            if (i <= (int)percentFrom20)
                System.out.print("=");
            else System.out.print(" ");
        }
        System.out.print("] "+sm);
    }
}


class SmartMove extends Move implements Comparable<SmartMove>{
        public static int MATERIAL_WEIGHT = 1;
        
        public double[] unWeightedTreeAverages;


        public double strategicValue = 0;
        public double tacticalValue = 0.0;

        public double locationValue;

        public SmartMove(Move move){
            super(move);

            this.materialValue *= MATERIAL_WEIGHT;
            this.tacticalValue = this.materialValue;
            this.locationValue = (getDistanceToTarget(this.origin) - getDistanceToTarget(this.destination));

        }

        /** returns absolute distance from target location (opposite king for normal pieces, default king position for king pieces) */
        public double getDistanceToTarget(Location location){
            if (!this.movingPiece.type.equals(Game.PieceType.KING))
                return this.board.getDistanceFromKing(Game.getOpposite(this.movingPiece.color), location);
            else return this.board.getDistanceFromLocation(location, (this.movingPiece.color.equals(Game.Color.WHITE) ? new Location(4,0) : new Location(4,7)));
        }

        public double calculateStrategicValue(){
            double best=0, worst=0;
            for (double val : unWeightedTreeAverages){
                if ( val < worst) worst = val;
                else if ( val > best) best = val;
            }
            return best+worst;
        }

        public double calculateStrategicValue(Chesster.StrategyType stype){
            double sv = 0;
            //unWeightedTreeAverages[0] = this.tacticalValue;
            for (int i = 0; i < unWeightedTreeAverages.length; ++i){
                
                double weightedVal = 0;
                
                switch (stype){
                    case LOG:
                        weightedVal = unWeightedTreeAverages[i] * ( 1.0 / Math.log(i + Math.E) );
                        break;
                    
                    case LINEAR:
                        weightedVal = unWeightedTreeAverages[i] * ((-(1.0/(unWeightedTreeAverages.length+1)) * (double)i) ) + 1.0;
                        break;
                    
                    case EXP2:
                        weightedVal = unWeightedTreeAverages[i] * ( 1.0 / Math.pow(2.0, i) );
                        break;

                    case EXP4:
                        weightedVal = unWeightedTreeAverages[i] * ( 1.0 / Math.pow(4.0, i) );
                        break;

                    case EXP10:
                        weightedVal = unWeightedTreeAverages[i] * ( 1.0 / Math.pow(10.0, i) );
                        break;
                }

                sv += weightedVal;
            }
            return sv;
        }

        /**
        *   Compares Moves based on strategic/tactical value, then on location value if strategic/tactical value is equivalent.
        *
        */
        @Override 
        public int compareTo(SmartMove other){
            if (this.CHECKMATE) return 1;

             return (this.strategicValue > other.strategicValue) ? 1 :
                    (this.strategicValue < other.strategicValue) ? -1 :
                    (this.materialValue  > other.materialValue) ? 1 :
                    (this.materialValue  < other.materialValue) ? -1 :
                    (this.locationValue  > other.locationValue) ? 1 :
                    (this.locationValue  < other.locationValue) ? -1 : 0;
        }

        public String getDataSring(){
            String res = "";

            res+=String.format("%s := %.2f :: ", this.toString(), strategicValue);
            int depth = 0;
            for (double d : unWeightedTreeAverages)
                res += String.format("n[%s]: %.2f | ", (depth++), d);
            return res;
        }
}