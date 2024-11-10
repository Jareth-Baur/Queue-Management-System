
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Queue;

public class QueueManagementServer extends WebSocketServer {

    private Queue<String> ticketQueue = new LinkedList<>();
    private int ticketNumber = 0;
    

    public QueueManagementServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from: " + conn.getRemoteSocketAddress());
        conn.send("Connected to Queue Management System.");
        sendQueueStatus(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress() + " - Reason: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);

        if (message.equalsIgnoreCase("newTicket")) {
            // Issue a new ticket
            issueNewTicket(conn);
        } else if (message.equalsIgnoreCase("nextTicket")) {
            // Serve the next ticket
            callNextTicket(conn);
        } else if (message.equalsIgnoreCase("queueStatus")) {
            // Return the current queue status
            sendQueueStatus(conn);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error occurred: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Queue Management WebSocket server started!");
    }

    // Method to issue a new ticket
    private void issueNewTicket(WebSocket conn) {
        ticketNumber++;
        String newTicket = "Ticket-" + ticketNumber;
        ticketQueue.offer(newTicket);
        System.out.println("New ticket issued: " + newTicket);
        conn.send("New ticket issued: " + newTicket);
        broadcastQueueStatus();
    }

    // Method to call the next ticket in the queue
    private void callNextTicket(WebSocket conn) {
        if (ticketQueue.isEmpty()) {
            conn.send("No tickets in the queue.");
        } else {
            String nextTicket = ticketQueue.poll();
            System.out.println("Calling ticket: " + nextTicket);
            conn.send("Serving: " + nextTicket);
            broadcastQueueStatus();
        }
    }

    // Method to broadcast the current queue status to all clients
    private void broadcastQueueStatus() {
        String queueStatus = "Queue Status: " + ticketQueue.toString();
        System.out.println(queueStatus);
        for (WebSocket client : getConnections()) {
            client.send(queueStatus);
        }
    }

    // Send queue status to a specific client
    private void sendQueueStatus(WebSocket conn) {
        String queueStatus = "Queue Status: " + ticketQueue.toString();
        conn.send(queueStatus);
    }

    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        QueueManagementServer server = new QueueManagementServer(address);
        server.start();
        System.out.println("Queue Management WebSocket server started on port 8080");
    }
}
