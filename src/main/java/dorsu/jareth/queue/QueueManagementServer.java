package dorsu.jareth.queue;

import dorsu.jareth.auth.DatabaseConnection;
import java.io.FileWriter;
import java.io.IOException;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueueManagementServer extends WebSocketServer {

    private Connection connection;

    public QueueManagementServer(InetSocketAddress address) {
        super(address);
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/queue_management";
            String user = "root";
            String password = ""; // Replace with your MySQL password
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
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
        if (parts.length >= 2) { // Check if there's at least an officeID and a command
            try {
                int officeId = Integer.parseInt(parts[0]); //officeID is the first part
                String command = parts[1].toLowerCase(); //Command is the second part

                switch (command) {
                    case "newticket":
                        issueNewTicket(conn, officeId);
                        break;
                    case "nextticket":
                        callNextTicket(conn, officeId); // Pass officeId to callNextTicket
                        break;
                    case "queuestatus":
                        sendQueueStatus(conn, officeId); // Pass officeId to sendQueueStatus
                        break;
                    default:
                        conn.send("Unknown command: " + message);
                }
            } catch (IOException | NumberFormatException e) {
                conn.send("Invalid office ID in message.");
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

    // Method to fetch the office name from the database
    private String fetchOfficeName(int officeId) {
        String officeName = null;
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT name FROM offices WHERE id = ?")) {
            statement.setInt(1, officeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    officeName = resultSet.getString("name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching office name: " + e.getMessage());
            // Consider adding more robust error handling here, such as displaying an error alert to the user.
        }
        return officeName;
    }

    private void issueNewTicket(WebSocket conn, int officeId) throws IOException {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/queue_management", "root", "")) {
            int newTicketNumber = getLastTicketNumber(connection) + 1; // Pass connection to getLastTicketNumber
            String newTicket = "Ticket-" + newTicketNumber;
            String sql = "INSERT INTO tickets (ticket_number, status, created_at, office_id) VALUES (?, 'PENDING', NOW(), ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newTicket);
                stmt.setInt(2, officeId);
                stmt.executeUpdate();
                String officeName = getOfficeName(connection, officeId); // Get office name
                conn.send("New ticket issued: " + newTicket + " (" + officeName + ")");
                broadcastQueueStatus(officeId);
                generateTicketText(newTicket, officeId);
                conn.send("Ticket PDF generated: ticket_" + newTicketNumber + ".pdf");
            }
        } catch (SQLException e) {
            conn.send("Error issuing a new ticket or generating PDF: " + e.getMessage());
            System.err.println("Error issuing new ticket: " + e.getMessage());
        }
    }

    private void callNextTicket(WebSocket conn, int officeId) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/queue_management", "root", "")) {
            String sql = "SELECT t.id, t.ticket_number, o.name AS office_name FROM tickets t JOIN offices o ON t.office_id = o.id WHERE t.status = 'PENDING' AND t.office_id = ? ORDER BY t.id LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, officeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String nextTicket = rs.getString("ticket_number");
                        String officeName = rs.getString("office_name");
                        conn.send("Serving: " + nextTicket + " (" + officeName + ")");

                        sql = "UPDATE tickets SET status = 'SERVED' WHERE id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(sql)) {
                            updateStmt.setInt(1, id);
                            updateStmt.executeUpdate();
                        }

                        broadcast("Serving: " + nextTicket + " (" + officeName + ")");
                        broadcastQueueStatus(officeId);
                    } else {
                        conn.send("No tickets in the queue for this office.");
                    }
                }
            }
        } catch (SQLException e) {
            conn.send("Error calling the next ticket: " + e.getMessage());
            System.err.println("Error calling next ticket: " + e.getMessage());
        }
    }

    // Helper function to get the office name
    private String getOfficeName(Connection connection, int officeId) throws SQLException {
        String sql = "SELECT name FROM offices WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, officeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return "Unknown Office"; // Or handle the case where the office is not found appropriately
    }

    //Helper function to retrieve the last ticket number from the database.
    private int getLastTicketNumber(Connection connection) throws SQLException {
        String sql = "SELECT MAX(id) AS max_id FROM tickets";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("max_id");
            }
        }
        return 0;
    }

    private void sendQueueStatus(WebSocket conn, int officeId) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/queue_management", "root", "")) {
            String sql = "SELECT t.ticket_number, o.name AS office_name FROM tickets t JOIN offices o ON t.office_id = o.id WHERE t.status = 'PENDING' AND t.office_id = ? ORDER BY t.id";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
            }
        } catch (SQLException e) {
            conn.send("Error fetching queue status: " + e.getMessage());
            System.err.println("Error fetching queue status: " + e.getMessage());
        }
    }

    private void broadcastQueueStatus(int officeId) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/queue_management", "root", "")) {
            String sql = "SELECT t.ticket_number, o.name AS office_name FROM tickets t JOIN offices o ON t.office_id = o.id WHERE t.status = 'PENDING' AND t.office_id = ? ORDER BY t.id";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
            }
        } catch (SQLException e) {
            System.err.println("Error broadcasting queue status: " + e.getMessage());
        }
    }

    private int getLastTicketNumber() throws SQLException {
        String sql = "SELECT MAX(id) AS max_id FROM tickets";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            return rs.getInt("max_id");
        }
        return 0;
    }

    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        QueueManagementServer server = new QueueManagementServer(address);
        try {
            server.start();
            System.out.println("Queue Management WebSocket server started on port 8080");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.stop();
                    System.out.println("Server stopped successfully.");
                } catch (InterruptedException e) {
                    System.err.println("Error while stopping the server: " + e.getMessage());
                }
            }));
        } catch (Exception e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }
}
