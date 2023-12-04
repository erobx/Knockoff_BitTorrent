package messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;
import util.PeerLogger;

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
        // Log the reception of the PIECE message
        String logMessage = String.format("PIECE message received from %s at %s", senderID, receiverID);
        System.out.println(logMessage);

        // Write the received piece to the file
        // writePieceToFile(piece);
        Bitfield myBitfield = Peer.bitfield;
        int totalNumpiece = myBitfield.getNumPieces();

        PeerLogger.DownloadPieceMessage(receiverID, senderID, pieceIndex, totalNumpiece);

        // Get the sending peer and update its data rate
        Neighbor sendingPeer = Peer.peers.get(senderID);
        updateDataRate(sendingPeer, piece.length);

        // Update the bitfield of the receiving peer
        myBitfield.setPiece(pieceIndex, true);

        // Update the interest status of peers and send HAVE messages
        updatePeersInterest();

        // Handle Part 2 of the protocol
        // handlePartTwo(sendingPeer, myBitfield);
    }

    // Method to update the data rate of a peer
    private void updateDataRate(Neighbor peer, int length) {
        peer.dataRate += length;
    }

    // Method to update the interest status of peers and send HAVE messages
    private void updatePeersInterest() throws IOException {
        Bitfield myBitfield = Peer.bitfield;

        for (Neighbor peer : Peer.peers.values()) {
            // Check if the peer has an interesting piece
            boolean hasInterestingPiece = checkInterestingPiece(peer.bitfield, myBitfield);
            peer.interested = hasInterestingPiece;

            // If the peer has an interesting piece, send a HAVE message
            if (hasInterestingPiece) {
                sendMessageHave(receiverID, senderID, pieceIndex);
            }
        }
    }

    // Method to check if a peer has an interesting piece
    private boolean checkInterestingPiece(Bitfield peerBitfield, Bitfield myBitfield) {
        for (int i = 0; i < Peer.numPieces; i++) {
            if (peerBitfield.hasPiece(i) && !myBitfield.hasPiece(i)) {
                return true;
            }
        }
        return false;
    }

    // Method to send a HAVE message to a peer
    private void sendMessageHave(int receiverID, int senderID, int pieceIndex) throws IOException {
        Message.sendMessage(MessageType.HAVE, senderID, receiverID, intToByteArray(pieceIndex));
    }

    // Method to handle Part 2 of the protocol
    private void handlePartTwo(Neighbor sendingPeer, Bitfield myBitfield) throws IOException {
        // Check if the sending peer is unchoked and interested
        if (!sendingPeer.peerChoking && sendingPeer.interested) {
            // Request a random unowned piece from the sending peer
            int requestedPieceIndex = getRandomUnownedPieceIndex(sendingPeer.bitfield, myBitfield);
            sendMessageRequest(receiverID, senderID, requestedPieceIndex);
        }
    }

    // Method to get a random unowned piece index from a peer's bitfield
    private int getRandomUnownedPieceIndex(Bitfield peerBitfield, Bitfield myBitfield) {
        Random random = new Random();
        int pieceIndex = random.nextInt(Peer.numPieces);

        // Keep selecting a random piece index until it is unowned by the receiving peer
        while (!(peerBitfield.hasPiece(pieceIndex) && !myBitfield.hasPiece(pieceIndex))) {
            pieceIndex = random.nextInt(Peer.numPieces);
        }

        return pieceIndex;
    }

    // Method to send a REQUEST message to a peer
    private void sendMessageRequest(int receiverID, int senderID, int pieceIndex) throws IOException {
        Message.sendMessage(MessageType.REQUEST, senderID, receiverID, intToByteArray(pieceIndex));
    }

    // Method to write a piece to a file
    public void writePieceToFile(byte[] piece) {
        String fileLocation = "/src/peer_" + senderID + "/" + pieceIndex;
        try (FileOutputStream out = new FileOutputStream(new File(fileLocation))) {
            out.write(piece);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + fileLocation, e);
        }
    }

}
