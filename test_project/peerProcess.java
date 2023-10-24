package test_project;
import java.io.*;
import java.util.Vector;
import java.lang.System;

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

    static int port;            // should pass this to client and server I'd assume
    static boolean hasFile = false;
    static boolean validPeerID = false;

    long lastUnchokingTime; // Last time from when preferred neighbors was updated (unchoked peers) 
    long lastOptimisticUnchokingTime; // Last time from when the optimistically unchoked neighbor was updated
    
    public Vector<Boolean> bitfield = new Vector<>();
    public Vector<PeerInfo> peers = new Vector<>();

    static int maxPeers;

    public peerProcess(String peerID) {
        this.peerID = peerID;
        getConfig();
        getPeers();

        if (peers.isEmpty()) {
            startServer(port);
        } else if (peers.size() == maxPeers-1) {
            startClients();
        } else {
            startBoth(port);
        }
    }

    /*
     * Reads Common.cfg
     * Sets variables and calculates pieces and last piece size
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
            maxPeers = (int)in.lines().count();
            in.close();

            in = new BufferedReader(new FileReader("PeerInfo.cfg"));
            
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

    private void startServer(int port) {
        Server server = new Server(port);
        server.start();
    }

    private void startClient(String serverAddress, int port, int id) {
        Client client = new Client(serverAddress, port, id);
        client.start();
        try {
            client.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void startClients() {
        for (PeerInfo pi : peers) {
            startClient(pi.pAddress, Integer.parseInt(pi.pPort), Integer.parseInt(pi.pID));
        }
    }

    private void startBoth(int port) {
        startServer(port);
        startClients();
    }

    public static void main(String[] args) throws IOException {
        peerProcess pp = new peerProcess(args[0]);
    }
}
