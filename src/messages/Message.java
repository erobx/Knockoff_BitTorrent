package messages;

import java.io.*;
import java.nio.*;

public class Message implements Serializable{
    public final int senderID;
    public final int receiverID;

    public Message(int senderID, int receiverID) {
        this.senderID = senderID;
        this.receiverID = receiverID;
    }

    public static Message decodeMessage() {return null;};
}
