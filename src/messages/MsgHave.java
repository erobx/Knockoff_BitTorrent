package messages;

import java.io.IOException;

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
        // String logMessage = String.format("HAVE message received from %s at %s", senderID, receiverID);
        // System.out.println(logMessage);
        Neighbor neighborPeer = Peer.peers.get(senderID);

        // is piece in bitfield?
        // if not send interested and update 

        boolean interested = hasInterestingPieces(Peer.bitfield, neighborPeer);
        MessageType messageType = interested ? MessageType.INTERESTED : MessageType.NOT_INTERESTED;

        updateNeighborBitfield(neighborPeer.bitfield);
        checkAndUpdatePeerCompletion(neighborPeer);

        Peer.peers.get(senderID).interested = interested ? true : false;
        try {
            Message.sendMessage(messageType, receiverID, senderID, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PeerLogger.ReceiveHaveMessage(receiverID, senderID, pieceIndex);
    }

    private boolean hasInterestingPieces(Bitfield myBitfield, Neighbor neighborPeer) {
        for (int i = 0; i < Peer.numPieces; i++) {
            if (neighborPeer.bitfield.hasPiece(i) && !myBitfield.hasPiece(i)) {
                return true;
            }
        }
        return false;
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
