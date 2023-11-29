package messages;

import java.io.IOException;
import java.util.Random;

import peers.Neighbor;
import peers.Peer;
import util.Bitfield;

public class MsgUnchoke extends Message {

    public MsgUnchoke(int length, byte type, byte[] payload, int senderID, int receiverID) {
        super(length, type, payload, senderID, receiverID);
        // TODO Auto-generated constructor stub
    }

    // TODO Debug and add logging
    @Override
    public void handle() throws IOException {
        System.out.println("UNCHOKE message received from" + senderID + " at " + receiverID);

        Neighbor unchokingPeer = Peer.peers.get(this.senderID);
        unchokingPeer.peerChoking = true;

        if (unchokingPeer.interested) {
            Bitfield unchokingBitfield = unchokingPeer.bitfield;
            Bitfield myBitfield = Peer.bitfield;

            Random random = new Random();

            // get the next piece randomly
            int pieceIndex = random.nextInt(Peer.numPieces);
            while (!(unchokingBitfield.hasPiece(pieceIndex) && !myBitfield.hasPiece(pieceIndex))) {
                pieceIndex = random.nextInt(Peer.numPieces);
            }

            sendMessage(MessageType.REQUEST, senderID, receiverID, intToByteArray(pieceIndex));

        }
    }

}
