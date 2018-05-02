//package uniChess;
//import java.io.PrintStream;
//import java.util.List;
//import java.util.ArrayList;
//import java.util.Collections;
//
//import java.lang.Thread;
//
//
///**
//*   An object representing a Simulated Player in a chess game.
//*/
//public class Chesster <T> extends Player <T> {
//    private Game game;
//
//    /** Determines amount of layers to calculate */
//    public int AI_DEPTH = 4;
//
//    /** Relative weight of material **/
//    public int MATERIAL_WEIGHT = 3;
//
//    public Chesster(T id, int c){
//        super(id, c);
//    }
//
//    public void registerGame(Game g){
//        this.game = g;
//    }
//
//    public Game getGame(){
//        return this.game;
//    }
//
//    long sysTime;
//    long avgThreadTime=0;
//    int threads;
//
//    /**
//    *   Returns the best possible legal move for the bot based on individual
//    *   tactics and strategy (logarithmic sum of average tactical value of future moves).
//    *
//    *   @return the best move
//    */
//    public String getMove(){
//        Move best = null;
//
//        List<Move> legal = game.getCurrentBoard().getLegalMoves(this);
//
//        List<SmartMove> smartMoves = new ArrayList<>();
//
//        for (Move move : legal)
//            smartMoves.add(new SmartMove(move));
//
////        sout.println("# Moves: "+smartMoves.size());
//        // sout.println("# Using: "+STRATEGY);
//
//        sysTime = System.currentTimeMillis();
//
//        List<StrategyProcessorThread> threadPool = new ArrayList<>();
//        for (SmartMove sm : smartMoves) {
//            threadPool.add(new StrategyProcessorThread(sm, this));
//            threadPool.get(threadPool.size()-1).setPriority(Thread.MAX_PRIORITY);
//        }
//
//        AsyncTask meme = new AsyncTask<Thread, Void, Void>(){
//            @Override
//            protected Void doInBackground(Thread... params) {
//                try {
//                    params[0].join();
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        };
//        for (int i = 0; i < smartMoves.size(); ++i){
//            threadPool.get(i).start();
//            // printProgress(i, smartMoves.size(), threadPool.get(i).sm.toString());
//            meme.execute(threadPool.get(i));
//        }
//
//        Collections.sort(smartMoves);
//        System.out.println("Starting minimax:");
//        // do minimax for last 3 moves
//        int searchDepth = 2;
//        double best_mm = -10000;
//        for (int i = smartMoves.size()-1; i > smartMoves.size()-4; --i){
//            Move m = smartMoves.get(i);
//            double v = minimax(m, searchDepth, Double.MIN_VALUE, Double.MAX_VALUE, this.color);
//            if (best == null || v > best_mm) {
//                best = m;
//                best_mm = v;
//            }
//        }
//
//        long processTime = (System.currentTimeMillis() - sysTime);
//
//        int treesize = 0;
//        for (StrategyProcessorThread t : threadPool){
//            treesize += t.treesize;
//            avgThreadTime += t.runTime;
//            ++threads;
//        }
//
//        sout.format("\n# Time : %sms | Avg Move Process Time: %sms\n", processTime, (avgThreadTime / threads));
//        sout.format("# Total Sub Move Tree Size: %s | Avg Sub Move Process Time: %sms\n\n", treesize, (processTime / treesize));
//
//        sout.println(((SmartMove)best).getDataSring());
//
//        return best.getANString();
//    }
//
//    private double minimax(Move move, int depth, double alpha, double beta, Game.Color color){
//        if (depth == 0){
//            return minmaxevaluate(move);
//        }
//
//        List<Move> responseMoves = move.getSimulation().getOpponentLegalMoves(color);
//
//        if (color.equals(this.color)){
//            double v = Double.MIN_VALUE;
//            for (Move opponentMove : responseMoves){
//                v = Math.max(minimax((opponentMove), depth-1, alpha, beta, Game.getOpposite(color)), v);
//                alpha = Math.max(alpha, v);
//                if (alpha >= beta) break;
//            }
//            return v;
//        }
//        else {
//            double v = Double.MAX_VALUE;
//            for (Move opponentMove : responseMoves){
//                v = Math.min(minimax((opponentMove), depth-1, alpha, beta, Game.getOpposite(color)), v);
//                beta = Math.min(beta, v);
//                if (alpha >= beta) break;
//            }
//            return v;
//        }
//    }
//
//    private double minmaxevaluate(Move move){
//        //return new Random().nextInt(10);/*
//        if (move.CHECKMATE) return Double.MAX_VALUE;
//        Board sim = move.getSimulation();
//        return (sim.getMaterialCount(this.color) / sim.getMaterialCount(Game.getOpposite(this.color)));//*/
//    }
//
//
//    public void printProgress(int prog, int total, String sm){
//        double percent = (double)prog/total;
//        double percentFrom20 = 20 * percent;
//        sout.print("\rThinking [");
//        for (int i = 0; i < 20; ++i)
//            sout.println((i <= (int)percentFrom20) ? "=" : " ");
//        sout.print("] "+sm);
//    }
//
//    private static PrintStream sout = System.out;
//    public void setPrintStream(PrintStream out){
//        sout = out;
//    }
//}
//
//
//
//class SmartMove extends Move implements Comparable<SmartMove>{
//        public static int MATERIAL_WEIGHT = 1;
//
//        public double[] unWeightedTreeAverages;
//
//
//        public double strategicValue = 0;
//        public double tacticalValue = 0.0;
//
//        public double locationValue;
//
//        public SmartMove(Move move){
//            super(move);
//
//            this.materialValue *= MATERIAL_WEIGHT;
//            this.tacticalValue = this.materialValue;
//            this.locationValue = (getDistanceToTarget(this.origin) - getDistanceToTarget(this.destination));
//
//        }
//
//        /** returns absolute distance from target location (opposite king for normal pieces, default king position for king pieces) */
//        public double getDistanceToTarget(Location location){
//            if (!this.piece.type.equals(Game.PieceType.KING))
//                return this.board.getDistanceFromKing(Game.getOpposite(this.piece.color), location);
//            else return this.board.getDistanceFromLocation(location, (this.piece.color.equals(Game.Color.WHITE) ? new Location(4,0) : new Location(4,7)));
//        }
//
//        public double calculateStrategicValue(){
//            double best=0, worst=0;
//            for (double val : unWeightedTreeAverages){
//                if ( val < worst) worst = val;
//                else if ( val > best) best = val;
//            }
//            return best+worst;
//        }
//
//        /**
//        *   Compares Moves based on strategic/tactical value, then on location value if strategic/tactical value is equivalent.
//        *
//        */
//        @Override
//        public int compareTo(SmartMove other){
//            if (this.CHECKMATE) return 1;
//
//             return (this.strategicValue > other.strategicValue) ? 1 :
//                    (this.strategicValue < other.strategicValue) ? -1 :
//                    (this.materialValue  > other.materialValue) ? 1 :
//                    (this.materialValue  < other.materialValue) ? -1 :
//                    (this.locationValue  > other.locationValue) ? 1 :
//                    (this.locationValue  < other.locationValue) ? -1 : 0;
//        }
//
//        public String getDataSring(){
//            String res = "";
//
//            res+=String.format("%s := %.2f :: ", this.toString(), strategicValue);
//            int depth = 0;
//            for (double d : unWeightedTreeAverages)
//                res += String.format("n[%s]: %.2f | ", (depth++), d);
//            return res;
//        }
//}