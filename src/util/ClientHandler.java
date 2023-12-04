package util;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import peers.Peer;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private OutputStream out;
    private int peerID;
    private Peer thisPeer;

    public ClientHandler(Socket clientSocket, int peerID, Peer thisPeer) {
        this.clientSocket = clientSocket;
        this.peerID = peerID;
        this.thisPeer = thisPeer;
        try {
            out = clientSocket.getOutputStream();
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // COULD BE REMOVED I DONT KNOW THOUGH, currently used by Server.java
    // public ClientHandler(Socket clientSocket, int peerID) {
    // this.clientSocket = clientSocket;
    // this.peerID = peerID;
    // try {
    // out = clientSocket.getOutputStream();
    // out.flush();
    // } catch (IOException ex) {
    // throw new RuntimeException(ex);
    // }
    // }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String msg = "";
            while ((msg = in.readLine()) != null) {
                // System.out.println("Message length: " + msg.getBytes().length);
                thisPeer.addToMessageQueue(msg.getBytes(), peerID);
                // System.out.println("Message added to queue");
            }
        } catch (Exception ex) {
            Peer.errorLogging(ex, peerID);
        }
    }

    public Peer getPeer() {
        return thisPeer;
    }

    public Socket getSocket() {
        return clientSocket;
    }
}
