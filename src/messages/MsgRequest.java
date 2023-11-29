package messages;

import java.io.IOException;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;

public class MsgRequest extends Message {

    private int pieceIndex;

    public MsgRequest(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        this.pieceIndex = byteArrayToInt(payload);
    }

    // TODO Fully implement
    @Override
    public void handle() throws IOException {
        System.out.println("REQUEST message received from" + senderID + " at " + receiverID);

        Neighbor peer = Peer.peers.get(this.senderID);
        Bitfield myBitfield = Peer.bitfield;

        if (!peer.choking && myBitfield.hasPiece(pieceIndex)) {
            // if we're not choking them and have the requested piece, send it

            byte[] piecePayload = createPiecePayload(pieceIndex, loadPiece(pieceIndex, senderID));
            Message.sendMessage(MessageType.PIECE, senderID, receiverID, piecePayload);

        }
    }

}
