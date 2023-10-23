package test_project;

import java.io.*;
import java.net.*;

import test_project.Handshake.Result;

public class Client extends Thread {
    private final String serverAddress;
    private final int port;
    private int id;

    Socket socket;
    OutputStream out;
    InputStream in;

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

            out = socket.getOutputStream();
            out.flush();
            in = socket.getInputStream();

            handshake();

            for (int i = 0; i < 8; i++) {
                sendMessage((byte)i);
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

    void handshake() {
        sendHandshake();
        Result result = receiveHandshake();
        if (result.valid) {
            System.out.println("Peer " + result.peerID + ": handshake accepted.");
        }
    }

    void sendHandshake() {
        try {
            Handshake msg = new Handshake("P2PFILESHARINGPROJ", id);
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

    void sendMessage(byte type) {
        try {
            Message msg;
            int length = 1;                 // always have 1 byte for type
            byte[] payload;
            int index;

            switch (type) {
                // Choke - no payload
                case 0:
                    msg = new Message(length, type);
                    msg.serialize(out);
                    break;
                // Unchoke - no payload
                case 1:
                    msg = new Message(length, type);
                    msg.serialize(out);
                    break;
                // Interested - no payload
                case 2:
                    msg = new Message(length, type);
                    msg.serialize(out);
                    break;
                // Not interested - no payload
                case 3:
                    msg = new Message(length, type);
                    msg.serialize(out);
                    break;
                // Have - index payload
                case 4:
                    index = 42;
                    payload = Message.intToByteArray(index);
                    length += payload.length;
                    msg = new Message(length, type, payload);
                    msg.serialize(out);
                    break;
                // Bitfield - bitfield payload
                case 5:
                    payload = new byte[100];                // number pieces ? placeholder
                    length += payload.length;
                    msg = new Message(length, type, payload);
                    msg.serialize(out);
                    break;
                // Request - index payload
                case 6:
                    index = 69;
                    payload = Message.intToByteArray(index);
                    length += payload.length;
                    msg = new Message(length, type, payload);
                    msg.serialize(out);
                    break;
                // Piece - index + piece payload
                case 7:
                    payload = new byte[4 + 30000];          // placeholder
                    length += payload.length;
                    msg = new Message(length, type, payload);
                    msg.serialize(out);
                    break;
                default:
                    break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // private void sendNoPayload(Message msg, int length, byte type, OutputStream out) throws IOException {
    //     msg = new Message(length, type);
    //     msg.serialize(out);
    // }
}
