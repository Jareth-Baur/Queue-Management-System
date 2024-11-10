/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xemacscode.websockettest;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author Admin
 */
public class SimpleClient extends WebSocketClient {

    // Constructor: define the server URI to connect to
    public SimpleClient(URI serverUri) {
        super(serverUri);
    }

    // Triggered when the connection is opened
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
        // Send a message to the server
        send("Hello, Server!");
    }

    // Triggered when a message is received from the server
    @Override
    public void onMessage(String message) {
        System.out.println("Message from server: " + message);
    }

    // Triggered when the connection is closed
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from server: " + reason);
    }

    // Triggered when there's an error
    @Override
    public void onError(Exception ex) {
        System.err.println("Error occurred: " + ex.getMessage());
    }

    public static void main(String[] args) {
        try {
            // Create a WebSocket client and connect to the server
            URI serverUri = new URI("ws://localhost:8887");
            SimpleClient client = new SimpleClient(serverUri);

            // Connect to the server
            client.connectBlocking(); // Wait for the connection to be established

            // Send a test message
            client.send("Hello from the WebSocket client!");

            // Keep the client running
            Thread.sleep(5000); // Keep connection open for 5 seconds to receive messages

            // Close the connection
            client.close();
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }
}