package messages;

import java.io.IOException;
import java.util.Random;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;

public class MsgInt extends Message {

    public MsgInt(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
    }

    // TODO Fully implement
    @Override
    public void handle() throws IOException {
        String logMessage = String.format("INTERESTED message received from %s at %s", senderID, receiverID);
        System.out.println(logMessage);

        Neighbor unchokingPeer = Peer.peers.get(senderID);
        setPeerChoking(unchokingPeer, true);

        if (unchokingPeer.interested) {
            requestRandomPiece(unchokingPeer.bitfield);
        }
    }

    private void setPeerChoking(Neighbor peer, boolean isChoking) {
        peer.peerChoking = isChoking;
    }

    private void requestRandomPiece(Bitfield unchokingBitfield) throws IOException {
        Bitfield myBitfield = Peer.bitfield;
        Random random = new Random();

        int pieceIndex = getRandomUnownedPiece(unchokingBitfield, myBitfield, random);
        sendMessageRequest(pieceIndex);
    }

    private int getRandomUnownedPiece(Bitfield unchokingBitfield, Bitfield myBitfield, Random random) {
        int pieceIndex = random.nextInt(Peer.numPieces);

        while (!(unchokingBitfield.hasPiece(pieceIndex) && !myBitfield.hasPiece(pieceIndex))) {
            pieceIndex = random.nextInt(Peer.numPieces);
        }

        return pieceIndex;
    }

    private void sendMessageRequest(int pieceIndex) throws IOException {
        Message.sendMessage(MessageType.REQUEST, senderID, receiverID, intToByteArray(pieceIndex));
    }

}
