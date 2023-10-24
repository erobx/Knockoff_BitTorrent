package util;

import java.io.*;
import java.net.*;
import messages.Message;
import messages.Handshake;

public class Server extends Thread {
    private final int port;
    private ServerSocket listener;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            listener = new ServerSocket(port);
            System.out.println("[Server] Waiting for connection on port " + port);
            while (true) {
                Socket clientSocket = listener.accept();

                // Wait for handshake
                int senderID = -1;
                try {
                    //BufferedReader in = new BufferedReader(new InputStreamReader(
                    //    clientSocket.getInputStream()));
                    senderID = Handshake.serverHandshake(clientSocket.getInputStream(), clientSocket.getOutputStream());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                // Create new ClientHandler
                ClientHandler ch = new ClientHandler(clientSocket, senderID);
                // Add ch to list of clientsockets?

                ch.setDaemon(true);
                ch.start();
                
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                listener.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
