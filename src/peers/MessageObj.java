package peers;

public class MessageObj {
    final byte[] message;
    final int senderID;

    MessageObj(byte[] message, int senderID) {
        this.message = message;
        this.senderID = senderID;
    }
}
