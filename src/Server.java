import java.net.*;
import java.io.*;

public class Server {
    private static final int port = 8001;
    
    public static void main(String[] args) throws Exception {
        System.out.println("The server is running on port: " + port);
        ServerSocket listener = new ServerSocket(port);
        int clientNum = 1;
        try {
            while (true) {
                new Handler(listener.accept(), clientNum).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String message;
        private String MESSAGE;
        private Socket connection;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private int no;

        public Handler(Socket connection, int no) {
            this.connection = connection;
            this.no = no;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                try {
                    while (true) {
                        message = (String)in.readObject();
                        System.out.println("Receive message: " + message + " from client " + no);
                        MESSAGE = message.toUpperCase();
                        sendMessage(MESSAGE);
                    }
                } catch (ClassNotFoundException classNot) {
                    System.err.println("Data receieved in unknown format");
                }
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client " + no);
            } finally {
                try {
                    in.close();
                    out.close();
                    connection.close();
                } catch (IOException ioException) {
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        public void sendMessage(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Client " + no);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}