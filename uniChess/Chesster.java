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

    private int threadProcessingCount = -1;

    public Chesster(T id, Game.Color c){
        super(id, c);
    }

    public void setThreadProcessingCount(int c){
        this.threadProcessingCount = c;
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

        for (SmartMove sm : smartMoves)
            threadPool.add(new StrategyProcessorThread(sm, this));
        
        threadProcessingCount = (threadProcessingCount < 0 ? smartMoves.size() : threadProcessingCount);

        processLoop:
        for (int i = 0; i < smartMoves.size(); i += threadProcessingCount){
            for (int j = i; j < i + threadProcessingCount; ++j){
                if (j == threadPool.size()) break processLoop;
                threadPool.get(j).start();
                printProgress(j, smartMoves.size(), threadPool.get(j).sm.toString());
                try{
                    threadPool.get(j).join();
                } catch (Exception e) {}
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

            // for (int i = cutoff; i < smartMoves.size(); ++i)
            //     System.out.print(smartMoves.get(i)+": "+smartMoves.get(i).strategicValue+" | ");

            Random r = new Random(System.currentTimeMillis());
            best = smartMoves.get(cutoff+r.nextInt(smartMoves.size()-cutoff));
        }

        else best = smartMoves.get(smartMoves.size()-1);

        System.out.println("\n"+best);

        return best.getANString();
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