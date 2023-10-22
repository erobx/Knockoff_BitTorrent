package test_project;

import java.io.Serializable;

public class Handshake implements Serializable {
    private String header = "P2PFILESHARINGPROJ";
    private byte[] zeros = new byte[10];
    private int peerID;

    public Handshake(int peerID) {
        this.peerID = peerID;
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
