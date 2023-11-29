package messages;

import java.io.*;
import java.security.SecureRandom;

import javax.sound.midi.Receiver;

public class Handshake implements Serializable {
    private String header;
    private byte[] zeros = new byte[10];
    private int senderPID;
    private int receiverPID;

    public Handshake(String header, int senderPID, int receiverPID) {
        this.header = header;
        this.senderPID = senderPID;
        this.receiverPID = receiverPID;
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
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        String header = "P2PFILESHARINGPROJ";

        dataOutputStream.writeUTF(header);
        dataOutputStream.write(zeros);
        dataOutputStream.writeInt(senderPID);

        dataOutputStream.flush();
    }

    public static Handshake deserialize(InputStream in, int senderPID) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(in);

        String header = dataInputStream.readUTF();
        byte[] zeros = new byte[10];
        dataInputStream.readFully(zeros);
        int receiverID = dataInputStream.readInt();

        Handshake msg = new Handshake(header, receiverID, senderPID);
        return msg;
    }

    /**
     * Process for serverside handshaking
     * @param in
     * @param out
     * @param peerID
     * @return peerID of accecpted handshake or -1 if invalid handshake;
     */
    public static int serverHandshake(InputStream in, OutputStream out, int senderPID) { 
        Handshake clientHandshake = receiveHandshake(in, senderPID);
        if (clientHandshake.header.equals("P2PFILESHARINGPROJ")) {
            System.out.println("Client Handshake received from " + clientHandshake.senderPID);
            sendHandshake(out, senderPID, clientHandshake.senderPID);
            return clientHandshake.senderPID;
        }
        else{
            System.out.println("Handshake invalid");
            return -1;
        }
    }

    public static void clientHandshake(InputStream in, OutputStream out, int senderID, int receiverID) {
        sendHandshake(out, senderID, receiverID);
        Handshake serverHandshake = receiveHandshake(in, senderID);
        
        if (serverHandshake.getSenderID() == receiverID) {
            System.out.println("Server handshake accepted from " + serverHandshake.getSenderID());
        }
        else {
            System.out.println("HANDSHAKE DENIED: " + serverHandshake.getSenderID() + "-> " + receiverID );
        }
    }



    public static void sendHandshake(OutputStream out, int senderPID, int receiverPID) {
        try {
            Handshake msg = new Handshake("P2PFILESHARINGPROJ", senderPID, receiverPID);
            msg.serialize(out);
            System.out.println("Sending handshake to " + receiverPID);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Handshake receiveHandshake(InputStream in, int senderPID) {
        try {
            Handshake msg = Handshake.deserialize(in, senderPID);
            System.out.println("Receiving handshake from " + msg.getSenderID());
            return msg;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public class Result {
        boolean valid;
        int senderPID;
        int receiverPID;

        public Result(boolean valid, int senderPID, int receiverPID) {
            this.valid = valid;
            this.senderPID = senderPID;
            this.receiverPID = receiverPID;
        }
        
    }
}
