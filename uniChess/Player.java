package uniChess;

import java.util.List;
import java.util.ArrayList;

public class Player <T> {
    private T IDENTIFIER;

    public boolean draw;

    public Game.Color color;

    public Player(T id, Game.Color c){
    	this.IDENTIFIER = id;
    	this.color = c;
    }

    public T getID(){
        return IDENTIFIER;
    }

    @Override
    public String toString(){
    	return String.valueOf(IDENTIFIER);
    }
}
