package messages;

import java.io.*;
import java.nio.ByteBuffer;
import util.Bitfield;

public class MsgBitfield extends Message {

    private Bitfield bitfield;

    public MsgBitfield(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        this.bitfield = new Bitfield(payload);
    }

    

    





    
}
