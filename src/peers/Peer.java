package peers;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.net.*;
import java.io.*;
import java.lang.ref.Cleaner.Cleanable;

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
    public static HashMap<Integer, Neighbor> peersBefore = new HashMap<Integer, Neighbor>();
    public static Set<Integer> finishedPeers = new HashSet<Integer>(); // might be gone
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

    class DowloadComparator implements Comparator<Neighbor>{
             
        // Overriding compare()method of Comparator 
                    // for descending order of cgpa
        public int compare(Neighbor n1, Neighbor n2) {
            if (n1.dataRate < n2.dataRate)
                return 1;
            else if (n1.dataRate > n2.dataRate)
                return -1;
                            return 0;
            }
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

    private void waitForConnections() {
        while (clients.size() < numPeers - 1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBitfields() {
        for (Map.Entry<Integer, Neighbor> entry : peers.entrySet()) {
            if (!Peer.bitfield.isEmpty()) { // if the bitfield is non-empty send bitfield msg
                try {
                    Message.sendMessage(MessageType.BITFIELD, this.peerID, entry.getValue().peerID,
                            Peer.bitfield.getBitfield());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void run() throws Exception {
        // Read config files
        readConfig();

        PeerLogger.ClearLog(peerID);
        PeerLogger.InitLog(peerID);

        // Init bitfield
        bitfield = new Bitfield(numPieces, hasFile);

        // Create server
        server = new Server(port, peerID, this);
        server.setDaemon(true);
        server.start();

        // Establish TCP connections with all peers before
        createClients();

        waitForConnections();

        sendBitfields();

        unfinishedPeers = numPeers; // might need to be numPeers - 1
        lastTimeoutCheck = System.currentTimeMillis();
        lastPreferredUpdateTime = System.currentTimeMillis();
        lastOpUnchokeUpdateTime = 0;
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
                // throw new RuntimeException(e);
            }

            // if there is a message do the thing
            if (messageObj != null) {

                byte[] messageBytes = messageObj.message;
                int senderID = messageObj.senderID;

                try {
                    Message m = Message.getMessage(messageBytes, senderID, peerID);
                    m.handle(); // will handle based on what message it is
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

            // check if enough time has passed for preferedNeighbors
            if ((System.currentTimeMillis() - lastPreferredUpdateTime) / 1000 >=
            updatePrefInterval) {
                updatePreferred();
                lastPreferredUpdateTime = System.currentTimeMillis();
            }

            // // check if enough time has passed for optimistically unchoked
            // if ((System.currentTimeMillis() - lastOpUnchokeUpdateTime) / 1000 >=
            // opUnchokeInterval*1000) {
            //     optimisticUnchoke();
            //     lastOpUnchokeUpdateTime = System.currentTimeMillis();
            // }
        }

        closePeers();
    }

    private void closePeers() {
        System.out.println("Closing");

        PeerLogger.CompletionOfDownloadMessage(this.peerID);
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

                Neighbor n = new Neighbor(peerID, pAddress, temp_port, hasFile, numPieces);
                // At this peer process
                if (peerID == this.peerID) {
                    this.port = Integer.parseInt(tokens[2]);
                    this.hasFile = tokens[3].equals("1");
                    this.validPeerID = true;
                } else {
                    peers.put(peerID, n);
                }

                if (peerID < this.peerID) {
                    peersBefore.put(peerID, n);
                }
            }
            in.close();

            if (!validPeerID) {
                System.out.println("Invalid peer ID.");
                System.exit(1);
            }
        } catch (Exception ex) {
            errorLogging(ex, peerID);
        }
    }

    // Method to connect to peers before
    private void createClients() {
        for (Map.Entry<Integer, Neighbor> entry : peersBefore.entrySet()) {
            Socket clientSocket;
            Neighbor neighbor;
            try {
                neighbor = entry.getValue();
                clientSocket = new Socket(neighbor.hostname, neighbor.port);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Handshake handshake = new Handshake("P2PFILESHARINGPROJ", peerID);
            try {
                // Sending Handshake
                handshake.serialize(clientSocket.getOutputStream());
                PeerLogger.TCPSendMessage(peerID, neighbor.peerID);
            } catch (IOException e) {
                PeerLogger.Error(neighbor.peerID, "IO Exception");
            }

            // Handle clients
            ClientHandler ch = new ClientHandler(clientSocket, entry.getValue().peerID, this);

            clients.put(entry.getValue().peerID, ch);

            try {
                // Receiving Handshake
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String msgString = in.readLine();
                Handshake reply = Handshake.deserialize(msgString.getBytes());
                int senderID = reply.getSenderID();
                PeerLogger.TCPReceiveMessage(peerID, neighbor.peerID);
                System.out.println("RECIEVED HANDSHAKE: " + senderID + " -> " + peerID);
            } catch (IOException ex) {
                PeerLogger.Error(neighbor.peerID, "IO Exception");
            }

            System.out.println("PUTTING CLIENT IN CLIENTS PEER: " + entry.getValue().peerID);
            System.out.println("Clients: " + clients.size());

            ch.setDaemon(true);
            ch.start();
        }
    }

    private void updatePreferred() throws IOException {
        // Determine preferred peers to unchoke based on download progress
        prefPeers.clear();
        if (!hasFile) {
            // Create a priority queue to store interested peers
            PriorityQueue<Neighbor> interestedPeersQueue = new PriorityQueue<Neighbor>(new DowloadComparator());

            // Iterate over all peers to identify interested ones
            if (!peers.isEmpty()) {
                for (Neighbor peer : peers.values()) {
                if (peer.peerInterested) {
                    interestedPeersQueue.add(peer);
                    System.out.println("ADDED INTERESTED PEER");
                }
                // Reset dataRate for each peer
                peer.dataRate = 0;
            }
                // Unchoke the top number of prefered interested peers
                for (int i = 0; i < numPrefNeighbors; i++) {
                    Neighbor preferredPeer = interestedPeersQueue.poll();
                    if (preferredPeer != null) {
                        prefPeers.add(preferredPeer.peerID);
                        if (!preferredPeer.isChoking()){
                            unchoke(preferredPeer.peerID);
                        }
                    } else {
                        System.err.println("Error! Trying to add an unconnected peer to preferred peers");
                    }
                }

                // Choke all remaining interested peers
                while (interestedPeersQueue.peek() != null) {
                    Neighbor chokedPeer = interestedPeersQueue.poll();
                    if (chokedPeer != null) {
                        choke(chokedPeer.peerID);
                    } else {
                        System.err.println("Error! Trying to choke an unconnected peer");
                    }
                }
            }

        } else {
            // The peer has downloaded the whole file so unchoke and choke randomly since
            // they don't receive pieces
            Random rand = new Random();
            Integer[] peerIDs = new Integer[peers.size()]; // make an array
            peerIDs = peers.keySet().toArray(peerIDs);
            knuthShuffle(peerIDs); // shuffle the array to get randomness

            int numCurrentPrefered = 0;

            // Iterate over all peers to unchoke preferred ones randomly
            for (int i = 0; i < peerIDs.length; i++) {
                Neighbor peer = peers.get(peerIDs[i]);
                if (peer != null) {
                    if (peer.peerInterested && numCurrentPrefered < numPrefNeighbors) {
                        prefPeers.add(peer.peerID);
                        unchoke(peer.peerID);
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
        PeerLogger.PrefNeighborMessage(this.peerID, prefPeers);
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

        // Update the choked status of the NeighborPeer
        toChoke.choking = true;

        // Create and send a Choke message
        Message.sendMessage(MessageType.CHOKE, peerID, receiverID, null);

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
        Message.sendMessage(MessageType.UNCHOKE, peerID, receiverID, null);
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

    public static void errorLogging(Exception e, int peerId) {
        String exceptionDetails;

        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement topFrame = stackTrace[0];
            String className = topFrame.getClassName();
            String methodName = topFrame.getMethodName();
            String fileName = topFrame.getFileName();
            int lineNumber = topFrame.getLineNumber();

            exceptionDetails = "Exception in " + className + "." + methodName +
                    " (" + fileName + ":" + lineNumber + "): " + e;
        } else {
            // Handle the case where the stack trace is empty
            exceptionDetails = "Exception: " + e;
        }

        PeerLogger.Error(peerId, exceptionDetails);
    }

    public void addToMessageQueue(byte[] msg, int peerID) {
        messageQueue.add(new MessageObj(msg, peerID));
    }
}
