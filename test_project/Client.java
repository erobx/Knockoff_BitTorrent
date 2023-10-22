package test_project;

import java.io.*;
import java.net.*;

import test_project.Handshake.Result;

public class Client extends Thread {
    private final String serverAddress;
    private final int port;
    private int id;

    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;

    public Client(String serverAddress, int port, int id) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverAddress, port);
            System.out.println("Client " + id + " connected to server on port " + port + ". ");

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            sendHandshake();
            Result result = receiveHandshake();
            if (result.valid) {
                System.out.println("Peer " + result.peerID + ": handshake accepted.");
            }

            // Main logic here
            while (true) {
                break;
            }
        } catch (IOException ex) {
            System.err.println("Connection terminated.");
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

    void sendHandshake() {
        Handshake handshake = new Handshake(id);
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
