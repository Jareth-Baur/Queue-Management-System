package dorsu.jareth.queue;

import dorsu.jareth.auth.Authentication;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.net.InetAddress;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert.AlertType;

public class MainDashboard extends Application {

    private TableView<Office> officesTable;
    private TableView<Ticket> ticketsTable;
    private Button startServerButton;
    private Button stopServerButton;
    private Button addNewOfficeButton;
    private Button viewTicketChartsButton;
    private Button viewTicketHistoryButton;
    private Button startQueueButton;
    private Button deleteOfficeButton;
    private Button editOfficeButton;
    private Button logoutButton;
    private ImageView dorsuLogo;

    private Stage dashboardStage;

    private ObservableList<Office> officeData = FXCollections.observableArrayList();
    private ObservableList<Ticket> ticketData = FXCollections.observableArrayList();
    private QueueManagementServer server;
    private boolean serverStarted = false;

    @Override
    public void start(Stage primaryStage) {
        Image icon = new Image(getClass().getResourceAsStream("/dorsu/jareth/auth/queue_icon.png"));
        primaryStage.getIcons().add(icon);
        dashboardStage = primaryStage;
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Application closing...");
            Platform.exit();
            System.exit(0);
        });

        officesTable = createOfficesTable();
        ticketsTable = createTicketsTable();
        startServerButton = createButton("Start Server", 51, 221, 124, 25);
        stopServerButton = createButton("Stop Server", 51, 262, 124, 25);
        addNewOfficeButton = createButton("Add new Office", 51, 302, 124, 25);
        viewTicketChartsButton = createButton("View Ticket Charts", 51, 344, 124, 25);
        viewTicketHistoryButton = createButton("View Ticket History", 51, 384, 124, 25);
        startQueueButton = createButton("Start Queue", 229, 661, 97, 25);
        deleteOfficeButton = createButton("Delete Office", 343, 661, 97, 25);
        editOfficeButton = createButton("Edit Office", 453, 661, 97, 25);
        logoutButton = createButton("Logout", 28, 661, 97, 25);
        dorsuLogo = createImageView(28, 14, 179, 169);

        fetchDataFromDatabase();

        AnchorPane root = new AnchorPane();
        root.setPrefHeight(710);
        root.setPrefWidth(1002);
        root.setStyle("-fx-background-color: #f0f0f0;");

        VBox buttonBox = new VBox(10);
        buttonBox.getChildren().addAll(startServerButton, stopServerButton, addNewOfficeButton, viewTicketChartsButton, viewTicketHistoryButton);
        buttonBox.setPadding(new Insets(20, 20, 20, 20));
        buttonBox.setLayoutX(30);
        buttonBox.setLayoutY(190);

        officesTable.setLayoutX(229);
        officesTable.setLayoutY(16);

        VBox officesAndTickets = new VBox(10); // Use VBox to stack tables vertically
        officesAndTickets.getChildren().addAll(officesTable, ticketsTable);
        officesAndTickets.setPadding(new Insets(10));
        officesAndTickets.setLayoutX(229);
        officesAndTickets.setLayoutY(16);

        root.getChildren().addAll(officesAndTickets, startServerButton, dorsuLogo, stopServerButton, addNewOfficeButton, viewTicketChartsButton, viewTicketHistoryButton, startQueueButton, deleteOfficeButton, editOfficeButton, logoutButton);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Queue Management System - Dashboard");
        primaryStage.show();

        // Action Handlers (Add the rest of your action handlers here)
        startServerButton.setOnAction(e -> startServer());
        stopServerButton.setOnAction(e -> stopServer());
        addNewOfficeButton.setOnAction(e -> addNewOffice());
        viewTicketChartsButton.setOnAction(e -> {
            try {
                viewTicketCharts();
            } catch (Exception ex) {
                ex.printStackTrace(); // Handle exceptions properly
            }
        });
        viewTicketHistoryButton.setOnAction(e -> {
            try {
                viewTicketHistory();
            } catch (Exception ex) {
                ex.printStackTrace(); // Handle exceptions properly
            }
        });
        startQueueButton.setOnAction(e -> {
            Office selectedOffice = officesTable.getSelectionModel().getSelectedItem();
            if (selectedOffice != null) {
                startQueue(selectedOffice.getId());
            } else {
                showAlert("Selection Required", "Please select an office from the table.", Alert.AlertType.WARNING);
            }
        });
        logoutButton.setOnAction(e -> {
            try {
                logout();
            } catch (InterruptedException x) {
                x.printStackTrace(); // Handle exceptions properly
            }
        });
        deleteOfficeButton.setOnAction(e -> deleteOffice());
        // Add action handler for editOfficeButton
        editOfficeButton.setOnAction(e -> editOffice());
    }

    private Button createButton(String text, double layoutX, double layoutY, double prefWidth, double prefHeight) {
        Button button = new Button(text);
        button.setLayoutX(layoutX);
        button.setLayoutY(layoutY);
        button.setPrefHeight(prefHeight);
        button.setPrefWidth(prefWidth);
        return button;
    }

    private ImageView createImageView(double layoutX, double layoutY, double fitWidth, double fitHeight) {
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/dorsu/jareth/auth/queue_logo.png")));
        imageView.setLayoutX(layoutX);
        imageView.setLayoutY(layoutY);
        imageView.setFitHeight(fitHeight);
        imageView.setFitWidth(fitWidth);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private TableView<Office> createOfficesTable() {
        TableView<Office> tableView = new TableView<>();

        TableColumn<Office, Integer> officeIdColumn = new TableColumn<>("Office ID");
        officeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        officeIdColumn.setPrefWidth(102);

        TableColumn<Office, String> officeNameColumn = new TableColumn<>("Office Name");
        officeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        officeNameColumn.setPrefWidth(149);

        TableColumn<Office, String> officeDetailsColumn = new TableColumn<>("Details");
        officeDetailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        officeDetailsColumn.setPrefWidth(274);

        TableColumn<Office, String> createdAtColumn = new TableColumn<>("Created At");
        createdAtColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        });
        createdAtColumn.setPrefWidth(223);

        tableView.getColumns().addAll(officeIdColumn, officeNameColumn, officeDetailsColumn, createdAtColumn);
        return tableView;
    }

    private TableView<Ticket> createTicketsTable() {
        TableView<Ticket> tableView = new TableView<>();

        TableColumn<Ticket, String> officeColumn = new TableColumn<>("Office");
        officeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOfficeName())); // Placeholder - needs actual getter
        officeColumn.setPrefWidth(196);

        TableColumn<Ticket, String> ticketColumn = new TableColumn<>("Ticket");
        ticketColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTicketNumber())); // Placeholder - needs actual getter
        ticketColumn.setPrefWidth(176);

        TableColumn<Ticket, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus())); // Placeholder - needs actual getter
        statusColumn.setPrefWidth(171);

        TableColumn<Ticket, String> dateIssuedColumn = new TableColumn<>("Date Issued");
        dateIssuedColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate() + " " + cellData.getValue().getFormattedTime())); // Placeholder - needs actual getter and assumes LocalDateTime
        dateIssuedColumn.setPrefWidth(205);

        tableView.getColumns().addAll(officeColumn, ticketColumn, statusColumn, dateIssuedColumn);
        return tableView;
    }

    private void fetchDataFromDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/queue_management", "root", "")) {
            String sql = "SELECT id, name, details, created_at FROM offices";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String details = rs.getString("details");
                    Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = LocalDateTime.ofInstant(createdAtTimestamp.toInstant(), ZoneId.systemDefault());
                    officeData.add(new Office(id, name, details, createdAt));
                }
            }
            officesTable.setItems(officeData);
        } catch (SQLException e) {
            System.err.println("Error fetching data from database: " + e.getMessage());
            showAlert("Database Error", "Could not fetch office data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/queue_management", "root", "")) {
            String sql = "SELECT o.name as office_name, t.ticket_number, t.status, t.created_at "
                    + "FROM offices o JOIN tickets t ON o.id = t.office_id "
                    + "WHERE t.status = 'pending'";

            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String officeName = rs.getString("office_name");
                    String ticketNumber = rs.getString("ticket_number");
                    String status = rs.getString("status");
                    Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = LocalDateTime.ofInstant(createdAtTimestamp.toInstant(), ZoneId.systemDefault());

                    // Format the date and time
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a"); // 12-hour format with AM/PM

                    String formattedDate = createdAt.format(dateFormatter);
                    String formattedTime = createdAt.format(timeFormatter);

                    ticketData.add(new Ticket(officeName, ticketNumber, status, formattedDate, formattedTime)); // Modified Ticket constructor
                }
            }
            ticketsTable.setItems(ticketData);
        } catch (SQLException e) {
            System.err.println("Error fetching ticket data from database: " + e.getMessage());
            showAlert("Database Error", "Could not fetch ticket data: " + e.getMessage(), Alert.AlertType.ERROR);
        }

    }

    private void startServer() {
        if (serverStarted) {
            showAlert("Server Status", "Server is already running.", Alert.AlertType.WARNING);
            return;
        }
        System.out.println("Starting server...");
        String lanIp = getLanIpAddress();
        if (lanIp == null) {
            showAlert("Server Error", "Could not determine LAN IP address.", Alert.AlertType.ERROR);
            return;
        }
        int port = 8080;
        InetSocketAddress serverAddress = new InetSocketAddress(lanIp, port);

        server = new QueueManagementServer(serverAddress);
        new Thread(() -> {
            try {
                server.start();
                serverStarted = true;
                System.out.println("Server started successfully on " + lanIp + ":" + port);
                Platform.runLater(() -> showAlert("Server Status", "Server started successfully on " + lanIp + ":" + port, Alert.AlertType.INFORMATION));
            } catch (Exception e) {
                System.err.println("Error starting server: " + e.getMessage());
                Platform.runLater(() -> showAlert("Server Error", "Could not start the server: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private static String getLanIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && address.getAddress().length > 0) {
                            String ipAddress = address.getHostAddress();
                            if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || ipAddress.startsWith("172.16.")) {
                                return ipAddress;
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error getting network interfaces: " + e.getMessage());
        }
        return null;
    }

    private void stopServer() {
        if (!serverStarted) {
            showAlert("Server Status", "Server is not running.", Alert.AlertType.WARNING);
            return;
        }
        System.out.println("Stopping server...");
        try {
            server.stop(10000);
            serverStarted = false;
            System.out.println("Server stopped successfully.");
            Platform.runLater(() -> showAlert("Server Status", "Server stopped successfully.", Alert.AlertType.INFORMATION));
        } catch (InterruptedException e) {
            System.err.println("Error stopping server: " + e.getMessage());
            Platform.runLater(() -> showAlert("Server Error", "Could not stop the server: " + e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void addNewOffice() {
        Stage addOfficeStage = new Stage();
        addOfficeStage.setTitle("Add New Office");

        AnchorPane addOfficePane = new AnchorPane();
        addOfficePane.setStyle("-fx-background-color: #f0f0f0;");
        addOfficePane.setPrefSize(300, 200);

        Label nameLabel = new Label("Office Name:");
        nameLabel.setLayoutX(20);
        nameLabel.setLayoutY(20);

        TextField nameField = new TextField();
        nameField.setLayoutX(120);
        nameField.setLayoutY(15);
        nameField.setPrefWidth(150);

        Label detailsLabel = new Label("Details:");
        detailsLabel.setLayoutX(20);
        detailsLabel.setLayoutY(60);

        TextArea detailsArea = new TextArea();
        detailsArea.setLayoutX(120);
        detailsArea.setLayoutY(55);
        detailsArea.setPrefSize(150, 80);

        Button addButton = new Button("Add Office");
        addButton.setLayoutX(100);
        addButton.setLayoutY(150);
        addButton.setOnAction(e -> {
            String officeName = nameField.getText();
            String officeDetails = detailsArea.getText();
            if (officeName.isEmpty()) {
                showAlert("Error", "Office name cannot be empty.", Alert.AlertType.ERROR);
                return;
            }
            addOfficeToDatabase(officeName, officeDetails);
            addOfficeStage.close();
            refreshOfficesTable();
        });

        addOfficePane.getChildren().addAll(nameLabel, nameField, detailsLabel, detailsArea, addButton);
        Scene addOfficeScene = new Scene(addOfficePane);
        addOfficeStage.setScene(addOfficeScene);
        addOfficeStage.show();
    }

    private void addOfficeToDatabase(String officeName, String officeDetails) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/queue_management", "root", "")) {
            String sql = "INSERT INTO offices (name, details, created_at) VALUES (?, ?, NOW())";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, officeName);
                stmt.setString(2, officeDetails);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Office added successfully.");
                    showAlert("Success", "Office added successfully.", Alert.AlertType.INFORMATION);
                } else {
                    System.out.println("Failed to add office.");
                    showAlert("Error", "Failed to add office. Please check the data and try again.", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding office to database: " + e.getMessage());
            showAlert("Database Error", "Could not add office to database: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshOfficesTable() {
        officeData.clear();
        fetchDataFromDatabase();
    }

    private void viewTicketCharts() throws Exception {
        System.out.println("View Ticket Charts action performed");
        TicketsCharts app = new TicketsCharts();
        Stage primaryStage = new Stage();
        app.start(primaryStage);
        primaryStage.show();
        System.out.println("Displaying dashboard...");
    }

    private void viewTicketHistory() throws Exception {
        System.out.println("View Ticket History action performed");
        TicketsTable app = new TicketsTable();
        Stage primaryStage = new Stage();
        app.start(primaryStage);
        primaryStage.show();
        System.out.println("Displaying queue history...");
    }

    private void startQueue(int officeID) {
        System.out.println("Start Queue action performed for office ID: " + officeID);
        if (serverStarted) {
            try {
                QueueManagementApp queueApp = new QueueManagementApp(officeID);
                Stage queueStage = new Stage();
                queueApp.start(queueStage);
                queueStage.show();
            } catch (Exception e) {
                System.err.println("Error starting queue application: " + e.getMessage());
                showAlert("Error", "Could not start the queue application: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Server Status", "Please start the server first.", Alert.AlertType.WARNING);
        }
    }

    private void deleteOffice() {
        Office selectedOffice = officesTable.getSelectionModel().getSelectedItem();
        if (selectedOffice != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Office Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete office '" + selectedOffice.getName() + "'?");
            ButtonType buttonTypeYes = new ButtonType("Yes");
            ButtonType buttonTypeNo = new ButtonType("No");
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
            ButtonType result = alert.showAndWait().orElse(buttonTypeNo);

            if (result == buttonTypeYes) {
                if (deleteOfficeFromDatabase(selectedOffice.getId())) {
                    refreshOfficesTable();
                }
            }
        } else {
            showAlert("Selection Required", "Please select an office from the table.", AlertType.WARNING);
        }
    }

    private void editOffice() {

    }

    // Modified to return a boolean indicating success or failure
    private boolean deleteOfficeFromDatabase(int officeId) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/queue_management", "root", "")) {
            // First check if there are any tickets associated with the office
            String checkSql = "SELECT COUNT(*) FROM tickets WHERE office_id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, officeId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert("Error", "Cannot delete office. There are tickets associated with this office.", AlertType.ERROR);
                        return false;
                    }
                }
            }

            // If no tickets are associated, proceed with deletion
            String deleteSql = "DELETE FROM offices WHERE id = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, officeId);
                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert("Success", "Office deleted successfully.", AlertType.INFORMATION);
                    return true;
                } else {
                    showAlert("Error", "No office found with that ID.", AlertType.ERROR);
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting office from database: " + e.getMessage());
            showAlert("Database Error", "Could not delete office from database: " + e.getMessage(), AlertType.ERROR);
            return false;
        }
    }

    private void logout() throws InterruptedException {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");
        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
        ButtonType result = alert.showAndWait().orElse(buttonTypeNo);

        if (result == buttonTypeYes) {
            Authentication app = new Authentication();
            Stage primaryStage = new Stage();
            app.start(primaryStage);
            primaryStage.show();
            this.dashboardStage.close();
            System.out.println("Logging out...");
        } else {
            System.out.println("Logout cancelled.");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Office {

        private final int id;
        private final String name;
        private final String details;
        private final LocalDateTime createdAt;

        public Office(int id, String name, String details, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.details = details;
            this.createdAt = createdAt;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDetails() {
            return details;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }

    // Add a Ticket class (You MUST implement this)
    public static class Ticket {

        private String officeName;
        private String ticketNumber;
        private String status;
        private String formattedDate; // Added formatted date string
        private String formattedTime; // Added formatted time string

        public Ticket(String officeName, String ticketNumber, String status, String formattedDate, String formattedTime) {
            this.officeName = officeName;
            this.ticketNumber = ticketNumber;
            this.status = status;
            this.formattedDate = formattedDate;
            this.formattedTime = formattedTime;
        }

        // Getters (add getters for formattedDate and formattedTime)
        public String getOfficeName() {
            return officeName;
        }

        public String getTicketNumber() {
            return ticketNumber;
        }

        public String getStatus() {
            return status;
        }

        public String getFormattedDate() {
            return formattedDate;
        } // Getter for formatted date

        public String getFormattedTime() {
            return formattedTime;
        } // Getter for formatted time
    }

}
