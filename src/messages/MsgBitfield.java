package messages;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;

public class MsgBitfield extends Message {

    private Bitfield receivedBitfield;

    public MsgBitfield(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        this.receivedBitfield = new Bitfield(payload);
    }

    // TODO Debug and add logging
    @Override
    public void handle() throws IOException {
        System.out.println("BITFIELD message received from" + senderID + " at " + receiverID + " : "
                + Arrays.toString(receivedBitfield.getBitfield()));

        // check if bitfield has the pieces that receiver wants

        Bitfield myBitfield = Peer.bitfield;

        boolean interested = false;

        for (int i = 0; i < Peer.numPieces; i++) {
            if (receivedBitfield.hasPiece(i) && !myBitfield.hasPiece(i)) {
                interested = true;
            }
        }

        if (interested) { // send interested
            // receive/sender id swapped because receive is this peer and we want that to
            // send the message
            Message.sendMessage(MessageType.INTERESTED, senderID, receiverID, null);
        } else { // send not interested
            // receive/sender id swapped because receive is this peer and we want that to
            // send the message
            Message.sendMessage(MessageType.NOT_INTERESTED, senderID, receiverID, null);
        }
    }

}
