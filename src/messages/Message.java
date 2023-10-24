package messages;

import java.io.*;
import java.nio.ByteBuffer;

public class Message implements Serializable {
    /*
     * Example:
     * Message length is 25 bytes. Message length field will contain the value 21
     * since the field itself is 4 bytes. Removing 1 byte (type field) will give
     * us the length of the payload, 20.
     */
    
    private int length;
    private byte type;
    private byte[] payload = null;

    public final int senderID;
    public final int receiverID;

    public Message(int senderID, int receiverID) {
        this.senderID = senderID;
        this.receiverID = receiverID;
    }


    public Message(int length, byte type, byte[] payload, int senderID, int receiverID) {
        this.length = length;
        this.type = type;
        this.payload = payload;
        this.senderID = senderID;
        this.receiverID = receiverID;
    }

    // public Message(int length, byte type, byte[] payload) {
    //     this.length = length;
    //     this.type = type;
    //     this.payload = payload;
    // }

    // public Message(int length, byte type) {
    //     this.length = length;
    //     this.type = type;
    // }

    public void serialize(OutputStream out) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);

        dataOutputStream.writeInt(length);
        dataOutputStream.writeByte(type);
        if (payload != null)
            dataOutputStream.write(payload);

        dataOutputStream.flush();
    }

    public static Message deserialize(InputStream in, int senderID, int receiverID) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(in);

        int length = dataInputStream.readInt();
        byte type = dataInputStream.readByte();
        byte[] payload = new byte[length-1];
        dataInputStream.readFully(payload);

        Message msg = new Message(length, type, payload, senderID,receiverID);
        return msg;
    }

    public static byte[] intToByteArray(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    public int byteArrayToInt() {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        return buffer.getInt();
    }

    public int getType() {
        return (int)type;
    }

    public int getIndex() {
        return byteArrayToInt();
    }

    void getMessage(InputStream in) {
        try {
            Message msg = Message.deserialize(in);
            int type = msg.getType();
            int index;

            switch (type) {
                // Choke - no payload
                case 0:
                    System.out.println("Choke me daddy.");
                    break;
                // Unchoke - no payload
                case 1:
                    System.out.println("Unchoke me daddy uWu.");
                    break;
                // Interested - no payload
                case 2:
                    System.out.println("Omg you're so tallllll!");
                    break;
                // Not interested - no payload
                case 3:
                    System.out.println("Sorry I have a boyfriend.");
                    break;
                // Have - index payload
                case 4:
                    index = msg.getIndex();
                    System.out.println("I have piece at index: " + index);
                    break;
                // Bitfield - bitfield payload
                case 5:
                    System.out.println("Send me your bitfield bitch.");
                    break;
                // Request - index payload
                case 6:
                    index = msg.getIndex();
                    System.out.println("Requested index: " + index);
                    // get piece
                    break;
                // Piece - index + piece payload
                case 7:
                    System.out.println("Give me a piece of dat ass.");
                    break;
                default:
                    break;
            }
        } catch (IOException ex) {
            System.out.println("Failed to deserialize.");
            ex.printStackTrace();
        }
    }
}
