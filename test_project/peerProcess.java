package test_project;
import java.io.*;
import java.net.*;
import java.util.Vector;

/*
 * Once peerProcess starts, it reads Common.cfg and PeerInfo.cfg.
 * If it has the file, 1's else 0's in bitfield.
 * Listen on port and connect to other peers if not first.
 * 
 * Once connected to at least one peer, it will start to exchange pieces.
 */

public class peerProcess {

    // Takes peer ID as parameter on the command line
    public String peerID;

    static int k;               // preferred neighbors
    static int p;               // unchoking interval
    static int m;               // optimistic unchoking interval
    static String fileName;
    static int fileSize;        // bytes
    static int pieceSize;       // bytes
    static int numPieces;
    static int lastPieceSize;   // byte size of last piece since not always equal to pieceSize

    public static Vector<Boolean> bitfield = new Vector<>();
    static boolean hasFile = false;
    static int port;            // should pass this to client and server I'd assume
    static boolean validPeerID = false;

    public Vector<PeerInfo> peers = new Vector<>();

    public peerProcess(String peerID) {
        this.peerID = peerID;
        getConfig();
        getPeers();
        // printPeers();
    }

    /*
     * Reads Common.cfg
     * Sets variables and calculcates pieces and last piece size
     */
    private void getConfig() {
        try {
            String line;
            BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                String key = tokens[0];
                String value = tokens[1];

                switch (key) {
                    case "NumberOfPreferredNeighbors":
                        k = Integer.parseInt(value);
                        break;
                    case "UnchokingInterval":
                        p = Integer.parseInt(value);
                        break;
                    case "OptimisticUnchokingInterval":
                        m = Integer.parseInt(value);
                        break;
                    case "FileName":
                        fileName = value;
                        break;
                    case "FileSize":
                        fileSize = Integer.parseInt(value);
                        break;
                    case "PieceSize":
                        pieceSize = Integer.parseInt(value);
                        break;
                }
            }
            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        numPieces = (int) Math.ceil((double)fileSize / pieceSize);
        lastPieceSize = fileSize - ((numPieces-1) * pieceSize);
    }

    /*
     * Reads PeerInfo.cfg
     * Loops through file until the process finds its ID
     * Adds previous peers to list of peers to make TCP connections to
     * Exits program if peerID given is not in PeerInfo.cfg
     */
    private void getPeers() {
        try {
            String line;
            BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                String pID = tokens[0];
                String pAddress = tokens[1];
                String temp_port = tokens[2];

                // Right peer process
                if (pID.equals(peerID)) {
                    port = Integer.parseInt(tokens[2]);
                    hasFile = tokens[3].equals("1");
                    validPeerID = true;
                    break;
                }
                peers.addElement(new PeerInfo(pID, pAddress, temp_port));
            }
            in.close();

            if (!validPeerID) {
                System.out.println("Invalid peer ID.");
                System.exit(0);
            }
            setBitfield();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    // Print debugging for the win!
    private void printPeers() {
        if (!peers.isEmpty()) {
            for (PeerInfo pi : peers) {
                System.out.println(pi.pID);
            }
        } else {
            System.out.println("First peer!");
        }
    }

    private void setBitfield() {
        if (hasFile) {
            for (int i = 0; i < numPieces; i++) {
                bitfield.add(i, true);
            }
        } else {
            for (int i = 0; i < numPieces; i++) {
                bitfield.add(i, false);
            }
        }
    }

    /*
     * Server functionality of the peer
     */
    public void listen() throws Exception {
        ServerSocket listener = new ServerSocket(port);
        try {
            while (true) {
                new Handler(listener.accept()).start();
                // Logging bs
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private Socket connection;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public Handler(Socket connection) {
            this.connection = connection;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                try {

                } catch (Exception ex) {

                }
            } catch (Exception ex) {

            } finally {

            }
        }
    }

    /*
     * Client functionality of the peer
     */



    public static void main(String[] args) {
        peerProcess pp = new peerProcess(args[0]);
    }
}
