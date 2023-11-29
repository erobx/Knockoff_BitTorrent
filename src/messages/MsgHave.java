package messages;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;

public class MsgHave extends Message {

    private int pieceIndex;

    public MsgHave(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);

        this.pieceIndex = Message.byteArrayToInt(payload);
    }

    // TODO Debug
    @Override
    public void handle() {
        System.out.println("HAVE message received from" + senderID + " at " + receiverID);

        Neighbor neighborPeer = Peer.peers.get(this.senderID);

        // set the bitfield of the neighbor
        Bitfield senderBitfield = neighborPeer.bitfield;
        senderBitfield.setPiece(pieceIndex, true);

        // check if the sending peer has all the pieces
        if (senderBitfield.isFull()) {
            Peer.unfinishedPeers--;
            neighborPeer.isDone = true;
        }
    }

}
