import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

class RemotePeerInfo {
    public String peerId;
    public String peerAddress;
    public String peerPort;

    public RemotePeerInfo(String pId, String pAddress, String pPort) {
        peerId = pId;
        peerAddress = pAddress;
        peerPort = pPort;
    }
}

class StartLocalPeers {

    public Vector<RemotePeerInfo> peerInfoVector;

    public void getConfiguration() {
        String st;
        peerInfoVector = new Vector<RemotePeerInfo>();
        try {
            BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
            while ((st = in.readLine()) != null) {

                String[] tokens = st.split("\\s+");
                // System.out.println("tokens begin ----");
                // for (int x=0; x<tokens.length; x++) {
                // System.out.println(tokens[x]);
                // }
                // System.out.println("tokens end ----");

                peerInfoVector.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2]));

            }

            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            StartLocalPeers myStart = new StartLocalPeers();
            myStart.getConfiguration();

            // get current path
            String path = System.getProperty("user.dir");

            // start clients at remote hosts
            for (int i = 0; i < myStart.peerInfoVector.size(); i++) {

                RemotePeerInfo pInfo = (RemotePeerInfo) myStart.peerInfoVector.elementAt(i);
                Runtime.getRuntime().exec("java peerProcess " + pInfo.peerId);
                // Thread thread = new Thread(() -> {
                //     try {
                //         Process process = Runtime.getRuntime().exec("java peerProcess " + pInfo.peerId);

                //         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                //         BufferedReader readerERR = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                //         String line;
                //         String errorLine = null;

                //         while ((line = reader.readLine()) != null || (errorLine = readerERR.readLine()) != null) {
                //             if (line != null) {
                //                 System.out.println("Peer " + pInfo.peerId + ": " + line);
                //             }
                //             if (errorLine != null) {
                //                 System.out.println(
                //                         "Peer Err " + pInfo.peerId + ": " + errorLine);
                //             }
                //         }

                //         int exitCode = process.waitFor();

                //         if (exitCode == 0) {
                //             System.out.println("Peer " + pInfo.peerId + " has terminated successfully.");
                //         } else {
                //             System.out.println("Peer " + pInfo.peerId + " has encountered an error.");
                //         }
                //     } catch (IOException | InterruptedException e) {
                //         e.printStackTrace();
                //     }
                // });
                // thread.start();
            }

            System.out.println("Starting all remote peers has been attempted.");

        } catch (

        Exception ex) {
            System.out.println(ex);
        }
    }

}