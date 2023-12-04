package peers;

import util.Bitfield;

public class Neighbor implements Comparable {
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

    public int compareTo(Object o) // reversed so prefpeers can use a max priority queue instead of the default min
    {
        Neighbor peer = (Neighbor) o;
        if (this.dataRate < peer.dataRate) {
            return 1;
        }
        if (this.dataRate > peer.dataRate) {
            return -1;
        }
        return 0;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    // Getter and Setter for connected
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    // Getter and Setter for choking
    public boolean isChoking() {
        return choking;
    }

    public void setChoking(boolean choking) {
        this.choking = choking;
    }

    // Getter and Setter for interested
    public boolean isInterested() {
        return interested;
    }

    public void setInterested(boolean interested) {
        this.interested = interested;
    }

    // Getter and Setter for peerChoking
    public boolean isPeerChoking() {
        return peerChoking;
    }

    public void setPeerChoking(boolean peerChoking) {
        this.peerChoking = peerChoking;
    }

    // Getter and Setter for peerInterested
    public boolean isPeerInterested() {
        return peerInterested;
    }

    public void setPeerInterested(boolean peerInterested) {
        this.peerInterested = peerInterested;
    }

    // Getter and Setter for dataRate
    public double getDataRate() {
        return dataRate;
    }

    public void setDataRate(double dataRate) {
        this.dataRate = dataRate;
    }
}
