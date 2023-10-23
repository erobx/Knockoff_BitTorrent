package util;

import java.io.*;
import java.net.*;

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
            String msg;
            while ((msg = in.readLine()) != null) {
                // add to msg queue
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
