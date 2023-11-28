package messages;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import peers.Peer;
import util.Bitfield;

public class MsgBitfield extends Message {

    private Bitfield bitfield;

    public MsgBitfield(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        this.bitfield = new Bitfield(payload);
    }

    // TODO Fully implement
    @Override
    public void handle() {
        System.out.println("BITFIELD message received from" + senderID + " at " + receiverID + " : "
                + Arrays.toString(bitfield.getBitfield()));

    }

}
