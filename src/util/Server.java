package util;

import java.io.*;
import java.net.*;
import messages.Message;
import messages.Message.MessageType;
import peers.Neighbor;
import peers.Peer;
import messages.Handshake;

public class Server extends Thread {
    private final int port;
    private ServerSocket listener;
    private int peerID;
    private Peer peer;

    public Server(int port, int peerID, Peer peer) {
        this.port = port;
        this.peerID = peerID;
        this.peer = peer;
    }

    @Override
    public void run() {
        try {
            listener = new ServerSocket(port);
            listener.setReuseAddress(true);
            // System.out.println("[Server] Waiting for connection on port " + port);
            while (true) {
                Socket clientSocket = listener.accept();

                // Wait for handshake
                int senderID = -1;
                try {
                    // send through ServerSocket output
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String msgString = in.readLine();
                    Handshake handshake = Handshake.deserialize(msgString.getBytes());
                    senderID = handshake.getSenderID();
                    System.out.println("Sender ID: " + senderID);
                    PeerLogger.TCPReceiveMessage(peerID, senderID);
                    
                    // Handshake.sendHandshake(, senderID, senderID);
                    // senderID = Handshake.serverHandshake(clientSocket.getInputStream(), clientSocket.getOutputStream(),
                    //         peerID);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                if (senderID == -1) {
                    PeerLogger.Error(peerID, "ERROR RECEIVING HANDSHAKE");
                }

                // Create new ClientHandler
                ClientHandler ch = new ClientHandler(clientSocket, senderID, peer);
                Peer.clients.put(senderID, ch);
                System.out.println("PUTTING CLIENT IN CLIENTS: " + senderID);
                System.out.println("Clients: " + Peer.clients.size());

                Handshake reply = new Handshake("P2PFILESHARINGPROJ", peerID);
                reply.serialize(clientSocket.getOutputStream());
                PeerLogger.TCPSendMessage(peerID, senderID);
                System.out.println("SENT HANDSHAKE: " + peerID + " -> " + senderID);

                ch.setDaemon(true);
                ch.start();

                if (!Peer.bitfield.isEmpty()) { // if the bitfield is non-empty send bitfield msg
                    Message.sendMessage(MessageType.BITFIELD, this.peerID, senderID,
                            Peer.bitfield.getBitfield());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                listener.close();
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
