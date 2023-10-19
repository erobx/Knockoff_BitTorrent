package test_project;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server extends Thread {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();

                new ServerThread(clientSocket).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class ServerThread extends Thread {
        private final Socket clientSocket;

        public ServerThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter writer = new PrintWriter(outputStream, true);

                while (true) {
                    String message = reader.readLine();
                    if (message == null) {
                        break;
                    }
                    System.out.println("Client: " + message);

                    System.out.println("Server: ");
                    String response = new Scanner(System.in).nextLine();
                    writer.println(response);
                }
                clientSocket.close();
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
