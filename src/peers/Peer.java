package peers;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.net.*;
import java.io.*;

import util.Bitfield;
import util.ClientHandler;
import util.PeerLogger;
import util.Server;
import messages.Handshake;
import messages.Message;
import messages.MsgBitfield;
import messages.MsgChoke;
import messages.MsgHave;
import messages.MsgInt;
import messages.MsgNotInt;
import messages.MsgPiece;
import messages.MsgRequest;
import messages.MsgUnchoke;
import messages.Handshake.Result;
import messages.Message.MessageType;

public class Peer {
    // Variables from config
    private int numPrefNeighbors;
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
    public static HashMap<Integer, Neighbor> peers = new HashMap<Integer, Neighbor>();
    public static HashMap<Integer, ClientHandler> clients = new HashMap<Integer, ClientHandler>();
    private int numPeers;
    private Vector<Integer> prefPeers;
    private int optUnchokedPeer;
    private String hostname;
    private int port;

    private Server server;

    // Timing Variables
    private long lastPreferredUpdateTime;
    private long lastOpUnchokeUpdateTime;
    private int updatePrefInterval;
    private int opUnchokeInterval;
    private long lastTimeoutCheck;
    private int unchokingInterval;

    private BlockingQueue<MessageObj> messageQueue = new LinkedBlockingQueue<MessageObj>();

    public Peer(int peerID) {
        this.peerID = peerID;
    }

    /*
     * Method called by peerProcess to control main loop.
     */

    // Temporary Stuff for testing
    private void timeout() {

        long TimeoutValue = 30; // 30 seconds

        System.out.println("Time: " + (System.currentTimeMillis() - lastTimeoutCheck) / 1000);

        if ((System.currentTimeMillis() - lastTimeoutCheck) / 1000 >= TimeoutValue) {
            unfinishedPeers = 0;
        }
    }

    public void run() throws Exception {
        // Read config files
        readConfig();

        PeerLogger.ClearAllLogs();
        PeerLogger.InitLog(peerID);

        // Init bitfield
        bitfield = new Bitfield(numPieces, hasFile);

        // Create server
        server = new Server(port, peerID, this);
        server.setDaemon(true);
        server.start();

        // Establish TCP connections with all peers before
        createClients();

        unfinishedPeers = numPeers;
        lastTimeoutCheck = System.currentTimeMillis();
        lastPreferredUpdateTime = System.currentTimeMillis();
        lastOpUnchokeUpdateTime = System.currentTimeMillis();
        // Main loop
        while (unfinishedPeers != 0) {

            // keep track of total run time
            // (can be removed once termination condition is created)
            timeout();

            // check if there's a message for me
            MessageObj messageObj = null;
            try {
                // try to get a message from buffer for 5 seconds
                messageObj = messageQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println("Failed to get message");
                throw new RuntimeException(e);
            }

            // check if enough time has passed for preferedNeighbors
            if ((System.currentTimeMillis() - lastPreferredUpdateTime) / 1000 >= updatePrefInterval) {
                updatePreferred();
                lastPreferredUpdateTime = System.currentTimeMillis();
            }

            // check if enough time has passed for optimistically unchoked
            if ((System.currentTimeMillis() - lastOpUnchokeUpdateTime) / 1000 >= opUnchokeInterval) {
                optimisticUnchoke();
                lastOpUnchokeUpdateTime = System.currentTimeMillis();
            }

            // if there is a message do the thing
            if (messageObj != null) {

                byte[] messageBytes = messageObj.message;
                int senderID = messageObj.senderID;

                try {
                    Message m = Message.getMessage(messageBytes, senderID, peerID);
                    m.handle(); // will handle based on what message it i
                } catch (Exception e) {
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    if (stackTrace.length > 0) {
                        StackTraceElement topFrame = stackTrace[0];
                        String className = topFrame.getClassName();
                        String methodName = topFrame.getMethodName();
                        String fileName = topFrame.getFileName();
                        int lineNumber = topFrame.getLineNumber();

                        System.out.println("Exception in " + className + "." + methodName +
                                " (" + fileName + ":" + lineNumber + "): " + e);
                    } else {
                        // Handle the case where the stack trace is empty
                        System.out.println("Exception: " + e);
                    }
                }
            }
        }

        closePeers();
    }

