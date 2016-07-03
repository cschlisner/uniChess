import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import uniChess.*;

import java.util.*;
import java.io.*;

class UDPChess {
  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);

    Player<String> p1 = new Player<>("Human", Game.Color.WHITE);
    Chesster<String> p2 = new Chesster<>("Chesster", Game.Color.BLACK);


    Game chessGame = new Game(p1, p2);
    System.out.print(chessGame.getCurrentBoard());
    
    DatagramSocket serverSocket = null;
    try {
      serverSocket = new DatagramSocket(Integer.valueOf(args[0]));
      //serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), Integer.valueOf(args[0])));
    } catch (Exception e){
      System.out.println(e.getMessage());
    }
    byte[] receiveData = new byte[255];
    byte[] sendData = new byte[4];

    while (true){
        Game.GameEvent gameResponse;
        
        if (chessGame.getCurrentPlayer().equals(p2)){
          String netMove = null;
          InetAddress clientIP = null;
          System.out.println("Waiting for move from network...");
          while (netMove == null || netMove.isEmpty()){
            try {
              DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
              serverSocket.receive(receivePacket);
              
              netMove = new String(receivePacket.getData(), 0, receivePacket.getLength());
              clientIP = receivePacket.getAddress();
            } catch(Exception e){
              System.out.println(e.getMessage());
            }
          }
          System.out.println(clientIP+" > "+netMove);
          gameResponse = chessGame.advance(netMove);
        }
        else gameResponse = chessGame.advance(in.nextLine());

        System.out.print(chessGame.getCurrentBoard());
        
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

}