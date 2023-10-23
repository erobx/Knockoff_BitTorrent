package peers;

import util.Bitfield;

public class Neighbor {
    final int peerID;
    public final String hostname;
    public final int port;
    public boolean hasFile;
    public Bitfield bitfield;


    public Neighbor(int peerID, String hostname, int port, boolean hasFile, int numPieces) {
        this.peerID = peerID;
        this.hostname = hostname;
        this.port = port;
        this.hasFile = hasFile;

        bitfield = new Bitfield(numPieces, hasFile);
    }
}