    private void closePeers() {
        System.out.println("Closing");
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
        prefPeers = new Vector<>(numPrefNeighbors);
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
                } else {
                    peers.put(peerID, new Neighbor(peerID, pAddress, temp_port, hasFile, numPieces));
                }
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
        for (Map.Entry<Integer, Neighbor> entry : peers.entrySet()) {
            Socket clientSocket;
            try {
                Neighbor neighbor = entry.getValue();
                clientSocket = new Socket(neighbor.hostname, neighbor.port);

                // Send handshake
                try {
                    Handshake.clientHandshake(clientSocket.getInputStream(), clientSocket.getOutputStream(), peerID,
                            neighbor.peerID);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Handle clients
                ClientHandler ch = new ClientHandler(clientSocket, neighbor.peerID, this);

                clients.put(neighbor.peerID, ch);

                ch.setDaemon(true);
                ch.start();

            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updatePreferred() throws IOException {
        // Determine preferred peers to unchoke based on download progress
        if (!hasFile) {
            // Create a priority queue to store interested peers
            PriorityQueue<Neighbor> interestedPeersQueue = new PriorityQueue<>();

            // Iterate over all peers to identify interested ones
            for (Neighbor peer : peers.values()) {
                if (peer != null) {
                    if (peer.peerInterested) {
                        interestedPeersQueue.add(peer);
                    }
                    // Reset dataRate for each peer
                    peer.dataRate = 0;
                }
            }

            // Unchoke the top number of prefered interested peers
            for (int i = 0; i < numPrefNeighbors; i++) {
                Neighbor preferredPeer = interestedPeersQueue.poll();
                if (preferredPeer != null) {
                    prefPeers.set(i, preferredPeer.peerID);
                    unchoke(preferredPeer.peerID);
                } else {
                    System.err.println("Error! Trying to add an unconnected peer to preferred peers");
                }
            }

            PeerLogger.PrefNeighborMessage(peerID, prefPeers);

            // Choke all remaining interested peers
            while (interestedPeersQueue.peek() != null) {
                Neighbor chokedPeer = interestedPeersQueue.poll();
                if (chokedPeer != null) {
                    choke(chokedPeer.peerID);
                } else {
                    System.err.println("Error! Trying to choke an unconnected peer");
                }
            }

        } else {
            // The peer has downloaded the whole file
            Random rand = new Random();
            Integer[] peerIDs = new Integer[peers.size()];
            peerIDs = peers.keySet().toArray(peerIDs);
            knuthShuffle(peerIDs);

            int nPrefDex = 0;

            // Iterate over all peers to unchoke preferred ones randomly
            for (int i = 0; i < peerIDs.length; i++) {
                Neighbor peer = peers.get(peerIDs[i]);
                if (peer != null) {
                    if (peer.peerInterested && nPrefDex < prefPeers.size()) {
                        prefPeers.set(nPrefDex++, peer.peerID);
                        unchoke(peer.peerID);
                        break;
                    } else {
                        // Choke all remaining peers
                        choke(peer.peerID);
                    }
                    // Reset dataRate for each peer
                    peer.dataRate = 0;
                } else {
                    System.err.println("Error! Unconnected peer");
                }
            }
        }

    }

    public static void knuthShuffle(Integer[] array) {
        Random rand = new Random();

        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);

            // Swap array[i] and array[j]
            Integer temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    public void choke(int receiverID) throws IOException {
        /*
         * Chokes the specified PeerID:
         * - Updates choked status of the appropriate NeighborPeer
         * - Sends a choked message to the appropriate peer
         */

        // Retrieve the NeighborPeer corresponding to the specified receiverID
        Neighbor toChoke = peers.get(receiverID);

        // If not yet connected to this peer, return
        if (!toChoke.connected) {
            return;
        }

        // Update the choked status of the NeighborPeer
        toChoke.choking = true;

        // Create and send a Choke message
        Message.sendMessage(MessageType.CHOKE, receiverID, peerID, null);

        System.out.println("Choking " + receiverID);
    }

    public void unchoke(int receiverID) throws IOException {
        /*
         * Unchokes the specified PeerID:
         * - Updates the choked status of the appropriate NeighborPeer
         * - Sends an unchoke message to the appropriate peer
         */

        // Retrieve the NeighborPeer corresponding to the specified receiverID
        Neighbor toUnchoke = peers.get(receiverID);

        // Update the choked status of the NeighborPeer
        toUnchoke.choking = false;

        // Create and send an Unchoke message
        Message.sendMessage(MessageType.UNCHOKE, receiverID, peerID, null);
    }

    public void optimisticUnchoke() throws IOException {
        /*
         * Select a random peer from choked peers interested in your data;
         * unchoke them (send them an unchoke message, mark them as unchoked).
         * Choke the peer that was previously optimistically unchoked.
         */

        // Get a random order of peer IDs
        Random rand = new Random();
        Integer[] peerIDs = new Integer[peers.size()];
        peerIDs = peers.keySet().toArray(peerIDs);
        knuthShuffle(peerIDs);

        // Iterate over shuffled peer IDs to find a choked, interested peer
        for (int i = 0; i < peerIDs.length; i++) {
            Neighbor peer = peers.get(peerIDs[i]);
            if (peer.peerInterested && peer.choking) {

                // Unchoke the selected peer and log the change
                optUnchokedPeer = peerIDs[i];
                unchoke(optUnchokedPeer);

                PeerLogger.OptUnchokeNeighborMessage(peerID, optUnchokedPeer);

                // System.out.println("Optimistically unchoking peer: " + optUnchokedPeer);
                break;
            }
        }
    }

    public void addToMessageQueue(byte[] msg, int peerID) {
        messageQueue.add(new MessageObj(msg, peerID));
    }
}
