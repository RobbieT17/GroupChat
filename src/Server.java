import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    
    private String serverName;
    private final ServerSocket serverSocket;

    public Server(ServerSocket socket) {
        this.serverSocket = socket;
    }

    // Starts the server on the specified port
    public void start() {
        System.out.println("Server started.");
        try {
            while (!this.serverSocket.isClosed()) { // While server is still running
                Socket socket = this.serverSocket.accept(); // Blocks until a client makes a connection request
                System.out.println("New client connected.");

                // Runs accepted client on a separate thread
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();

                clientHandler.writeToBuffer(String.format("Connected to [%s]", this.serverName));
                clientHandler.writeToBuffer("Type \\c to display list of commands.");

            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Closes the server socket
    public void close() {
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6575);
        Server server = new Server(serverSocket);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Group chat name: ");

        // Sets group chat name and starts server
        server.serverName = scanner.nextLine();
        server.start();
    }
}
