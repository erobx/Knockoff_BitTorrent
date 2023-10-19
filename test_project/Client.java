package test_project;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread {
    private final String serverAddress;
    private final int port;

    public Client(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(serverAddress, port);

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter writer = new PrintWriter(outputStream, true);

            while (true) {
                System.out.println("Client: ");
                String message = new Scanner(System.in).nextLine();
                writer.print(message);

                String response = reader.readLine();
                if (response == null) {
                    break;
                }
                System.out.println("Server: " + response);
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
