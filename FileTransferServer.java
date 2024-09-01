import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTransferServer {
    private static final int SERVER_PORT = 8888;
    private static final String BASE_DIRECTORY = "C:\\Users\\Ayusman Nayak\\Pictures\\Screenshots"; 

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is running on port " + SERVER_PORT + ".");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            String fileName = (String) inFromClient.readObject();
            Path filePath = Paths.get(BASE_DIRECTORY, fileName);

            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                long fileSize = Files.size(filePath);
                outToClient.writeLong(fileSize);
                outToClient.flush();

                try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outToClient.write(buffer, 0, bytesRead);
                        outToClient.flush();
                    }
                }

                System.out.println("File sent successfully to: " + clientSocket.getInetAddress());
            } else {
                System.out.println("File not found or is not a regular file.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace() ; 
        }
    }
}