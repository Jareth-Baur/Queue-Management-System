
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class QueueManagementApp extends Application {

    private WebSocketClient webSocketClient;
    private TextArea queueStatusArea;
    private Label currentlyServingLabel;
    private Label nextInQueueLabel;
    private Label totalQueueLabel;  // New label to display the number of tickets in the queue
    private Stage infoStage;

    @Override
    public void start(Stage primaryStage) throws URISyntaxException {
        initializeWebSocketClient();

        queueStatusArea = new TextArea();
        queueStatusArea.setEditable(false);
        queueStatusArea.setWrapText(true);
        queueStatusArea.setFont(Font.font("Arial", 18));  // Larger font for queue status area
        queueStatusArea.setPrefHeight(300);  // Adjusted height

        // Buttons with larger font
        Button issueTicketButton = createStyledButton("Issue New Ticket", "#4CAF50");
        Button callNextTicketButton = createStyledButton("Call Next Ticket", "#FF9800");
        Button refreshQueueButton = createStyledButton("Refresh Queue Status", "#2196F3");

        issueTicketButton.setPrefWidth(200);
        callNextTicketButton.setPrefWidth(200);
        refreshQueueButton.setPrefWidth(200);

        issueTicketButton.setOnAction(e -> sendWebSocketMessage("newTicket"));
        callNextTicketButton.setOnAction(e -> sendWebSocketMessage("nextTicket"));
        refreshQueueButton.setOnAction(e -> sendWebSocketMessage("queueStatus"));

        HBox buttonBox = new HBox(15, issueTicketButton, callNextTicketButton, refreshQueueButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        VBox layout = new VBox(20, queueStatusArea, buttonBox);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #F0F0F0; -fx-border-color: #333; -fx-border-radius: 8;");

        Scene scene = new Scene(layout, 900, 700); // Larger main window size
        primaryStage.setScene(scene);
        primaryStage.setTitle("Queue Management System");
        primaryStage.show();

        createInfoWindow();
    }

    private void createInfoWindow() {
        infoStage = new Stage();
        infoStage.setTitle("Queue Info");

        // Larger font for labels in the info window with a cleaner, more modern font
        currentlyServingLabel = new Label("Currently Serving: None");
        nextInQueueLabel = new Label("Next in Queue: None");
        totalQueueLabel = new Label("Total Tickets in Queue: 0");

        // Set modern fonts and adjust sizes
        currentlyServingLabel.setFont(Font.font("Segoe UI", 22));  // Modern font with a larger size
        nextInQueueLabel.setFont(Font.font("Segoe UI", 22));
        totalQueueLabel.setFont(Font.font("Segoe UI", 22));

        // Improve color contrast and text effects
        currentlyServingLabel.setTextFill(Color.web("#00796B")); // Teal color for better contrast
        nextInQueueLabel.setTextFill(Color.web("#00796B"));
        totalQueueLabel.setTextFill(Color.web("#00796B"));

        // Create a more visually appealing VBox layout with more padding and margins
        VBox infoLayout = new VBox(20, currentlyServingLabel, nextInQueueLabel, totalQueueLabel);
        infoLayout.setPadding(new Insets(30));  // Increased padding for breathing room
        infoLayout.setAlignment(Pos.CENTER);
        infoLayout.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 12; -fx-border-color: #00796B; -fx-border-width: 2;");

        // Adjust the scene size and apply styling
        Scene infoScene = new Scene(infoLayout, 450, 350); // Slightly larger window size for better spacing
        infoStage.setScene(infoScene);
        infoStage.show();
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("Arial", 16));  // Larger font for buttons
        button.setPrefWidth(180);
        button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 20; -fx-font-weight: bold;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -20%); -fx-background-radius: 20;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 20;"));
        return button;
    }

    private void initializeWebSocketClient() throws URISyntaxException {
        URI serverUri = new URI("ws://localhost:8080");

        webSocketClient = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Connected to WebSocket Server");
                send("queueStatus");
            }

            @Override
            public void onMessage(String message) {
                Platform.runLater(() -> {
                    queueStatusArea.appendText(message + "\n");
                    updateInfoWindow(message);
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

    private void sendWebSocketMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        } else {
            System.err.println("WebSocket is not connected.");
        }
    }

    private void updateInfoWindow(String message) {
        if (message.startsWith("Serving:")) {
            currentlyServingLabel.setText("Currently Serving: " + message.replace("Serving: ", ""));
        } else if (message.startsWith("Queue Status:")) {
            String queueStatus = message.replace("Queue Status: ", "").trim();

            // Check if the queueStatus contains any actual tickets
            if (queueStatus.isEmpty()) {
                // If no queue data exists, treat it as empty
                nextInQueueLabel.setText("Next in Queue: None");
                totalQueueLabel.setText("Total Tickets in Queue: 0");
            } else {
                // Split the queue data into individual tickets
                String[] queueParts = queueStatus.split(", ");

                // Fix: Check for the correct number of items
                String nextInQueue = queueParts.length > 1 ? queueParts[0] : "None";
                nextInQueueLabel.setText("Next in Queue: " + nextInQueue);

                // Remove any unwanted characters (like brackets or extra spaces)
                if (nextInQueue.contains("[")) {
                    nextInQueue = nextInQueue.replace("[", "").replace("]", "");
                }

                // Display the total number of tickets in the queue
                int queueSize = queueParts.length; // Count the number of items in the queue
                totalQueueLabel.setText("Total Tickets in Queue: " + queueSize);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
