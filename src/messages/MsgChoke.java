package messages;

import java.util.Arrays;

import peers.Neighbor;
import peers.Peer;
import util.PeerLogger;

public class MsgChoke extends Message {

    public MsgChoke(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
    }

    @Override
    public void handle() {
        String logMessage = String.format("CHOKE message received from %s at %s", senderID, receiverID);
        System.out.println(logMessage);

        Neighbor chokedNeighbor = Peer.peers.get(senderID);
        setPeerChoking(chokedNeighbor, true);
    }

    private void setPeerChoking(Neighbor neighbor, boolean isChoking) {
        neighbor.peerChoking = isChoking;
        // PeerLogger.ChokeMessage(receiverID, senderID);
    }

}
