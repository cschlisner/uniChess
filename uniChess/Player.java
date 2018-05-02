package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/**
*   An object representing a Player in a chess game. Each Player has a color and an Identifier of type T. 
*   Two players of opposite color are required to initiate a Game.
*/
public class Player <T> {
    private T IDENTIFIER;
    private Game game;
    /** A general boolean switch for drawing.*/
    public boolean draw;

    /** The color of piece that the Player can move.*/
    public int color;

    public Player(T id, int color){
    	this.IDENTIFIER = id;
    	this.color = color;
    }

    public void registerGame(Game game){
        this.game = game;
    }

    public String getMove(){
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    /**
    *   Returns the identifier object associated with the player.
    *
    *   @return the identifier object associated with the player.
    **/
    public T getID(){
        return IDENTIFIER;
    }

    @Override
    public String toString(){
    	return String.valueOf(IDENTIFIER);
    }

    /** Compares the players based on the value of their identifier objects*/
    @Override
    public boolean equals(Object o){
        if (o instanceof Player){
            Player op = (Player)o;
            return op.getID().equals(IDENTIFIER);
        }
        return false;
    }
}
