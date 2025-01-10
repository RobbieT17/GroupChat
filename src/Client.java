import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
            closeAll();
        }  
    }

    // Sends a message to the group chat
    public void messageSender() {
        try {
            // First thing sent to the server is the username inputted
            this.bufferedWriter.write(this.username);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);

            // Loops until user disconnects
            while (this.socket.isConnected()) {
                String message = scanner.nextLine();

                // Sends message to all
                this.bufferedWriter.write(message);
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeAll();
        }
    }

    // Receives messages sent from other users
    public void messageListener() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String message = bufferedReader.readLine();
                    System.out.println(message);
                } catch (IOException e) {
                    closeAll();
                }
            }
        }).start();
    }

    public void closeAll() {
        try {
            if (this.bufferedReader != null) {
                this.bufferedReader.close();
            }

            if (this.bufferedWriter != null) {
                this.bufferedWriter.close();
            }

            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        Socket socket = new Socket("localhost", 6575);
        Client client = new Client(socket, username);

        client.messageListener();
        client.messageSender();
    }
}
