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
        System.out.println("INTERESTED message received from" + senderID + " at " + receiverID);

        Neighbor unchokingPeer = Peer.peers.get(this.senderID);
        unchokingPeer.peerChoking = true;

        if (unchokingPeer.interested) {
            Bitfield unchokingBitfield = unchokingPeer.bitfield;
            Bitfield myBitfield = Peer.bitfield;

            Random random = new Random();

            int pieceIndex = random.nextInt(Peer.numPieces);
            while (!(unchokingBitfield.hasPiece(pieceIndex) && !myBitfield.hasPiece(pieceIndex))) {
                pieceIndex = random.nextInt(Peer.numPieces);
            }

            Message.sendMessage(MessageType.REQUEST, senderID, receiverID, intToByteArray(pieceIndex));
        }
    }

}
