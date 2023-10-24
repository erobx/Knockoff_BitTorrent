package messages;

import java.io.*;

public class Handshake implements Serializable {
    private String header;
    private byte[] zeros = new byte[10];
    private int peerID;

    public Handshake(String header, int peerID) {
        this.header = header;
        this.peerID = peerID;
    }

    public void serialize(OutputStream out) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        String header = "P2PFILESHARINGPROJ";

        dataOutputStream.writeUTF(header);
        dataOutputStream.write(zeros);
        dataOutputStream.writeInt(peerID);

        dataOutputStream.flush();
    }

    public static Handshake deserialize(InputStream in) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(in);

        String header = dataInputStream.readUTF();
        byte[] zeros = new byte[10];
        dataInputStream.readFully(zeros);
        int peerID = dataInputStream.readInt();

        Handshake msg = new Handshake(header, peerID);
        return msg;
    }

    public String getHeader() {
        return header;
    }

    public int getID() {
        return peerID;
    }

    /**
     * Process for serverside handshaking
     * @param in
     * @param out
     * @param peerID
     * @return peerID of accecpted handshake or -1 if invalid handshake;
     */
    public static int serverHandshake(InputStream in, OutputStream out) { 
        Result result = receiveHandshake(in);
            if (result.valid) {
                System.out.println("Peer " + result.peerID + ": handshake accepted.");
                sendHandshake(out, result.peerID);
                return result.peerID;
            }
            else{
                System.out.println("Handshake invalid");
                return -1;
            }
    }

    public static void clientHandshake(InputStream in, OutputStream out, int peerID) {
        sendHandshake(out, peerID);
        Result result = receiveHandshake(in);
        if (result.isValid()) {
            System.out.println("Peer " + result.getPeerID() + ": handshake accepted.");
        }
    }



    public static void sendHandshake(OutputStream out, int id) {
        try {
            Handshake msg = new Handshake("P2PFILESHARINGPROJ", id);
            msg.serialize(out);
            System.out.println("Sending handshake");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Result receiveHandshake(InputStream in) {
        try {
            System.out.println("Receiving handshake");
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

    public class Result {
        boolean valid;
        int peerID;

        public Result(boolean valid, int peerID) {
            this.valid = valid;
            this.peerID = peerID;
        }

        public boolean isValid(){
            return valid;
        }

        public int getPeerID(){
            return peerID;
        }
    }
}
