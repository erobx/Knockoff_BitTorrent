package messages;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;
import util.PeerLogger;

public class MsgHave extends Message {

    private int pieceIndex;

    public MsgHave(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);

        this.pieceIndex = Message.byteArrayToInt(payload);
    }

    // TODO Debug
    @Override
    public void handle() {
        String logMessage = String.format("HAVE message received from %s at %s", senderID, receiverID);
        System.out.println(logMessage);

        Neighbor neighborPeer = Peer.peers.get(senderID);
        updateNeighborBitfield(neighborPeer.bitfield);
        checkAndUpdatePeerCompletion(neighborPeer);

        PeerLogger.ReceiveHaveMessage(receiverID, senderID, pieceIndex);
    }

    private void updateNeighborBitfield(Bitfield bitfield) {
        bitfield.setPiece(pieceIndex, true);
    }

    private void checkAndUpdatePeerCompletion(Neighbor neighbor) {
        Bitfield neighborBitfield = neighbor.bitfield;

        if (neighborBitfield.isFull()) {
            if (!neighbor.isDone == true) 
                Peer.unfinishedPeers--;
            neighbor.isDone = true;
        }
    }

}
