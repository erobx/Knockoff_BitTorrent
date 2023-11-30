package peers;

import util.Bitfield;

public class Neighbor {
    public final int peerID;
    public final String hostname;
    public final int port;
    public boolean hasFile;
    public Bitfield bitfield;

    // Flags/info
    public boolean isDone;
    public boolean connected;
    public boolean choking; // am I choking this peer?
    public boolean interested; // am I interested in a piece this peer has?
    public boolean peerChoking; // is this peer choking me?
    public boolean peerInterested; // is this peer interested in a piece I have?
    public double dataRate; // just bytes (updated in piece.handle) until datarate is actually calculated in
                            // Peer.updatePreferred

    public Neighbor(int peerID, String hostname, int port, boolean hasFile, int numPieces) {
        this.peerID = peerID;
        this.hostname = hostname;
        this.port = port;
        this.hasFile = hasFile;

        bitfield = new Bitfield(numPieces, hasFile);
    }
}
