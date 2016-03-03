package uniChess;

public class GameException extends Exception {
	private String[] messages;

	public GameException(String... messageLines){
		super(messageLines[0]);
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
}