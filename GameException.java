package uniChess;

public class GameException extends Exception {

	public static final int INPUT = 0;
	public static final int FORFEIT = 1;
	public static final int CHECKMATE = 2;
	public static final int STALEMATE = 3;

	private String[] messages;
	private int type;

	public GameException(int t, String... messageLines){
		super(messageLines[0]);
		this.type = t;
		messages = messageLines;
	}

	public void writeMessagesToLog(){
		Game.gameLog.startBuffer();
		for (String ln : messages)
			Game.gameLog.bufferAppendln(ln);
		Game.gameLog.terminateBuffer();
	}

	public String[] getMessages(){
		return messages;
	}

	public int getType(){
		return type;
	}
}