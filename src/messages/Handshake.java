package messages;

import java.io.*;
import java.net.MulticastSocket;
import java.security.SecureRandom;

import javax.sound.midi.Receiver;

import util.PeerLogger;

import java.nio.ByteBuffer;

public class Handshake implements Serializable {
    private String header;
    private byte[] zeros = new byte[10];
    private int senderPID;
    private int receiverPID;

    public Handshake(String header, int senderPID) {
        this.header = header;
        this.senderPID = senderPID;
    }

    public String getHeader() {
        return header;
    }

    public int getReceiverID() {
        return receiverPID;
    }

    public int getSenderID() {
        return senderPID;
    }

    public void serialize(OutputStream out) throws IOException {
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(out));
        String header = "P2PFILESHARINGPROJ";

        ByteBuffer buffer = ByteBuffer.allocate(32);

        buffer.put(header.getBytes());
        buffer.put(zeros);
        buffer.put(String.valueOf(senderPID).getBytes());

        String message = new String(buffer.array());
        output.write(message);
        output.newLine();
        output.flush();
    }

    public static Handshake deserialize(byte[] messageBytes) throws IOException {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(messageBytes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(byteArrayInputStream));

        String message = reader.readLine();
        // Read the header 
        //String header = byteArrayInputStream.readNBytes(18).toString();
        String header = message.substring(0, 18);
        // Read the zeros 
        // byte[] zeros = byteArrayInputStream.readNBytes(10);
        String zeros = message.substring(19, 27);
        // Read the rest of the bytes as payload
        int senderID = Integer.parseInt(message.substring(28, 32));
        //int receiverID = Integer.parseInt(message.substring(28,32));
        // byte[] idBytes = byteArrayInputStream.readNBytes(4);
        //int receiverID = Message.byteArrayToInt(idBytes);
        return new Handshake(header, senderID);
    }

    // /**
    //  * Process for serverside handshaking
    //  * 
    //  * @param in
    //  * @param out
    //  * @param peerID
    //  * @return peerID of accecpted handshake or -1 if invalid handshake;
    //  */
    // public static int serverHandshake(InputStream in, OutputStream out, int senderPID) {
    //     Handshake clientHandshake = receiveHandshake(in, senderPID);
    //     if (clientHandshake.header.equals("P2PFILESHARINGPROJ")) {
    //         // System.out.println("Client Handshake received from " +
    //         // clientHandshake.senderPID);

    //         sendHandshake(out, senderPID, clientHandshake.senderPID);
    //         return clientHandshake.senderPID;
    //     } else {
    //         System.out.println("Handshake invalid");
    //         return -1;
    //     }
    // }

    // public static int clientHandshake(InputStream in, OutputStream out, int senderID, int receiverID) {
    //     sendHandshake(out, senderID, receiverID);
    //     Handshake serverHandshake = receiveHandshake(in, senderID);

    //     if (serverHandshake.getSenderID() == receiverID) {
    //         // System.out.println("Server handshake accepted from " +
    //         // serverHandshake.getSenderID());
    //         PeerLogger.TCPReceiveMessage(senderID, receiverID);
    //         return 0;
    //     } else {
    //         System.out.println("HANDSHAKE DENIED: " + serverHandshake.getSenderID() + "-> " + receiverID);
    //         return -1;
    //     }
    // }

    // public static void sendHandshake(OutputStream out, int senderPID, int receiverPID) {
    //     try {
    //         Handshake msg = new Handshake("P2PFILESHARINGPROJ", senderPID, receiverPID);
    //         msg.serialize(out);
    //         PeerLogger.TCPSendMessage(senderPID, receiverPID);
    //         // System.out.println("Sending handshake to " + receiverPID);
    //     } catch (IOException ex) {
    //         ex.printStackTrace();
    //     }
    // }

    // public static Handshake receiveHandshake(InputStream in, int senderPID) {
    //     try {
    //         Handshake msg = Handshake.deserialize(in, senderPID);
    //         // System.out.println("Receiving handshake from " + msg.getSenderID());
    //         return msg;
    //     } catch (IOException ex) {
    //         ex.printStackTrace();
    //     }
    //     return null;
    // }
    }
