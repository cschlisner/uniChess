import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import uniChess.*;

import java.util.*;
import java.io.*;

class UDPChess {

	static int server_port = 9876;
	static int send_port = 9876;
	static InetAddress clientIP;


	public static void main(String[] args) {
		
		if (args.length >= 1 && args[0] != null)
			server_port = Integer.valueOf(args[0]);
		if (args.length >= 2 && args[1] != null)
			send_port = Integer.valueOf(args[1]);

		System.out.format("Receiving on: %s\nSending to: %s\n", server_port, send_port);

		try {
			clientIP = InetAddress.getByName("10.208.114.93");
		} catch(Exception e){};

		Scanner in = new Scanner(System.in);

		Player<String> p1 = new Player<>("Jake", Game.Color.WHITE);
		Chesster<String> p2 = new Chesster<>("Chesster", Game.Color.BLACK);


		Game chessGame = new Game(p1, p2);
		
		chessGame.getCurrentBoard().print(p1,p2);

		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(server_port);
			//serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), Integer.valueOf(args[0])));
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		byte[] receiveData = new byte[255];
		byte[] sendData = new byte[4];

		while (true){
				Game.GameEvent gameResponse;
				
				if (chessGame.getCurrentPlayer().equals(p1)){
					String netMove = null;
					System.out.println("Waiting for move from network...");
					while (netMove == null || netMove.isEmpty()){
						try {
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							serverSocket.receive(receivePacket);
							
							netMove = new String(receivePacket.getData(), 0, receivePacket.getLength());
							netMove.replace("x", "");
							clientIP = receivePacket.getAddress();
						} catch(Exception e){
							System.out.println(e.getMessage());
						}
					}
					System.out.println(clientIP+" > "+netMove);
					gameResponse = chessGame.advance(netMove);
				}
				else {
					Move input = p2.getMove();
					System.out.println("Sending move to "+clientIP+"...");
					gameResponse = chessGame.advance(input.getANString());
					String sym = input.movingPiece.getSymbol(false).toUpperCase();
					send((sym.equals("P") ? "" : sym)+input.destination.toString());
				}
				
				chessGame.getCurrentBoard().print(p1,p2);
				
				switch(gameResponse){

					case OK:
						break;
					case AMBIGUOUS:
						System.out.println("Ambiguous Move.");
						break;
					case INVALID:
						System.out.println("Invalid Move.");
						break; 
					case ILLEGAL:
						System.out.println("Illegal Move.");          
						break;
					case CHECK:
						System.out.println("You are in check!");
						break;
					case CHECKMATE:
						System.out.println("Checkmate. "+chessGame.getDormantPlayer().getID()+" wins!");
						System.out.println(chessGame.getGameString());
						System.exit(0);
						break;
					case STALEMATE:
						System.out.println("Stalemate. "+chessGame.getDormantPlayer().getID()+" wins!");
						break;
					case DRAW:
						System.out.println("Draw!");
						break;

				}
		}
	}

	private static void send(String msg){
		try {
        DatagramSocket soc = new DatagramSocket();
        soc.send(new DatagramPacket(msg.getBytes(), msg.length(), clientIP, send_port));
        soc.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
	}

}