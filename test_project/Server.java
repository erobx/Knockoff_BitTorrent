package test_project;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import test_project.Handshake.Result;

public class Server extends Thread {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket listener = new ServerSocket(port);
            System.out.println("\nServer is running.\n");
            
            while (true) {
                Socket connection = listener.accept();
                new ClientHandler(connection).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private final Socket connection;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ClientHandler(Socket connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();

                Result result = receiveHandshake();
                if (result.valid) {
                    System.out.println("Peer " + result.peerID + ": handshake accepted.");
                    sendHandshake();
                }

                // Main logic here
                while (true) {
                    break;
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    connection.close();
                    System.out.println("Client disconnect. Goodbye!\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        void sendHandshake() {
            Handshake handshake = new Handshake(1001);
            try {
                out.writeObject(handshake);
                out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        Result receiveHandshake() {
            try {
                Handshake handshake = (Handshake)in.readObject();
                String header = handshake.getHeader();
                int peerID = handshake.getID();
                if (header.equals("P2PFILESHARINGPROJ")) {
                    Result result = handshake.new Result(true, peerID);
                    return result;
                }
            } catch (ClassNotFoundException classNot) {
                System.err.println("Data received in unknown format.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }
}
