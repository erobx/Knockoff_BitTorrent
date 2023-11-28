package messages;

public class MsgPiece extends Message {

    public MsgPiece(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        // TODO Auto-generated constructor stub
    }

    // TODO Fully implement
    @Override
    public void handle() {
        System.out.println("PIECE message received from" + senderID + " at " + receiverID);

    }
}
