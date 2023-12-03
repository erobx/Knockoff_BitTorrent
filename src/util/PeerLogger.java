package util;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringJoiner;
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

import peers.Neighbor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PeerLogger {

    public static class MyFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, uuuu HH:mm:ss a"))
                    + ": " + record.getMessage() + "\n";
        }
    }

    static String logsDirectory = ".";

    public static void ClearAllLogs() {
        try {
            File directory = new File(logsDirectory);
            File[] logFiles = directory.listFiles((dir, name) -> name.startsWith("log_peer_") && name.endsWith(".log"));

            if (logFiles != null) {
                for (File logFile : logFiles) {
                    if (logFile.delete()) {
                        System.out.println("Deleted log file: " + logFile.getName());
                    } else {
                        System.out.println("Failed to delete log file: " + logFile.getName());
                    }
                }
            } else {
                System.out.println("No log files found in the directory: " + logsDirectory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ClearLog(int pid) {
        try {
            File directory = new File(logsDirectory);
            File[] logFiles = directory.listFiles((dir, name) -> name.equals("log_peer_" + pid + ".log"));

            if (logFiles != null && logFiles.length > 0) {
                File logFile = logFiles[0]; // Assuming there is only one log file per peer

                if (logFile.delete()) {
                    System.out.println("Deleted log file: " + logFile.getName());
                } else {
                    System.out.println("Failed to delete log file: " + logFile.getName());
                }
            } else {
                System.out.println("No log file found for peer " + pid + " in the directory: " + logsDirectory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void InitLog(int pid) {
        String logFileName = "log_peer_" + pid + ".log";
        Path currentRelPath = Paths.get(logsDirectory);
        String path = currentRelPath.toAbsolutePath().toString();

        try {
            File newLogFile = new File(logFileName);
            if (newLogFile.createNewFile()) {
                System.out.println("Log File Created: " + newLogFile.getName());
            } else {
                System.out.println("File Already Exists");
            }
        } catch (IOException e) {
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

    public static void TCPSendMessage(int pSend, int pRecieve) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pSend);
        logger.log(Level.INFO, String.format("Peer %s makes a connection to Peer %s", pSend, pRecieve));
    }

    public static void TCPReceiveMessage(int pSend, int pRecieve) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pSend);
        logger.log(Level.INFO, String.format("Peer %s is connected from Peer %s", pSend, pRecieve));
    }

    public static void bitfieldMessage(int pRecieve, int pSend) { // TODO remove
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pRecieve);
        logger.log(Level.INFO, String.format("Peer %s has the optimistically unchoked neighbor %s", pRecieve, pSend));
    }

    public static void PrefNeighborMessage(int pid1, Vector<Integer> preferredNeighbors) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid1);

        StringJoiner neighborsJoiner = new StringJoiner(", ");
        preferredNeighbors.forEach(neighbor -> neighborsJoiner.add(String.valueOf(neighbor)));
        String neighbors = neighborsJoiner.toString();

        logger.log(Level.INFO, String.format("Peer %s has the preferred neighbors %s", pid1, neighbors));
    }

    public static void OptUnchokeNeighborMessage(int pid, int pidNeighbor) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid);
        logger.log(Level.INFO, String.format("Peer %s has the optimistically unchoked neighbor %s", pid, pidNeighbor));
    }

    public static void UnchokeMessage(int pid, int pidNeighbor) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid);
        logger.log(Level.INFO, String.format("Peer %s is unchoked by %s", pid, pidNeighbor));
    }

    public static void ChokeMessage(int pid, int pidNeighbor) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid);
        logger.log(Level.INFO, String.format("Peer %s is choked by %s", pid, pidNeighbor));
    }

    public static void ReceiveHaveMessage(int pidReceived, int pidSend, int pieceIndex) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pidReceived);
        logger.log(Level.INFO,
                String.format("Peer %s received the 'have' message from %s for the piece %d", pidReceived, pidSend,
                        pieceIndex));
    }

    public static void ReceiveInterestedMessage(int pidReceived, int pidSend) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pidReceived);
        logger.log(Level.INFO,
                String.format("Peer %s received the 'interested' message from %s", pidReceived, pidSend));
    }

    public static void ReceiveNotInterestedMessage(int pidReceived, int pidSend) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pidReceived);
        logger.log(Level.INFO,
                String.format("Peer %s received the 'not interested' message from %s", pidReceived, pidSend));
    }

    public static void DownloadPieceMessage(int pRecieve, int pSend, int pieceIndex, int pieceCount) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pRecieve);
        logger.log(Level.INFO, String.format("Peer %s has downloaded the piece %d from %s. Now the number" +
                " of pieces it has is %d", pRecieve, pieceIndex, pSend, pieceCount));
    }

    public static void CompletionOfDownloadMessage(int pid) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid);
        logger.log(Level.INFO, String.format("Peer %s has downloaded the complete file.", pid));
    }

    public static void Error(int pid, String error) {
        Logger logger = LogManager.getLogManager().getLogger("log_peer_" + pid);
        logger.log(Level.INFO, String.format("%s", error));
    }
}
