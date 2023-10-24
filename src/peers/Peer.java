package peers;

import java.util.*;
import java.net.*;
import java.io.*;

import util.Bitfield;
import util.ClientHandler;
import util.Server;
import messages.Handshake;
import messages.Handshake.Result;
import java.util.HashMap;

public class Peer {
    // Variables from config
    private int numPrefNeighbors;
    private int unchokingInterval;
    private int opUnchokeInterval;
    public final int peerID;
    private String fileName;
    private int fileSize;
    private int pieceSize;
    public static int numPieces;
    public static int lastPieceSize;
    private boolean hasFile;
    private boolean validPeerID;

    public static Bitfield bitfield;
    public static int unfinishedPeers;
    private Vector<Neighbor> peers = new Vector<>();
    private HashMap<Integer, ClientHandler> clients = new HashMap<Integer, ClientHandler>();
    private int numPeers;
    private int[] prefPeers;
    private int optUnchokedPeer;
    private String hostname;
    private int port;

    private Server server;

    public Peer(int peerID) {
        this.peerID = peerID;
    }

    /*
     * Method called by peerProcess to control main loop.
     */
    public void run() throws IOException {
        // Read config files
        readConfig();

        // Init bitfield
        bitfield = new Bitfield(numPieces, hasFile);
        unfinishedPeers = numPeers;

        // Create server
        server = new Server(port);
        // server.setDaemon(true);
        server.start();

        // Establish TCP connections with all peers before
        createClients();

        // Main loop
        while (unfinishedPeers != 0) {

            // check if there's a message for me

            // check if enough time has passed for preferedNeighbors

            // check if enough time has passed for optimistically unchoked

            // if there is a message do the thing

        }

        System.out.println("End of simulation.");

        // Close connections

    }

    private void readConfig() {
        readCommon();
        readPeerInfo();
    }

    private void readCommon() {
        /*
         * NumberOfPreferredNeighbors 3
         * UnchokingInterval 5
         * OptimisticUnchokingInterval 10
         * FileName thefile
         * FileSize 2167705
         * PieceSize 16384
         */
        try {
            String line;
            BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                String key = tokens[0];
                String value = tokens[1];

                switch (key) {
                    case "NumberOfPreferredNeighbors":
                        numPrefNeighbors = Integer.parseInt(value);
                        break;
                    case "UnchokingInterval":
                        unchokingInterval = Integer.parseInt(value);
                        break;
                    case "OptimisticUnchokingInterval":
                        opUnchokeInterval = Integer.parseInt(value);
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
        numPieces = (int) Math.ceil((double) fileSize / pieceSize);
        lastPieceSize = fileSize - ((numPieces - 1) * pieceSize);
        prefPeers = new int[numPrefNeighbors];
    }

    private void readPeerInfo() {
        /*
         * 1001 lin114-00.cise.ufl.edu 6008 1
         * 1002 lin114-01.cise.ufl.edu 6008 0
         * 1003 lin114-02.cise.ufl.edu 6008 0
         * 1004 lin114-03.cise.ufl.edu 6008 0
         * 1005 lin114-04.cise.ufl.edu 6008 0
         * 1006 lin114-05.cise.ufl.edu 6008 1
         */

        try {
            String line;
            BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
            numPeers = (int) in.lines().count();
            in.close();

            in = new BufferedReader(new FileReader("PeerInfo.cfg"));

            while ((line = in.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                int peerID = Integer.parseInt(tokens[0]);
                String pAddress = tokens[1];
                int temp_port = Integer.parseInt(tokens[2]);
                boolean hasFile = tokens[3].equals("1");

                // At this peer process
                if (peerID == this.peerID) {
                    this.port = Integer.parseInt(tokens[2]);
                    this.hasFile = tokens[3].equals("1");
                    this.validPeerID = true;
                    break;
                }
                peers.addElement(new Neighbor(peerID, pAddress, temp_port, hasFile, numPieces));
            }
            in.close();

            if (!validPeerID) {
                System.out.println("Invalid peer ID.");
                System.exit(1);
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    // Method to connect to peers before
    private void createClients() {
        for (Neighbor neighbor : peers) {
            Socket clientSocket;
            try {
                clientSocket = new Socket(neighbor.hostname, neighbor.port);

                // Send handshake
                try {
                    // PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    Handshake.clientHandshake(clientSocket.getInputStream(), clientSocket.getOutputStream(), peerID);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Handle clients
                ClientHandler ch = new ClientHandler(clientSocket, neighbor.peerID);
                // Add ch to list of clientsockets?
                clients.put(neighbor.peerID, ch);

                // ch.setDaemon(true);
                ch.start();
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
