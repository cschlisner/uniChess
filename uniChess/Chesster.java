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
    private Game game;

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
    double processTimeSum = 0.0;
    double processes = 0.0;
    /**
    *   Returns the best possible legal move for the bot based on individual 
    *   tactics and strategy (logarithmic sum of average tactical value of future moves).
    *
    *   @return the best move
    */
    public String getMove(){

        Move best;


        List<Move> legal = game.getCurrentBoard().getLegalMoves(this);
        
        List<SmartMove> smartMoves = new ArrayList<>();
        
        for (Move move : legal)
            smartMoves.add(new SmartMove(move));

        System.out.println("# Moves: "+smartMoves.size());
        sysTime = System.currentTimeMillis();


        List<StrategyProcessorThread> threadPool = new ArrayList<>();

        for (SmartMove sm : smartMoves){
            threadPool.add(new StrategyProcessorThread(sm, this, (smartMoves.size() <= 25 ? 0 : 1)));
            threadPool.get(threadPool.size()-1).start();
        }
        
        // for (Thread t : threadPool){
        //     try {
        //         t.join();
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        // }

        int threadsComplete = 0;
        while (!threadPool.isEmpty()){
            for (int i = 0; i < threadPool.size(); ++i){
                if (threadPool.get(i).getState().equals(Thread.State.TERMINATED)){
                    ++threadsComplete;
                    printProgress(threadsComplete, smartMoves.size());
                    threadPool.remove(i);
                }
            }
        }

        Collections.sort(smartMoves);

        double processTime = (double)((System.currentTimeMillis() - sysTime)/1000);
        
        processTimeSum += Math.round(processTime * 100.0) / 100.0;
        ++processes;
        double avgProcTime = Math.round((processTimeSum / processes) * 100.0) / 100.0; 
        System.out.format("\n# Time : %ss | avg: %ss\n", processTime, avgProcTime);

        if (smartMoves.get(smartMoves.size()-1).strategicValue > 0){
            int cutoff=0;
            for(int i = smartMoves.size()-2; i > 0; --i){
                if (smartMoves.get(i).strategicValue < smartMoves.get(i+1).strategicValue){
                    cutoff = i+1;
                    break;
                }
            }

            for (int i = cutoff; i < smartMoves.size(); ++i)
                System.out.print(smartMoves.get(i)+": "+smartMoves.get(i).strategicValue+" | ");

            Random r = new Random();
            best = smartMoves.get(cutoff+r.nextInt(smartMoves.size()-cutoff));
        }

        else best = smartMoves.get(smartMoves.size()-1);

        System.out.println("\n"+best);

        return best.getANString();
    }


    public void printProgress(int prog, int total){
        double percent = (double)prog/total;
        double percentFrom20 = 20 * percent;
        System.out.print("\rThinking [");
        for (int i = 0; i < 20; ++i){
            if (i <= (int)percentFrom20)
                System.out.print("=");
            else System.out.print(" ");
        }
        System.out.print("]");
    }
}