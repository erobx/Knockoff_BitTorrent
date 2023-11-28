import java.io.IOException;
import peers.Peer;

public class peerProcess {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Invalid arguments.");
            System.exit(1);
        }

        int peerID = Integer.parseInt(args[0]);
        Peer peer = new Peer(peerID);
        try {
            peer.run();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
