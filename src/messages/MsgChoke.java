package messages;

import java.util.Arrays;

public class MsgChoke extends Message {

    public MsgChoke(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        // TODO Auto-generated constructor stub
    }

    // TODO Fully implement
    @Override
    public void handle() {
        System.out.println("CHOKE message received from" + senderID + " at " + receiverID);

    }

}
