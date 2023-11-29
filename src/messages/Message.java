package messages;

import java.io.*;
import java.net.http.HttpClient.Redirect;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.swing.plaf.PanelUI;

import peers.Peer;
import util.ClientHandler;

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

        public static MessageType getTypeByInt(int value) {
            for (MessageType messageType : MessageType.values()) {
                if (messageType.getValue() == value) {
                    return messageType;
                }
            }
            return null; // Or throw an exception if needed
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
    // TODO this isn't being picked up by the input stream
    public void serialize(OutputStream out) throws IOException {
        // DataOutputStream dataOutputStream = new DataOutputStream(out);
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(out));

        // dataOutputStream.writeInt(length);
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + payload.length);
        // System.out.println("Buffer Length: " + buffer.remaining() + 1);
        buffer.putInt(length);
        // System.out.println("Buffer Pos: " + buffer.position());
        System.out.println("Length: " + length);
        //dataOutputStream.writeByte(type);
        buffer.put(type);
        // System.out.println("Buffer Pos: " + buffer.position());
        System.out.println("Type: " + type);
        if (payload != null) {
            // dataOutputStream.write(payload);
            buffer.put(payload);
            // System.out.println("Buffer Pos: " + buffer.position());
            System.out.println("Payload: " + payload);
        }
        //dataOutputStream.flush();
        //Charset charset = Charset.forName("US-ASCII");
        System.out.println("Serialized message: " + (buffer.array()).toString());
        System.out.println("Deserialized message: " + (buffer.array()).toString().getBytes());
        output.write(buffer.array().toString());
        output.newLine();
        output.flush(); 
    }

    // public static Message deserialize(InputStream in, int senderID, int
    // receiverID) throws IOException {
    // DataInputStream dataInputStream = new DataInputStream(in);

    // int length = dataInputStream.readInt();
    // byte type = dataInputStream.readByte();
    // byte[] payload = new byte[length-1];
    // dataInputStream.readFully(payload);

    // Message msg = new Message(length, type, payload, senderID, receiverID);
    // return msg;
    // }

    public static byte[] intToByteArray(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    public static int byteArrayToInt(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        return buffer.getInt();
    }

    public int getType() {
        return (int) type;
    }

    // public int getIndex() {
    // return byteArrayToInt();
    // }

    // Essentially Deserialize just checks the input stream for a message and
    // returns the message as a message obj
    public static Message getMessage(byte[] messageBytes, int senderID, int receiverID) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(messageBytes);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

            int length = dataInputStream.readInt();
            System.out.println("Length: " + length);
            byte type = dataInputStream.readByte();
            System.out.println("Type: " + type);
            byte[] payload = new byte[length - 1];
            dataInputStream.readFully(payload);

            int index = byteArrayToInt(payload);

            Message message = null;

            switch (MessageType.getTypeByInt((int) type)) {
                // Choke - no payload
                case CHOKE ->
                    message = new MsgChoke(length, (byte) MessageType.CHOKE.getValue(), null, senderID, receiverID);
                // Unchoke - no payload
                case UNCHOKE ->
                    message = new MsgUnchoke(length, (byte) MessageType.UNCHOKE.getValue(), null, senderID, receiverID);
                // Interested - no payload
                case INTERESTED ->
                    message = new MsgInt(length, (byte) MessageType.INTERESTED.getValue(), null, senderID, receiverID);
                // Not interested - no payload
                case NOT_INTERESTED ->
                    message = new MsgNotInt(length, (byte) MessageType.NOT_INTERESTED.getValue(), null, senderID,
                            receiverID);
                // Have - index payload
                case HAVE ->
                    message = new MsgHave(length, (byte) MessageType.HAVE.getValue(), payload, senderID, receiverID);
                // Bitfield - bitfield payload
                case BITFIELD ->
                    message = new MsgBitfield(length, (byte) MessageType.BITFIELD.getValue(), payload, senderID,
                            receiverID);
                // Request - index payload
                case REQUEST ->
                    message = new MsgHave(length, (byte) MessageType.REQUEST.getValue(), payload, senderID, receiverID);
                // Piece - index + piece payload
                // TODO PIECE message could be setup wrong
                case PIECE ->
                    message = new MsgPiece(length, (byte) MessageType.PIECE.getValue(), payload, senderID, receiverID);
                // Add more cases as needed
            }

            return message;

        } catch (IOException ex) {
            System.out.println("Failed to deserialize.");
            ex.printStackTrace();
        }
        return null;
    }

    public static void sendMessage(Message.MessageType type, int receiverID, int senderID, byte[] payload)
            throws IOException {
        util.ClientHandler ch = Peer.clients.get(receiverID);
        if (ch == null) {
            throw new RuntimeException("Client receiver ID Cannot be found");
        }

        // TODO check if parameters are correct
        switch (type) {
            // Choke - no payload
            case CHOKE -> {
                int length = 0; // no payload
                Message msg = new MsgChoke(length + 1, (byte) type.getValue(), null, senderID,
                        receiverID);
                msg.serialize(ch.getSocket().getOutputStream());
                System.out.println("CHOKE message sent from " + senderID + " to " + receiverID);
            }
            // Unchoke - no payload
            case UNCHOKE -> {
                int length = 0; // no payload
                Message msg = new MsgUnchoke(length + 1, (byte) type.getValue(), null, senderID,
                        receiverID);
                msg.serialize(ch.getSocket().getOutputStream());
                System.out.println("UNCHOKE message sent from " + senderID + " to " + receiverID);
            }
            // Interested - no payload
            case INTERESTED -> {
                int length = 0; // no payload
                Message msg = new MsgInt(length + 1, (byte) type.getValue(), null, senderID,
                        receiverID);
                msg.serialize(ch.getSocket().getOutputStream());
                System.out.println("INTERESTED message sent from " + senderID + " to " + receiverID);
            }
            // Not interested - no payload
            case NOT_INTERESTED -> {
                int length = 0; // no payload
                Message msg = new MsgNotInt(length + 1, (byte) type.getValue(), null, senderID,
                        receiverID);
                msg.serialize(ch.getSocket().getOutputStream());
                System.out.println("NOT_INTERESTED message sent from " + senderID + " to " + receiverID);
            }
            // Have - index payload TODO adjust payload
            case HAVE -> {
                int length = 0; // no payload
                Message msg = new MsgHave(length + 1, (byte) type.getValue(), null, senderID,
                        receiverID);
                msg.serialize(ch.getSocket().getOutputStream());
                System.out.println("HAVE message sent from " + senderID + " to " + receiverID);
            }
            // Bitfield - bitfield payload
            case BITFIELD -> {
                int length = payload.length;
                Message msg = new MsgBitfield(length + 1, (byte) type.getValue(), payload, senderID,
                        receiverID);
                msg.serialize(ch.getSocket().getOutputStream());
                System.out.println("BITFIELD message sent from " + senderID + " to " + receiverID);
            }
            // Request - index payload TODO adjust payload and length
            case REQUEST -> {
                int length = payload.length;
                Message msg = new MsgRequest(length + 1, (byte) type.getValue(), payload, senderID,
                        receiverID);
                msg.serialize(ch.getSocket().getOutputStream());
                System.out.println("REQUEST message sent from " + senderID + " to " + receiverID);
            }
            // Piece - index + piece payload TODO adjust payload and length
            case PIECE -> {
                int length = payload.length;
                Message msg = new MsgPiece(length + 1, (byte) type.getValue(), payload, senderID,
                        receiverID);
                msg.serialize(ch.getSocket().getOutputStream());
                System.out.println("PIECE message sent from " + senderID + " to " + receiverID);
            }
        }

    }

    public void handle() throws Exception {
        throw new Exception("Handler not overriden default handler called");
    }
}
