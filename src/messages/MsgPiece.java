package messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;

public class MsgPiece extends Message {

    private int pieceIndex;
    private byte[] piece;

    public MsgPiece(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        Pair<Integer, byte[]> piecePayload = decodePiecePayload(payload);
        this.pieceIndex = piecePayload.getFirst();
        this.piece = piecePayload.getSecond();
    }

    // TODO Fully implement
    @Override
    public void handle() throws IOException {
        System.out.println("PIECE message received from" + senderID + " at " + receiverID);

        writePieceToFile(piece);

        Neighbor sendingPeer = Peer.peers.get(this.senderID);
        sendingPeer.datarate += piece.length;

        Bitfield myBitfield = Peer.bitfield;
        myBitfield.setPiece(pieceIndex, true);

        for (Neighbor peer : Peer.peers.values()) { // for each peer
            // check if you are interested in them
            for (int i = 0; i < Peer.numPieces; i++) {
                if (peer.bitfield.hasPiece(i) && !myBitfield.hasPiece(i)) {
                    peer.interested = true;
                    break;
                }
            }
            Message.sendMessage(MessageType.HAVE, receiverID, senderID, intToByteArray(pieceIndex));
        }

        // PART 2
        if (!sendingPeer.peerChoking && sendingPeer.interested) {

            Random random = new Random();
            int pieceIndex = random.nextInt(Peer.numPieces);
            while (!(sendingPeer.bitfield.hasPiece(pieceIndex) && !myBitfield.hasPiece(pieceIndex))) {
                pieceIndex = random.nextInt(Peer.numPieces);
            }
            Message.sendMessage(MessageType.REQUEST, receiverID, senderID, intToByteArray(pieceIndex));
        }
    }

    public void writePieceToFile(byte[] piece) {
        String fileLocation = "/src/peer_" + senderID + "/" + pieceIndex;
        FileOutputStream out;
        try {
            out = new FileOutputStream(new File(fileLocation));
            out.write(piece);
            out.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot find file: " + fileLocation, e);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to file: " + fileLocation, e);
        }

    }
}
