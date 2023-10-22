package test_project;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket listener = new ServerSocket(port);
            System.out.println("Server is running.");
            
            while (true) {
                Socket connection = listener.accept();
                System.out.println("Connection established.");
                new ClientHandler(connection).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private final Socket connection;
        String message;
        String MESSAGE;
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
                
                try {
                    while (true) {
                        message = (String)in.readObject();
                        if (message.equals("end"))
                            break;
                        System.out.println("Received message: " + message + " from client.");
                        MESSAGE = message.toUpperCase();
                        sendMessage(MESSAGE);
                    }
                } catch (ClassNotFoundException classNot) {
                    System.err.println("Data received in unknown format.");
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    connection.close();
                    System.out.println("\nGoodbye!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void sendMessage(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to client.");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
