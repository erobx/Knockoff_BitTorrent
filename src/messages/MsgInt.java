package messages;

public class MsgInt extends Message {

    public MsgInt(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        // TODO Auto-generated constructor stub
    }

    // TODO Fully implement
    @Override
    public void handle() {
        System.out.println("INTERESTED message received from" + senderID + " at " + receiverID);

    }

}
