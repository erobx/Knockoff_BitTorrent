import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class StartRemotePeers {

    private static final String scriptPrefix = "src\\java peerProcess ";

    public static class PeerInfo {

        private String peerID;
        private String hostName;
        private String port;
        private boolean hasFile;

        public PeerInfo(String peerID, String hostName, String port, boolean hasFile) {
            super();
            this.peerID = peerID;
            this.hostName = hostName;
            this.port = port;
            this.hasFile = hasFile;
        }

        public String getPeerID() {
            return peerID;
        }

        public void setPeerID(String peerID) {
            this.peerID = peerID;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public boolean getHasFile() {
            return hasFile;
        }

        public void setHasFile(boolean hasFile) {
            this.hasFile = hasFile;
        }

    }

    public static void main(String[] args) {
        ArrayList<PeerInfo> peerList = parseConfig();
        String ciseUser = "vboudreau1"; // change with your CISE username

        for (PeerInfo remotePeer : peerList) {
            Session session = null;
            try {
                JSch jsch = new JSch();
                jsch.addIdentity("private2.ppk", "wutnob-3guhro-Copgoz"); // Specify the correct private key file path
                session = jsch.getSession(ciseUser, remotePeer.getHostName(), 22);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                session.connect();

                System.out.println("Session to peer# " + remotePeer.getPeerID() + " at " + remotePeer.getHostName());

                Channel channel = session.openChannel("exec");
                System.out.println("remotePeerID" + remotePeer.getPeerID());
                ((ChannelExec) channel).setCommand(scriptPrefix + remotePeer.getPeerID());

                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream input = channel.getInputStream();
                channel.connect();

                System.out.println("Channel Connected to peer# " + remotePeer.getPeerID() + " at "
                        + remotePeer.getHostName() + " server with commands");

                (new Thread() {
                    @Override
                    public void run() {
                        try {
                            InputStreamReader inputReader = new InputStreamReader(input);
                            BufferedReader bufferedReader = new BufferedReader(inputReader);
                            String line = null;

                            while ((line = bufferedReader.readLine()) != null) {
                                // start peer process TODO
                                // peerProcess peer = new peerProcess(remotePeer.getPeerID());
                                System.out.println(remotePeer.getPeerID() + ">:" + line);
                            }

                            bufferedReader.close();
                            inputReader.close();
                        } catch (Exception ex) {
                            System.out.println(remotePeer.getPeerID() + " Exception >:");
                            ex.printStackTrace();
                        } finally {
                            channel.disconnect();
                        }
                    }
                }).start();

            } catch (JSchException e) {
                System.out.println(remotePeer.getPeerID() + " JSchException >:");
                e.printStackTrace();
            } catch (IOException ex) {
                System.out.println(remotePeer.getPeerID() + " IOException >:");
                ex.printStackTrace();
            } finally {
                session.disconnect();
            }
        }
    }

    private static ArrayList<PeerInfo> parseConfig() {
        ArrayList<PeerInfo> peerList = new ArrayList<>();

        try (BufferedReader in = new BufferedReader(new FileReader("PeerInfo2.cfg"))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                String peerId = tokens[0];
                String hostName = tokens[1];
                String port = tokens[2];
                boolean hasFile = tokens[3].equals("1");
                peerList.add(new PeerInfo(peerId, hostName, port, hasFile));
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return peerList;
    }
}
