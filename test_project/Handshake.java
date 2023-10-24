package test_project;

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

    public class Result {
        boolean valid;
        int peerID;

        public Result(boolean valid, int peerID) {
            this.valid = valid;
            this.peerID = peerID;
        }
    }
}
