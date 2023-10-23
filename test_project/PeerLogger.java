package test_project;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PeerLogger {
    
    public static class MyFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            return 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, uuuu HH:mm:ss a"))
                +  ": " + record.getMessage()+ "\n";
        }
    }

    public static void ClearLogs() {
        // Clear file contents of each log
        // or
        // Delete all log files
    }

    public static void InitLog(String pid) {
        String logFileName = "log_peer_" + pid + ".log";
        Path currentRelPath = Paths.get(".");
        String path = currentRelPath.toAbsolutePath().toString();

        try {
            File newLogFile = new File(logFileName);
            if(newLogFile.createNewFile()) {
                System.out.println("Log File Created: " + newLogFile.getName());
            }
            else {  
                System.out.println("File Already Exists");
            }
        }
        catch (IOException e) {
            System.out.println("ERROR OCCURED MAKING LOG FILE: " + logFileName);
            e.printStackTrace();
        }

        Logger logger = Logger.getLogger("log_peer_" + pid);
        logger.setLevel(Level.INFO);
        LogManager.getLogManager().addLogger(logger);
        
        try {
            Handler fileHandler = new FileHandler(path + "/" + logFileName);
            fileHandler.setFormatter(new MyFormatter());
            logger.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void TCPSendMessage(String pid1, String pid2){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s makes a connection to Peer %s", pid1, pid2));
    }

    public static void TCPReceiveMessage(String pid1, String pid2){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s is connected from Peer %s", pid1, pid2));
    }

    public static void PrefNeighborMessage(String pid1, Vector<PeerInfo> preferredNeighbors){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);

        String neighbors = "";
        Iterator<PeerInfo> it = preferredNeighbors.iterator();
        if (it.hasNext())
            neighbors += it.next().pID;
        while (it.hasNext())
            neighbors += ", " + it.next().pID;

        logger.log(Level.INFO, String.format("Peer %s has the preferred neighbors %s", pid1, neighbors));
    }

    public static void OptUnchokeNeighborMessage(String pid1, String pid2){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s has the optimistically unchoked neighbor %s", pid1, pid2));
    }

    public static void UnchokeMessage(String pid1, String pid2){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s is unchoked by %s", pid1, pid2));
    }

    public static void ChokeMessage(String pid1, String pid2){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s is choked by %s", pid1, pid2));
    }

    public static void ReceiveHaveMessage(String pid1, String pid2, int pieceIndex){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s received the 'have' message from %s for the piece %d", pid1, pid2, pieceIndex));
    }

    public static void ReceiveInterestedMessage(String pid1, String pid2){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s received the 'interested' message from %s", pid1, pid2));
    }

    public static void ReceiveNotInterestedMessage(String pid1, String pid2){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s received the 'not interested' message from %s", pid1, pid2));
    }

    public static void DownloadPieceMessage(String pid1, String pid2, int pieceIndex, int pieceCount){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s has downloaded the piece %d from %s. Now the number" +
                                             " of pieces it has is %d", pid1, pieceIndex, pid2, pieceCount));
    }

    public static void CompletionOfDownloadMessage(String pid1){
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);
        logger.log(Level.INFO, String.format("Peer %s has downloaded the complete file.", pid1));
    }
}
