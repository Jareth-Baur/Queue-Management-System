package dorsu.jareth.queue;

import dorsu.jareth.auth.Authentication;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Properties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class QueueManagementApp extends Application {

    private TextArea queueStatusArea;
    private Button issueTicketButton;
    private Button callNextTicketButton;
    private Button refreshQueueStatusButton;
    private Button infoWindowButton;
    private ImageView dorsuLogo;

    private Stage dashboardStage;

    private WebSocketClient webSocketClient;

    private Label currentlyServingLabel;
    private Label nextInQueueLabel;
    private Label totalQueueLabel;
    private Stage infoStage;
    private MediaPlayer mediaPlayer;

    private final int officeID;
    private String officeName;

    // Constructor to receive the office ID
    public QueueManagementApp(int officeID) {
        this.officeID = officeID;
        this.officeName = fetchOfficeName(officeID); // Fetch name immediately
    }

    @Override
    public void start(Stage primaryStage) throws URISyntaxException {
        dashboardStage = primaryStage;
        initializeWebSocketClient();
        Scene scene = createDashboardScene(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Queue Management System - " + officeName + " Office");
        primaryStage.show();
        currentlyServingLabel = new Label();
        nextInQueueLabel = new Label();
        totalQueueLabel = new Label();
    }

    public Scene createDashboardScene(Stage primaryStage) {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(788, 543);
        root.setStyle("-fx-background-color: #404040;");

        queueStatusArea = new TextArea();
        queueStatusArea.setStyle("-fx-background-color: #555; -fx-text-fill: black;");
        queueStatusArea.setEditable(false);
        queueStatusArea.setLayoutX(232);
        queueStatusArea.setLayoutY(14);
        queueStatusArea.setPrefSize(542, 436);

        issueTicketButton = new Button("Issue New Ticket");
        issueTicketButton.setLayoutX(232);
        issueTicketButton.setLayoutY(461);
        issueTicketButton.setPrefSize(138, 34);
        issueTicketButton.setOnAction(event -> issueNewTicket());

        callNextTicketButton = new Button("Call Next Ticket");
        callNextTicketButton.setLayoutX(394);
        callNextTicketButton.setLayoutY(461);
        callNextTicketButton.setPrefSize(138, 34);
        callNextTicketButton.setOnAction(event -> callNextTicket());

        refreshQueueStatusButton = new Button("Refresh Queue Status");
        refreshQueueStatusButton.setLayoutX(556);
        refreshQueueStatusButton.setLayoutY(461);
        refreshQueueStatusButton.setPrefSize(138, 34);
        refreshQueueStatusButton.setOnAction(event -> refreshQueueStatus());

        infoWindowButton = new Button("Info Window");
        infoWindowButton.setLayoutX(53);
        infoWindowButton.setLayoutY(230);
        infoWindowButton.setPrefSize(121, 25);
        infoWindowButton.setOnAction(event -> openInformationWindow());

        dorsuLogo = new ImageView(new Image(getClass().getResourceAsStream("/dorsu/jareth/auth/DOrSU_logo.png")));
        dorsuLogo.setLayoutX(53);
        dorsuLogo.setLayoutY(26);
        dorsuLogo.setFitHeight(133);
        dorsuLogo.setFitWidth(133);
        dorsuLogo.setPreserveRatio(true);

        issueTicketButton.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
        callNextTicketButton.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
        refreshQueueStatusButton.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
        infoWindowButton.setStyle("-fx-background-color: #555; -fx-text-fill: white;");

        root.getChildren().addAll(
                queueStatusArea,
                issueTicketButton,
                callNextTicketButton,
                refreshQueueStatusButton,
                infoWindowButton,
                dorsuLogo
        );

        return new Scene(root, 788, 543);
    }

    private void initializeWebSocketClient() throws URISyntaxException {
        String serverIp = getServerIpAddress(); // Get the server's LAN IP address
        if (serverIp == null) {
            // Handle the case where the server IP cannot be determined
            System.err.println("Could not determine server IP address. Exiting.");
            return; // Or throw an exception
        }
        int port = 8080; // Port number your server is listening on
        URI serverUri = new URI("ws://" + serverIp + ":" + port);

        webSocketClient = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Connected to WebSocket Server");
                sendWebSocketMessage("queuestatus");
            }

            @Override
            public void onMessage(String message) {
                Platform.runLater(() -> {
                    queueStatusArea.appendText(message + "\n");
                    if (nextInQueueLabel != null && totalQueueLabel != null && currentlyServingLabel != null) {
                        updateInfoWindow(message);
                    }
                });
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                System.err.println("WebSocket Error: " + ex.getMessage());
            }
        };

        webSocketClient.connect();
    }

    // Function to get the server's IP address (you'll likely need to modify this)
    private String getServerIpAddress() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input);
            return properties.getProperty("serverIp");
        } catch (IOException e) {
            System.err.println("Error loading configuration file: " + e.getMessage());
            return null;
        }
    }

    private void sendWebSocketMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(officeID + ":" + message);
        } else {
            System.err.println("WebSocket is not connected.");
        }
    }

    private void createInfoWindow() {
        File defaultVideoFile = new File("ad/QueueManagementSystem.mp4");
        if (!defaultVideoFile.exists()) {
            System.err.println("Error: Video file not found at: " + defaultVideoFile.getAbsolutePath());
            return;
        }

        infoStage = new Stage();
        infoStage.setTitle("Queue Info - " + officeName + " Office");

        AnchorPane infoPane = new AnchorPane();
        infoPane.setStyle("-fx-background-color: #404040;");
        infoPane.setPrefWidth(1308);
        infoPane.setPrefHeight(918);

        AnchorPane containerForTheInfo = new AnchorPane();
        containerForTheInfo.setPrefHeight(919);
        containerForTheInfo.setPrefWidth(442);
        AnchorPane.setLeftAnchor(containerForTheInfo, 0.0);
        AnchorPane.setTopAnchor(containerForTheInfo, 0.0);

        currentlyServingLabel.setText("Currently Serving: None (" + officeName + ")");
        currentlyServingLabel.setStyle("-fx-text-fill: white;");
        currentlyServingLabel.setFont(new Font(28));
        AnchorPane.setTopAnchor(currentlyServingLabel, 166.0);
        AnchorPane.setLeftAnchor(currentlyServingLabel, 20.0);
        containerForTheInfo.getChildren().add(currentlyServingLabel);

        nextInQueueLabel.setText("Next in Queue: None (" + officeName + ")");
        nextInQueueLabel.setStyle("-fx-text-fill: white;");
        nextInQueueLabel.setFont(new Font(28));
        AnchorPane.setTopAnchor(nextInQueueLabel, 374.0);
        AnchorPane.setLeftAnchor(nextInQueueLabel, 20.0);
        containerForTheInfo.getChildren().add(nextInQueueLabel);

        totalQueueLabel.setText("Total Tickets in Queue: 0 (" + officeName + ")");
        totalQueueLabel.setStyle("-fx-text-fill: white;");
        totalQueueLabel.setFont(new Font(28));
        AnchorPane.setTopAnchor(totalQueueLabel, 270.0);
        AnchorPane.setLeftAnchor(totalQueueLabel, 20.0);
        containerForTheInfo.getChildren().add(totalQueueLabel);

        Button selectFileButton = new Button("Select File");
        selectFileButton.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
        AnchorPane.setBottomAnchor(selectFileButton, 12.0);
        AnchorPane.setLeftAnchor(selectFileButton, 14.0);
        containerForTheInfo.getChildren().add(selectFileButton);

        StackPane containerForTheMedia = new StackPane();
        AnchorPane.setRightAnchor(containerForTheMedia, 0.0);
        AnchorPane.setTopAnchor(containerForTheMedia, 0.0);
        AnchorPane.setBottomAnchor(containerForTheMedia, 0.0);
        AnchorPane.setLeftAnchor(containerForTheMedia, 442.0);

        Pane mediaWrapper = new Pane();
        StackPane.setAlignment(mediaWrapper, Pos.CENTER);
        containerForTheMedia.getChildren().add(mediaWrapper);

        MediaView mediaView = new MediaView();
        mediaWrapper.getChildren().add(mediaView);
        mediaView.setPreserveRatio(false);

        mediaWrapper.prefWidthProperty().bind(containerForTheMedia.widthProperty());
        mediaWrapper.prefHeightProperty().bind(containerForTheMedia.heightProperty());

        mediaView.fitWidthProperty().bind(mediaWrapper.prefWidthProperty());
        mediaView.fitHeightProperty().bind(mediaWrapper.prefHeightProperty());

        Media media = new Media(defaultVideoFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));

        selectFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Video File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi")
            );
            File selectedFile = fileChooser.showOpenDialog(infoStage);
            if (selectedFile != null) {
                try {
                    URI selectedFileURI = selectedFile.toURI();
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                    }
                    Media newMedia = new Media(selectedFileURI.toString());
                    mediaPlayer = new MediaPlayer(newMedia);
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaPlayer.setAutoPlay(true);
                    mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                } catch (Exception ex) {
                    System.err.println("Error creating media from selected file: " + ex.getMessage());
                }
            }
        });

        infoStage.setOnCloseRequest(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });
        Scene infoScene = new Scene(infoPane);
        infoPane.getChildren().addAll(containerForTheInfo, containerForTheMedia);
        infoStage.setScene(infoScene);
        infoStage.show();
    }

    private void updateInfoWindow(String message) {
        if (message.startsWith("Serving:")) {
            currentlyServingLabel.setText("Currently Serving: " + message.substring("Serving: ".length()) + " (" + officeName + ")");
        } else if (message.startsWith("Queue Status:")) {
            String queueStatus = message.substring("Queue Status: ".length()).trim();
            if (queueStatus.equals("The queue is empty for this office.")) {
                nextInQueueLabel.setText("Next in Queue: None (" + officeName + ")");
                totalQueueLabel.setText("Total Tickets in Queue: 0 (" + officeName + ")");
            } else {
                String[] queueParts = queueStatus.split(", ");
                String nextTicket = queueParts.length > 0 ? queueParts[0] : "None";
                nextInQueueLabel.setText("Next in Queue: " + nextTicket + " (" + officeName + ")");
                totalQueueLabel.setText("Total Tickets in Queue: " + queueParts.length + " (" + officeName + ")");
            }
        }
    }

    private void issueNewTicket() {
        sendWebSocketMessage("newticket");
    }

    private void callNextTicket() {
        sendWebSocketMessage("nextticket");
    }

    private void refreshQueueStatus() {
        sendWebSocketMessage("queuestatus");
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

    private void openInformationWindow() {
        createInfoWindow();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to fetch the office name from the database
    private String fetchOfficeName(int officeID) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/queue_management", "root", "")) {
            String sql = "SELECT name FROM offices WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, officeID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        officeName = rs.getString("name");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching office name: " + e.getMessage());
            showAlert("Database Error", "Could not fetch office name from database.", AlertType.ERROR);
        }
        return officeName;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
