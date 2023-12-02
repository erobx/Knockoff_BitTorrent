package messages;

import peers.Peer;
import util.PeerLogger;

public class MsgNotInt extends Message {

    public MsgNotInt(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
    }

    // TODO Fully implement
    @Override
    public void handle() {
        String logMessage = String.format("NOT_INTERESTED message received from %s at %s", senderID, receiverID);
        System.out.println(logMessage);

        PeerLogger.ReceiveNotInterestedMessage(receiverID, receiverID);

        setPeerInterested(senderID, false);
    }

    private void setPeerInterested(int senderID, boolean isInterested) {
        Peer.peers.get(senderID).peerInterested = isInterested;
    }

}
