package dorsu.jareth.queue;

import dorsu.jareth.util.DatabaseConnection;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;

public class QueueManagementServer extends WebSocketServer {

    public QueueManagementServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from: " + conn.getRemoteSocketAddress());
        conn.send("Connected to Queue Management System.");
        sendQueueStatus(conn, 0);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress() + " - Reason: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client (" + conn.getRemoteSocketAddress() + "): " + message);
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            try {
                int officeId = Integer.parseInt(parts[0]);
                String command = parts[1].toLowerCase();

                switch (command) {
                    case "newticket":
                        issueNewTicket(conn, officeId);
                        break;
                    case "nextticket":
                        callNextTicket(conn, officeId);
                        break;
                    case "queuestatus":
                        sendQueueStatus(conn, officeId);
                        break;
                    default:
                        conn.send("Unknown command: " + message);
                }
            } catch (NumberFormatException e) {
                conn.send("Invalid office ID in message.");
            } catch (IOException e) {
                conn.send("Error processing request: " + e.getMessage());
            }
        } else {
            conn.send("Invalid message format.");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error occurred with client: "
                + (conn != null ? conn.getRemoteSocketAddress() : "unknown")
                + " - " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Queue Management WebSocket server started!");
    }

    private void generateTicketText(String ticketNumber, int officeId) throws IOException {
        Path ticketsDirectory = Paths.get("tickets");
        Files.createDirectories(ticketsDirectory);
        String filePath = ticketsDirectory.resolve("ticket_" + ticketNumber.substring(7) + ".txt").toString();

        String content = "Ticket Number: " + ticketNumber + "\n";
        content += "Status: PENDING\n";
        content += "Issued on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a")) + "\n";
        content += "Office ID: " + officeId + "\n";
        String officeName = fetchOfficeName(officeId);
        if (officeName != null) {
            content += "Office Name: " + officeName + "\n";
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            throw new IOException("Error generating text file: " + e.getMessage(), e);
        }
    }

    private String fetchOfficeName(int officeId) {
        String officeName = null;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT name FROM offices WHERE id = ?")) {
            statement.setInt(1, officeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    officeName = resultSet.getString("name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching office name: " + e.getMessage());
        }
        return officeName;
    }

    private void issueNewTicket(WebSocket conn, int officeId) throws IOException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement("INSERT INTO tickets (ticket_number, status, created_at, office_id) VALUES (?, 'PENDING', NOW(), ?)")) {
            int newTicketNumber = getLastTicketNumber(connection) + 1;
            String newTicket = "Ticket-" + newTicketNumber;
            stmt.setString(1, newTicket);
            stmt.setInt(2, officeId);
            stmt.executeUpdate();
            String officeName = getOfficeName(connection, officeId);
            conn.send("New ticket issued: " + newTicket + " (" + officeName + ")");
            broadcastQueueStatus(officeId);
            generateTicketText(newTicket, officeId);
            conn.send("Ticket generated: ticket_" + newTicketNumber + ".txt");
        } catch (SQLException e) {
            conn.send("Error issuing a new ticket: " + e.getMessage());
            System.err.println("Error issuing new ticket: " + e.getMessage());
        }
    }

    private void callNextTicket(WebSocket conn, int officeId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT t.id, t.ticket_number, o.name AS office_name FROM tickets t JOIN offices o ON t.office_id = o.id WHERE t.status = 'PENDING' AND t.office_id = ? ORDER BY t.id LIMIT 1")) {
            stmt.setInt(1, officeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String nextTicket = rs.getString("ticket_number");
                    String officeName = rs.getString("office_name");
                    conn.send("Serving: " + nextTicket + " (" + officeName + ")");

                    try (PreparedStatement updateStmt = connection.prepareStatement("UPDATE tickets SET status = 'SERVED' WHERE id = ?")) {
                        updateStmt.setInt(1, id);
                        updateStmt.executeUpdate();
                    }
                    broadcast("Serving: " + nextTicket + " (" + officeName + ")");
                    broadcastQueueStatus(officeId);
                } else {
                    conn.send("No tickets in the queue for this office.");
                }
            }
        } catch (SQLException e) {
            conn.send("Error calling the next ticket: " + e.getMessage());
            System.err.println("Error calling next ticket: " + e.getMessage());
        }
    }

    private String getOfficeName(Connection connection, int officeId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT name FROM offices WHERE id = ?")) {
            stmt.setInt(1, officeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return "Unknown Office";
    }

    private int getLastTicketNumber(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MAX(id) AS max_id FROM tickets")) {
            if (rs.next()) {
                return rs.getInt("max_id");
            }
        }
        return 0;
    }

    private void sendQueueStatus(WebSocket conn, int officeId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT t.ticket_number, o.name AS office_name FROM tickets t JOIN offices o ON t.office_id = o.id WHERE t.status = 'PENDING' AND t.office_id = ? ORDER BY t.id")) {
            stmt.setInt(1, officeId);
            try (ResultSet rs = stmt.executeQuery()) {
                StringBuilder queueStatusBuilder = new StringBuilder("Queue Status: ");
                boolean firstTicket = true;
                while (rs.next()) {
                    String ticketNumber = rs.getString("ticket_number");
                    String officeName = rs.getString("office_name");
                    if (!firstTicket) {
                        queueStatusBuilder.append(", ");
                    }
                    queueStatusBuilder.append(ticketNumber).append(" (").append(officeName).append(")");
                    firstTicket = false;
                }

                if (queueStatusBuilder.length() == "Queue Status: ".length()) {
                    queueStatusBuilder.append("The queue is empty for this office.");
                }
                conn.send(queueStatusBuilder.toString());
            }
        } catch (SQLException e) {
            conn.send("Error fetching queue status: " + e.getMessage());
            System.err.println("Error fetching queue status: " + e.getMessage());
        }
    }

    private void broadcastQueueStatus(int officeId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT t.ticket_number, o.name AS office_name FROM tickets t JOIN offices o ON t.office_id = o.id WHERE t.status = 'PENDING' AND t.office_id = ? ORDER BY t.id")) {
            stmt.setInt(1, officeId);
            try (ResultSet rs = stmt.executeQuery()) {
                StringBuilder queueStatusBuilder = new StringBuilder("Queue Status: ");
                boolean firstTicket = true;
                while (rs.next()) {
                    String ticketNumber = rs.getString("ticket_number");
                    String officeName = rs.getString("office_name");
                    if (!firstTicket) {
                        queueStatusBuilder.append(", ");
                    }
                    queueStatusBuilder.append(ticketNumber).append(" (").append(officeName).append(")");
                    firstTicket = false;
                }

                if (queueStatusBuilder.length() == "Queue Status: ".length()) {
                    queueStatusBuilder.append("The queue is empty for this office.");
                }
                broadcast(queueStatusBuilder.toString());
            }
        } catch (SQLException e) {
            System.err.println("Error broadcasting queue status: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String lanIp = getLanIpAddress(); // Get LAN IP address using Method 2
        if (lanIp == null) {
            System.err.println("Could not determine a suitable LAN IP address. Exiting.");
            return;
        }
        System.out.println("LAN IP Address: " + lanIp);
        int port = 8080; // Choose your port
        InetSocketAddress address = new InetSocketAddress(lanIp, port);
        QueueManagementServer server = new QueueManagementServer(address);
        try {
            server.start();
            System.out.println("Queue Management WebSocket server started on " + lanIp + ":" + port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.stop();
                    System.out.println("Server stopped successfully.");
                    DatabaseConnection.closeConnection();
                } catch (InterruptedException | SQLException e) {
                    System.err.println("Error while stopping the server: " + e.getMessage());
                }
            }));
        } catch (Exception e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }

    private static String getLanIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {  // Consider only up, non-loopback interfaces
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && address.getAddress().length > 0) {
                            String ipAddress = address.getHostAddress();
                            if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || ipAddress.startsWith("172.16.")) {
                                return ipAddress; // Return the first suitable IPv4 address
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error getting network interfaces: " + e.getMessage());
        }
        return null; // Return null if no suitable IP address is found
    }
}
