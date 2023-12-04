package messages;

import java.io.IOException;
import java.util.Random;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;
import util.PeerLogger;

public class MsgUnchoke extends Message {

    public MsgUnchoke(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        // TODO Auto-generated constructor stub
    }

    // TODO Debug and add logging
    @Override
    public void handle() throws IOException {
        System.out.println("UNCHOKE message received from " + senderID + " at " + receiverID);

        Neighbor unchokingPeer = Peer.peers.get(senderID);

        unchokingPeer.peerChoking = true;

        // Check if the unchoking peer is interested
        if (unchokingPeer.interested) {
            // If interested, determine the next piece to request

            Bitfield unchokingBitfield = unchokingPeer.bitfield;
            Bitfield myBitfield = Peer.bitfield;

            Random random = new Random();

            int pieceIndex = getRandomUnownedPieceIndex(unchokingBitfield, myBitfield, random);

            Message.sendMessage(MessageType.REQUEST, receiverID, senderID, intToByteArray(pieceIndex));
            PeerLogger.UnchokeMessage(receiverID, unchokingPeer.peerID);
        }
    }

    private int getRandomUnownedPieceIndex(Bitfield peerBitfield, Bitfield myBitfield, Random random) {
        int pieceIndex = random.nextInt(Peer.numPieces);

        // Keep selecting a random piece index until it is unowned by the receiving peer
        while (!(peerBitfield.hasPiece(pieceIndex) && !myBitfield.hasPiece(pieceIndex))) {
            pieceIndex = random.nextInt(Peer.numPieces);
        }

        return pieceIndex;
    }

}
