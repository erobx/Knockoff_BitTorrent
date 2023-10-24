package messages;

import java.io.*;
import java.nio.ByteBuffer;

public abstract class Message implements Serializable {
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

    public enum MessageType {
        CHOKE(0),
        UNCHOKE(1),
        INTERESTED(2),
        NOT_INTERESTED(3),
        HAVE(4),
        BITFIELD(5),
        REQUEST(6),
        PIECE(7);

        private final int value;

        MessageType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

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

    // Serializes messages and sends to output stream
    public void serialize(OutputStream out) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);

        dataOutputStream.writeInt(length);
        dataOutputStream.writeByte(type);
        if (payload != null)
            dataOutputStream.write(payload);

        dataOutputStream.flush();
    }

    // public static Message deserialize(InputStream in, int senderID, int receiverID) throws IOException {
    //     DataInputStream dataInputStream = new DataInputStream(in);

    //     int length = dataInputStream.readInt();
    //     byte type = dataInputStream.readByte();
    //     byte[] payload = new byte[length-1];
    //     dataInputStream.readFully(payload);

    //     Message msg = new Message(length, type, payload, senderID, receiverID);
    //     return msg;
    // }

    public static byte[] intToByteArray(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    public int byteArrayToInt(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        return buffer.getInt();
    }

    public int getType() {
        return (int)type;
    }

    // public int getIndex() {
    //     return byteArrayToInt();
    // }

    public Message getMessage(InputStream in, int senderID, int receiverID) {        
        try {
            DataInputStream dataInputStream = new DataInputStream(in);

            int length = dataInputStream.readInt();
            byte type = dataInputStream.readByte();
            byte[] payload = new byte[length-1];
            dataInputStream.readFully(payload);

            int index = byteArrayToInt(payload);

            switch ((int)type) {
                // Choke - no payload
                case 0:
                    return new MsgChoke(length, (byte)MessageType.CHOKE.getValue(), null, senderID, receiverID);
                // Unchoke - no payload
                case 1:
                    return new MsgUnchoke(length, (byte)MessageType.UNCHOKE.getValue(), null, senderID, receiverID);
                // Interested - no payload
                case 2:
                    return new MsgInt(length, (byte)MessageType.INTERESTED.getValue(), null, senderID, receiverID);
                // Not interested - no payload
                case 3:
                    return new MsgNotInt(length, (byte)MessageType.NOT_INTERESTED.getValue(), null, senderID, receiverID);
                // Have - index payload
                case 4:
                    return new MsgHave(length, (byte)MessageType.HAVE.getValue(), payload, senderID, receiverID);
                // Bitfield - bitfield payload
                case 5:
                    return new MsgBitfield(length, (byte)MessageType.BITFIELD.getValue(), payload, senderID, receiverID);
                // Request - index payload
                case 6:
                    return new MsgHave(length, (byte)MessageType.REQUEST.getValue(), payload, senderID, receiverID);
                // Piece - index + piece payload
                case 7:
                    return new MsgHave(length, (byte)MessageType.REQUEST.getValue(), payload, senderID, receiverID);
            }
        } catch (IOException ex) {
            System.out.println("Failed to deserialize.");
            ex.printStackTrace();
        }
        return null;
    }
    
}
