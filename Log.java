package uniChess;

import org.json.*;
import java.util.List;
import java.util.ArrayList;

// Image libs
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.io.*;
import javax.imageio.ImageIO;

import java.lang.Runtime;


public class Log {
	private Game game;
	private Board board;

	private String buffer = null;
	private List<String> bufferHistory = null;
	private int bufferRead;

	private List<String> moveHistory = new ArrayList<String>();


	public Log(Game g){
		game = g;
		board = g.getBoard();

		bufferHistory = new ArrayList<String>();
		bufferRead = 0;

		Runtime run = Runtime.getRuntime();
		run.addShutdownHook(new Thread(){
			public void run(){
				saveGame();
			}
		});
	}

	public <T>void writeBuffer(T str){
		startBuffer();
		bufferAppendln(str);
		terminateBuffer();
	}

	public void startBuffer(){
		buffer = "";
	}

	public <T> void bufferAppendArray(T[] arr){
		for (T el : arr)
			bufferAppend(String.valueOf(el+" "));
		bufferAppendln("");
	}

	public <T>void bufferAppend(T str){
		buffer += String.valueOf(str);
	}

	public <T>void bufferAppendln(T str){
		buffer += String.valueOf(str)+"\n";
	}

	public void terminateBuffer(){
		bufferHistory.add(buffer);
		buffer = null;
	}

	// get the elements not read yet
	public String getUnreadBuffer(){
		String buf = "";
		for (;bufferRead < bufferHistory.size(); ++bufferRead)
			buf+=bufferHistory.get(bufferRead);
		return buf;
	}

	public String getLastBuffer(){
		return bufferHistory.get(bufferHistory.size()-1);
	}

	public void removeLastBuffer(){
		bufferHistory.remove(bufferHistory.size()-1);
	}

	public String getBufferHistory(){
		String result = "";
		for (String s : bufferHistory)
			result += s;
		return result;
	}

	public void appendMoveHistory(String moveText){
		moveHistory.add(moveText);
	}

	public List<String> getMoveHistory(){
		return moveHistory;
	}

	public boolean saveGame(){
		try {
			JSONObject gameSave = new JSONObject();

			gameSave.put("player1", game.player1.getName());
			gameSave.put("player2", game.player2.getName());
			gameSave.put("imageOutput", game.getImageOutput());
			gameSave.put("id", game.getId());
			gameSave.put("moves", moveHistory);

			PrintWriter writer = new PrintWriter(game.getId()+".chess", "UTF-8");
			gameSave.write(writer);
			writer.close();
			return true;
		} catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public JSONObject importGame(String filename){
		JSONObject gameData;

		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }

		    return new JSONObject(sb.toString());

		} catch (Exception e){
			System.out.println("Error importing data");
			e.printStackTrace();
		}
		return null;
	}

	public void logBoard(boolean imageOut){
		boolean reversed = game.isCurrentPlayer(game.player2);

		startBuffer();

		int max = findMaxLen(board.getBoardSpace());
		int y = 8;
		writeColumnLabels(max, reversed);
		if (!reversed){
			for (Board.Tile[] row : board.getBoardSpace()){
				bufferAppend(y);
				for (Board.Tile el : row){
					bufferAppend(el);
					for (int k=0;k<((max-String.valueOf(el).length()));++k)	
						bufferAppend(" ");
				}
				bufferAppendln(y--);
			}
		} else {
			for (int i = board.getBoardSpace().length-1; i >= 0; --i){
				bufferAppend(y-i);
				for (int j = board.getBoardSpace()[0].length-1; j >= 0; --j){
					bufferAppend(board.getBoardSpace()[i][j]);
					for (int k=0;k<((max-String.valueOf(board.getBoardSpace()[i][j]).length()));++k)	
						bufferAppend(" ");
				}
				bufferAppendln(y-i);
			}
		}
		writeColumnLabels(max, reversed);
		terminateBuffer();

		if (imageOut)
			createBoardImage();
	}

	private void createBoardImage(){
		String outputFileName = game.defaultFileOut;
	    String[] boardTextLines = getLastBuffer().split("\n");
	    removeLastBuffer();

		Font boardFont = new Font("DejaVu Sans Mono", Font.PLAIN, 40);
		FontMetrics metrics = new BufferedImage(1,1, BufferedImage.TYPE_INT_RGB).getGraphics().getFontMetrics(boardFont);
		int fontHeight = metrics.getHeight();
		int fontAdvFull = metrics.stringWidth(boardTextLines[1]);
		int fontAdvOne = metrics.stringWidth(" ");
		int fontAdvBoard = metrics.stringWidth(boardTextLines[0]);

		BufferedImage bufferedImage = new BufferedImage(fontAdvFull+4, (fontHeight*11), BufferedImage.TYPE_INT_RGB);

	    Graphics2D g = bufferedImage.createGraphics();

		// Background
		g.setColor(new Color(238,238,238));
		g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

	    // Text color
	    g.setColor(new Color(34, 49, 63));
	    g.setFont(boardFont);
	    int i = 0;
	    for (String line : boardTextLines){
	    	g.drawString(line, 2, (++i*fontHeight));
	    }

	    // Turn Colors light | dark
	    Color light = new Color(189,195,199);
	    Color dark = new Color(34, 49, 63);

	    g.setColor((game.isCurrentPlayer(game.player2)?dark:light));
		g.fillRect(0, (i*fontHeight)+10, bufferedImage.getWidth(), bufferedImage.getHeight());
		
		g.setColor((game.isCurrentPlayer(game.player2)?light:dark));
		boardFont = new Font("DejaVu Sans Mono", Font.PLAIN, 22);
		g.setFont(boardFont);
		
		String nombre = game.getCurrentPlayer().getName();
		int halfNombre = (nombre.length()/2);
		g.drawString(nombre, (bufferedImage.getWidth()/2)-(g.getFontMetrics(boardFont).stringWidth(nombre.substring(halfNombre))), (i*fontHeight)+35);
	    g.dispose();
	    
	    try{
            File outputfile = new File(outputFileName);
            ImageIO.write(bufferedImage, "png", outputfile);
        } catch(Exception e){
            e.printStackTrace();
        }
	}

	private void writeColumnLabels(int max, boolean reversed){
		for (int x = 0; x<9; ++x){
			if (x>0) bufferAppend(" ABCDEFGH".charAt((reversed)?9-x:x));
			for (int k=0;k<(max-1);++k)	
				bufferAppend(" ");
		}
		bufferAppendln("");
	}
	private static <T> int findMaxLen(T[][] arr){
		int max=0;
		for (T[] row : arr)
			for (T el : row)
		 max = (String.valueOf(el).length() > max)?String.valueOf(el).length():max;
		return max;
    }
}