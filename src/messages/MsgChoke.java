package messages;

import java.util.Arrays;

import peers.Neighbor;
import peers.Peer;

public class MsgChoke extends Message {

    public MsgChoke(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
    }

    // TODO Fully implement
    @Override
    public void handle() {
        System.out.println("CHOKE message received from" + senderID + " at " + receiverID);

        Neighbor chokedNeighbor = Peer.peers.get(senderID);
        chokedNeighbor.peerChoking = true; // the peer that sent us the message is letting us know they are choking us
    }

}
