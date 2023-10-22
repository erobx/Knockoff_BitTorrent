package test_project;

import java.io.*;
import java.net.*;

public class Client extends Thread {
    private final String serverAddress;
    private final int port;
    private int no;

    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;

    public Client(String serverAddress, int port, int no) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.no = no;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverAddress, port);
            System.out.println("Connected to server on port " + port + ". Client " + no + ".");

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("Input a sentence: ");
                String message = reader.readLine();
                sendMessage(message);
                try {
                    String response = (String)in.readObject();
                    System.out.println("Received message: " + response);
                } catch (ClassNotFoundException classNot) {
                    System.err.println("No data received.");
                }
            }
        } catch (IOException ex) {
            System.err.println("\nConnection terminated.");
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
