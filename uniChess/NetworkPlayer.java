package uniChess;

import java.net.*;
import java.nio.ByteBuffer;

import uniChess.*;
import java.io.*;

import java.util.*;
import java.io.*;

/**
 * Created by cschl_000 on 4/20/2017.
 */

public class NetworkPlayer<T> extends Player<T>  {
    DatagramSocket serverSocket = null;

    static int server_port = 9876;
    static int send_port = 9876;
    static InetAddress clientIP;
    byte[] receiveData = new byte[255];
    byte[] sendData = new byte[4];

    /**
     * UDP now, eventually network play
     * @param id identifier (
     * @param c
     */
    public NetworkPlayer(T id, Game.Color c){
        super(id, c);

        System.out.format("%s :: Receiving on: %s\nSending to: %s\n", id, server_port, send_port);

        try {
            serverSocket = new DatagramSocket(server_port);
            //serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), Integer.valueOf(args[0])));
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void registerOpponentIP(String IP) throws Exception{
        clientIP = InetAddress.getByName(IP);
    }

    public void setServer_port(int port) {
        server_port = port;
    }

    public void setSend_port(int port) {
        send_port = port;
    }

    public String getMoveAN(){
        String netMove = null;
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
        return netMove;
    }

    public void sendMoveAN(String AN){
        try {
            DatagramSocket soc = new DatagramSocket();
            soc.send(new DatagramPacket(AN.getBytes(), AN.length(), clientIP, send_port));
            soc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
