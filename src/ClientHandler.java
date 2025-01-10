import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;

public class ClientHandler implements Runnable {

    // A list of all connected clients
    private static final LinkedList<ClientHandler> clientHandlers = new LinkedList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.clientUsername = bufferedReader.readLine();

            clientHandlers.add(this);

            // Sends out notification of new client arrival
            this.broadcastMessage(String.format("[SERVER] %s has entered the chat.", this.clientUsername));
        } catch (IOException e) {
            this.closeAll();
        }
    }

    // Writes message to buffer
    public void writeToBuffer(String message) {
        try {
            this.bufferedWriter.write(message);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (IOException e) {
            this.closeAll();
        }
    }
 
    // Sends a message to all users (except sender)
    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUsername.equals(this.clientUsername)) {
                continue;
            }
            clientHandler.writeToBuffer(message);
        }
    }

    // Lists all commands
    public void displayCommands() {
        this.writeToBuffer("Command List");
        this.writeToBuffer("\\c -- List all commands");
        this.writeToBuffer("\\l -- List all chat members");
        this.writeToBuffer("\\p <username> <message> -- Sends private message to a user");
    }

    // Lists all group chat members.
    public void listChatMembers() {
        for (ClientHandler clientHandler : clientHandlers) {
            this.writeToBuffer(
                clientHandler.clientUsername.equals(this.clientUsername) 
                ? String.format("%s (You)", clientHandler.clientUsername)
                : clientHandler.clientUsername
            );
        }
    }

    // Sends a private message to a client
    public void sendPrivateMessage(String command) {
        // Splits command into it's operands
        String parts[] = command.split(" ", 3);

        // Validates command: must have three parameters
        if (parts.length != 3) {
            this.writeToBuffer("Usage: <\\p> <username> <message>");
            return;
        }

        String username = parts[1];
        String message = parts[2];

        // Validates username: cannot be self
        if (this.clientUsername.equals(username)) {
            this.writeToBuffer("Cannot write message to yourself."); 
            return;
        }

        // Loops through all clients until it finds matching usernames
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUsername.equals(username)) {
                clientHandler.writeToBuffer(String.format("{%s}: %s", this.clientUsername, message)); 
                return;
            }
        }

        // Username does not match any of the clients
        this.writeToBuffer("Username does not exist.");
    }

    // Closes socket connection and buffer reader/writer. Removes client from handler list
    public void closeAll() {
        this.removeClientHandler(); 

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

    // Removes connection between client and server
    public void removeClientHandler() {
        clientHandlers.remove(this);
        this.broadcastMessage(String.format("[SERVER] %s has left the chat.", this.clientUsername));
    }


    @Override
    // Clients inputs a message to send to all other users
    public void run() {
        while (this.socket.isConnected()) {
            try {
                String message = this.bufferedReader.readLine();
                
                // Using a backslash, you can do different commands
                if (message.charAt(0) == '\\') {
                    switch (message.charAt(1)) {
                        case 'c' -> this.displayCommands();
                        case 'l' -> this.listChatMembers();
                        case 'p' ->  this.sendPrivateMessage(message);
                        default ->  this.writeToBuffer("Unknown command.");   
                    }
                    continue;
                }

                // Default action: Send message to all members of group chat
                broadcastMessage(String.format("(%s): %s", this.clientUsername, message));
            } catch (IOException e) {
                closeAll();
                break;
            }
        }
    }
    
}
