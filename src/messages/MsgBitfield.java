package messages;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;
import util.PeerLogger;

public class MsgBitfield extends Message {

    private Bitfield receivedBitfield;

    public MsgBitfield(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        this.receivedBitfield = new Bitfield(payload);
    }

    // TODO Debug and add logging
    @Override
    public void handle() throws IOException {
        // System.out.println("BITFIELD message received from " + senderID + " at " +
        // receiverID + " : "
        // + Arrays.toString(receivedBitfield.getBitfield()));

        System.out.println("BITFIELD RECEIVED FROM " + senderID);
        PeerLogger.bitfieldMessage(senderID, receiverID);

        // Check if bitfield has the pieces that receiver wants
        Bitfield myBitfield = Peer.bitfield;
        boolean interested = hasInterestingPieces(myBitfield);

        // Peer.peers.get(senderID).peerInterested = neighborIsInterested(myBitfield);

        MessageType messageType = interested ? MessageType.INTERESTED : MessageType.NOT_INTERESTED;

        // Swap senderID and receiverID for the message
        Message.sendMessage(messageType, receiverID, senderID, null);
    }

    private boolean hasInterestingPieces(Bitfield myBitfield) {
        for (int i = 0; i < Peer.numPieces; i++) {
            if (receivedBitfield.hasPiece(i) && !myBitfield.hasPiece(i)) {
                return true;
            }
        }
        return false;
    }

    // private boolean neighborIsInterested(Bitfield myBitfield) {
    //     for (int i = 0; i < Peer.numPieces; i++) {
    //         if (!receivedBitfield.hasPiece(i) && myBitfield.hasPiece(i)) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

}
