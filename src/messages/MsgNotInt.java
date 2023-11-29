package messages;

import peers.Peer;

public class MsgNotInt extends Message {

    public MsgNotInt(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
    }

    // TODO Fully implement
    @Override
    public void handle() {
        System.out.println("NOT_INTERESTED message received from" + senderID + " at " + receiverID);

        Peer.peers.get(senderID).peerInterested = false;
    }

}
