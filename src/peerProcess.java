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
