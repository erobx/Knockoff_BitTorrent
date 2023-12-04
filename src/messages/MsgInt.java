package messages;

import java.io.IOException;
import java.util.Random;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;
import util.PeerLogger;

public class MsgInt extends Message {

    public MsgInt(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
    }

    // TODO Fully implement
    @Override
    public void handle() throws IOException {
        String logMessage = String.format("INTERESTED message received from %s at %s", senderID, receiverID);
        System.out.println(logMessage);

        PeerLogger.ReceiveInterestedMessage(receiverID, senderID);

        Neighbor newNeighbor = Peer.peers.get(senderID);
        newNeighbor.setPeerInterested(true);
        Peer.peers.put(senderID, newNeighbor);
        // Peer.peers.get(senderID).setPeerInterested(true);
        // sendingPeer.peerInterested = true;

        // requestRandomPiece(newNeighbor.bitfield);
    }

    private void requestRandomPiece(Bitfield sendingBitfield) throws IOException {
        Bitfield myBitfield = Peer.bitfield;
        Random random = new Random();

        int pieceIndex = getRandomUnownedPiece(sendingBitfield, myBitfield, random);
        sendMessageRequest(pieceIndex);
    }

    private int getRandomUnownedPiece(Bitfield sendingBitfield, Bitfield myBitfield, Random random) {
        int pieceIndex = random.nextInt(Peer.numPieces);

        if (!myBitfield.isFull()) {
            while (!(sendingBitfield.hasPiece(pieceIndex) && !myBitfield.hasPiece(pieceIndex))) {
                pieceIndex = random.nextInt(Peer.numPieces);
                System.out.println("getting stuck");
            }
        }

        return pieceIndex;
    }

    private void sendMessageRequest(int pieceIndex) throws IOException {
        Message.sendMessage(MessageType.HAVE, receiverID, senderID, intToByteArray(pieceIndex));
    }

}
