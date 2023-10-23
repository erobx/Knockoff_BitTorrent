package test_project;

import java.io.*;
import java.net.*;

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
            System.out.println("\nServer is running.");
            
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

        private InputStream in;
        private OutputStream out;

        public ClientHandler(Socket connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                in = connection.getInputStream();
                out = connection.getOutputStream();
                out.flush();

                handshake();

                for (int i = 0; i < 8; i++) {
                    getMessage();
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

        void handshake() {
            Result result = receiveHandshake();
            if (result.valid) {
                System.out.println("Peer " + result.peerID + ": handshake accepted.");
                sendHandshake(result.peerID);
            }
        }

        void sendHandshake(int pid) {
            try {
                Handshake msg = new Handshake("P2PFILESHARINGPROJ", pid);
                msg.serialize(out);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    
        Result receiveHandshake() {
            try {
                Handshake msg = Handshake.deserialize(in);
                String header = msg.getHeader();
                int peerID = msg.getID();
                
                if (header.equals("P2PFILESHARINGPROJ")) {
                    Result result = msg.new Result(true, peerID);
                    return result;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        void getMessage() {
            try {
                Message msg = Message.deserialize(in);
                int type = msg.getType();
                int index;

                switch (type) {
                    // Choke - no payload
                    case 0:
                        System.out.println("Choke me daddy.");
                        break;
                    // Unchoke - no payload
                    case 1:
                        System.out.println("Unchoke me daddy uWu.");
                        break;
                    // Interested - no payload
                    case 2:
                        System.out.println("Omg you're so tallllll!");
                        break;
                    // Not interested - no payload
                    case 3:
                        System.out.println("Sorry I have a boyfriend.");
                        break;
                    // Have - index payload
                    case 4:
                        index = msg.getIndex();
                        System.out.println("I have piece at index: " + index);
                        break;
                    // Bitfield - bitfield payload
                    case 5:
                        System.out.println("Send me your bitfield bitch.");
                        break;
                    // Request - index payload
                    case 6:
                        index = msg.getIndex();
                        System.out.println("Requested index: " + index);
                        // get piece
                        break;
                    // Piece - index + piece payload
                    case 7:
                        System.out.println("Give me a piece of dat ass.");
                        break;
                    default:
                        break;
                }
            } catch (IOException ex) {
                System.out.println("Failed to deserialize.");
                ex.printStackTrace();
            }
        }
    }
}
