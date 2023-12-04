package messages;

import java.io.IOException;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;
import util.PeerLogger;

public class MsgRequest extends Message {

    private int pieceIndex;

    public MsgRequest(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        this.pieceIndex = byteArrayToInt(payload);
    }

    @Override
    public void handle() throws IOException {
        String logMessage = String.format("REQUEST message received from %s at %s", senderID, receiverID);
        System.out.println(logMessage);

        // Get information about the requesting peer and the local bitfield
        Neighbor requestingPeer = Peer.peers.get(senderID);
        Bitfield myBitfield = Peer.bitfield;

        // Check if the requesting peer is not choking and the requested piece is
        // available
        if (!requestingPeer.choking && myBitfield.hasPiece(pieceIndex)) {
            // If conditions are met, send the requested piece to the requesting peer
            byte[] piecePayload = createPiecePayload(pieceIndex, loadPiece(pieceIndex));
            // byte[] piecePayload = Peer.bitfield[pieceIndex];

            Message.sendMessage(MessageType.PIECE, receiverID, senderID, piecePayload);

            // SEND RANDOM PIECE IN HAVE MESSAGE
            // Message.sendMessage(MessageType.HAVE, receiverID, senderID, null);
        }
        PeerLogger.ReceiveRequestMessage(receiverID, senderID, pieceIndex);
    }

}
