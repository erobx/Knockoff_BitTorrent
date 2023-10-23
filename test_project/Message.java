package test_project;

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

    public Message() {
    }

    public Message(int length, byte type, byte[] payload) {
        this.length = length;
        this.type = type;
        this.payload = payload;
    }

    public Message(int length, byte type) {
        this.length = length;
        this.type = type;
    }

    public void serialize(OutputStream out) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);

        dataOutputStream.writeInt(length);
        dataOutputStream.writeByte(type);
        if (payload != null)
            dataOutputStream.write(payload);

        dataOutputStream.flush();
    }

    public static Message deserialize(InputStream in) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(in);

        int length = dataInputStream.readInt();
        byte type = dataInputStream.readByte();
        byte[] payload = new byte[length-1];
        dataInputStream.readFully(payload);

        Message msg = new Message(length, type, payload);
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
}
