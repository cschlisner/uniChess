package uniChess;

public class GameException extends Exception {

	public static final int INVALID_MOVE = 0;
	public static final int AMBIGUOUS_MOVE = 1;

	private String[] messages;
	private int type;

	public GameException(int t, String... messageLines){
		super(messageLines[0]);
		this.type = t;
		messages = messageLines;
	}

	public String[] getMessages(){
		return messages;
	}

	public int getType(){
		return type;
	}
}