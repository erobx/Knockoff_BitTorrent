package util;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import peers.Peer;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
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
    public ClientHandler(Socket clientSocket, int peerID) {
        this.clientSocket = clientSocket;
        this.peerID = peerID;
        try {
            out = clientSocket.getOutputStream();
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            byte[] msg;
            while ((msg = in.readLine().getBytes()) != null) {
                // TODO This just isn't happening, most likely a input/output buffer problem
                thisPeer.addToMessageQueue(msg, peerID);
                System.out.println("Message added to queue");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Socket getSocket() {
        return clientSocket;
    }
}
