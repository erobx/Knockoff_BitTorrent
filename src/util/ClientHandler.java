package util;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import peers.Peer;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private OutputStream out;
    private int peerID;

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
                Peer.addToMessageQueue(msg, peerID);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Socket getSocket() {
        return clientSocket;
    }
}
