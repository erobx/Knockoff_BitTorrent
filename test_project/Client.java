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
            // Bitfield msg is only sent as the first msg right after handshaking is done when a
            // connection is established.
            sendMessage((byte) 5);

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
            Message msg = new Message();
            int length = 1;                 // always have 1 byte for type
            byte[] payload = null;
            int index = 0;

            switch (type) {
                // Choke - no payload
                case 0:
                    handleEmptyPayloadMsg(out, msg, length, type);
                    break;
                // Unchoke - no payload
                case 1:
                    handleEmptyPayloadMsg(out, msg, length, type);
                    break;
                // Interested - no payload
                case 2:
                    handleEmptyPayloadMsg(out, msg, length, type);
                    break;
                // Not interested - no payload
                case 3:
                    handleEmptyPayloadMsg(out, msg, length, type);
                    break;
                // Have - index payload
                case 4:
                    index = 42;                             // need actual index
                    handleIndexMsg(out, msg, length, type, index, payload);
                    break;
                // Bitfield - bitfield payload
                case 5:
                    payload = new byte[100];                // number pieces ? placeholder
                    length += payload.length;
                    msg = new Message(length, type, payload);
                    msg.serialize(out);
                    // handleBitfieldMsg();
                    break;
                // Request - index payload
                case 6:
                    index = 69;                             // need actual index
                    handleIndexMsg(out, msg, length, type, index, payload);
                    break;
                // Piece - index + piece payload
                case 7:
                    payload = new byte[4 + 30000];          // placeholder
                    length += payload.length;
                    msg = new Message(length, type, payload);
                    msg.serialize(out);
                    // handlePieceMsg()
                    break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleEmptyPayloadMsg(OutputStream out, Message msg, int length, byte type) throws IOException {
        msg = new Message(length, type);
        msg.serialize(out);
    }

    private void handleIndexMsg(OutputStream out, Message msg, int length, byte type, int index, byte[] payload) throws IOException {
        payload = Message.intToByteArray(index);
        length += payload.length;
        msg = new Message(length, type, payload);
        msg.serialize(out);
    }

    private void handleBitfieldMsg() {}

    private void handlePieceMsg() {}
}
