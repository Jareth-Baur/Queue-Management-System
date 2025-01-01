package dorsu.jareth.queue;

import dorsu.jareth.auth.Authentication;
import java.net.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class MainDashboard extends Application {

    private TableView<Office> officesTable;
    private Button startServerButton;
    private Button stopServerButton;
    private Button addNewOfficeButton;
    private Button viewTicketChartsButton;
    private Button viewTicketHistoryButton;
    private Button startQueueButton;
    private Button deleteOfficeButton; // Add a new button for deleting offices
    private Button logoutButton;
    private ImageView dorsuLogo;

    private Stage dashboardStage;

    private ObservableList<Office> officeData = FXCollections.observableArrayList();
    private QueueManagementServer server;
    private boolean serverStarted = false; // Flag to track server status

    @Override
    public void start(Stage primaryStage) {
        dashboardStage = primaryStage;
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Application closing...");
            Platform.exit();
            System.exit(0);
        });
        // Create UI elements
        officesTable = createOfficesTable();
        startServerButton = createButton("Start Server");
        stopServerButton = createButton("Stop Server");
        addNewOfficeButton = createButton("Add New Office");
        viewTicketChartsButton = createButton("View Ticket Charts");
        viewTicketHistoryButton = createButton("View Ticket History");
        startQueueButton = createButton("Start Queue");
        deleteOfficeButton = createButton("Delete Office"); // Create the delete button
        logoutButton = createButton("Logout");
        dorsuLogo = createImageView();

        // Fetch data from the database
        fetchDataFromDatabase();

        // Layout using AnchorPane
        AnchorPane root = new AnchorPane();
        root.setPrefHeight(710);
        root.setPrefWidth(1020);
        root.setStyle("-fx-background-color: #f0f0f0;"); // Light gray background

        //Improved Layout using VBox
        VBox buttonBox = new VBox(10); //Spacing of 10 between buttons
        buttonBox.getChildren().addAll(startServerButton, stopServerButton, addNewOfficeButton, viewTicketChartsButton, viewTicketHistoryButton);
        buttonBox.setPadding(new Insets(20, 20, 20, 20)); // Add padding around the buttons

        officesTable.setLayoutX(229);
        officesTable.setLayoutY(10);
        buttonBox.setLayoutX(20);
        buttonBox.setLayoutY(150);
        startQueueButton.setLayoutX(216);
        startQueueButton.setLayoutY(661);
        logoutButton.setLayoutX(28);
        logoutButton.setLayoutY(661);
        dorsuLogo.setLayoutX(50);
        dorsuLogo.setLayoutY(20);

        deleteOfficeButton.setLayoutX(430); // Adjust layout as needed
        deleteOfficeButton.setLayoutY(661); // Adjust layout as needed
        deleteOfficeButton.setOnAction(e -> deleteOffice()); // Add action handler

        // Add action handlers to buttons
        startServerButton.setOnAction(e -> startServer());
        stopServerButton.setOnAction(e -> stopServer());
        addNewOfficeButton.setOnAction(e -> addNewOffice());
        viewTicketChartsButton.setOnAction(e -> {
            try {
                viewTicketCharts();
            } catch (Exception ex) {
            }
        });
        viewTicketHistoryButton.setOnAction(e -> {
            try {
                viewTicketHistory();
            } catch (Exception ex) {
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
            }
        });
        root.getChildren().addAll(officesTable, buttonBox, startQueueButton, deleteOfficeButton, logoutButton, dorsuLogo);

        // Create scene and set to stage
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Queue Management System - Dashboard");
        primaryStage.show();

    }

    // Helper method to create buttons
    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(25);
        button.setPrefWidth(124);
        return button;
    }

    // Helper method to create the ImageView
    private ImageView createImageView() {
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/dorsu/jareth/auth/DOrSU_logo.png")));
        imageView.setFitHeight(133);
        imageView.setFitWidth(138);
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
            return new SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        });
        createdAtColumn.setPrefWidth(247);

        tableView.getColumns().addAll(officeIdColumn, officeNameColumn, officeDetailsColumn, createdAtColumn);
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
        }
    }

    // Separate methods for each button's functionality
    private void startServer() {
        if (serverStarted) {
            showAlert("Server Status", "Server is already running.", Alert.AlertType.WARNING);
            return;
        }
        System.out.println("Starting server...");
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8080);
        server = new QueueManagementServer(serverAddress);
        new Thread(() -> {
            try {
                server.start();
                serverStarted = true;
                System.out.println("Server started successfully.");
                // Use Platform.runLater to update the UI on the FX Application Thread
                Platform.runLater(() -> showAlert("Server Status", "Server started successfully.", AlertType.INFORMATION));
            } catch (Exception e) {
                System.err.println("Error starting server: " + e.getMessage());
                Platform.runLater(() -> showAlert("Server Error", "Could not start the server: " + e.getMessage(), AlertType.ERROR)); // Use Platform.runLater here too
            }
        }).start();
    }

    private void stopServer() {
        if (!serverStarted) {
            showAlert("Server Status", "Server is not running.", Alert.AlertType.WARNING);
            return;
        }
        System.out.println("Stopping server...");
        try {
            server.stop();
            serverStarted = false;
            System.out.println("Server stopped successfully.");
            Platform.runLater(() -> showAlert("Server Status", "Server stopped successfully.", AlertType.INFORMATION)); // Use Platform.runLater here as well
        } catch (InterruptedException e) {
            System.err.println("Error stopping server: " + e.getMessage());
            Platform.runLater(() -> showAlert("Server Error", "Could not stop the server: " + e.getMessage(), Alert.AlertType.ERROR)); // Use Platform.runLater here as well
        }
    }

    private void addNewOffice() {
        Stage addOfficeStage = new Stage();
        addOfficeStage.setTitle("Add New Office");

        AnchorPane addOfficePane = new AnchorPane();
        addOfficePane.setStyle("-fx-background-color: #f0f0f0;"); // Light gray background
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
                showAlert("Error", "Office name cannot be empty.", AlertType.ERROR);
                return;
            }
            addOfficeToDatabase(officeName, officeDetails);
            addOfficeStage.close();
            refreshOfficesTable(); //Refresh the main table after adding a new office
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
                int rowsAffected = stmt.executeUpdate(); // Get the number of rows affected
                if (rowsAffected > 0) {
                    System.out.println("Office added successfully.");
                    showAlert("Success", "Office added successfully.", AlertType.INFORMATION);
                } else {
                    System.out.println("Failed to add office.");
                    showAlert("Error", "Failed to add office. Please check the data and try again.", AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding office to database: " + e.getMessage());
            showAlert("Database Error", "Could not add office to database: " + e.getMessage(), AlertType.ERROR); // Include the error message
        }
    }

    private void refreshOfficesTable() {
        officeData.clear();
        fetchDataFromDatabase();
    }

    private void viewTicketCharts() throws Exception {
        System.out.println("View Ticket Charts action performed");
        // Add your logic to display ticket charts here. This might involve opening a new window or updating a chart within the existing window.
        TicketsCharts app = new TicketsCharts();
        Stage primaryStage = new Stage();
        app.start(primaryStage);
        primaryStage.show();
        System.out.println("Displaying dashboard...");
    }

    private void viewTicketHistory() throws Exception {
        System.out.println("View Ticket History action performed");
        // Add your logic to display ticket history here. This might involve opening a new window or updating a table within the existing window.
        TicketsTable app = new TicketsTable();
        Stage primaryStage = new Stage();
        app.start(primaryStage);
        primaryStage.show();
        System.out.println("Displaying queue history...");
    }

    // Modified startQueue method to accept office ID
    private void startQueue(int officeID) {
        System.out.println("Start Queue action performed for office ID: " + officeID);
        if (serverStarted) {
            try {
                QueueManagementApp queueApp = new QueueManagementApp(officeID); // Pass officeID to the constructor
                Stage queueStage = new Stage();
                queueApp.start(queueStage);
                queueStage.show();
            } catch (URISyntaxException e) {
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
            Alert alert = new Alert(AlertType.CONFIRMATION);
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
}
