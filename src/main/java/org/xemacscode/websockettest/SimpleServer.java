/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xemacscode.websockettest;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author Admin
 */
public class SimpleServer extends WebSocketServer  {

    // Constructor: set WebSocket server address and port
    public SimpleServer(InetSocketAddress address) {
        super(address);
    }
   
    
    // Triggered when a new client connects
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from: " + conn.getRemoteSocketAddress());
        conn.send("Welcome to the WebSocket server!"); // Send a welcome message to the client
    }

    // Triggered when a client disconnects
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress() + " - Reason: " + reason);
    }

    // Triggered when a message is received from a client
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);
        conn.send("Server received: " + message); // Echo the message back to the client
    }

    // Triggered when there's an error
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error occurred: " + ex.getMessage());
        if (conn != null) {
            conn.close(); // Close the connection if there's an error
        }
    }

    // Optional: Triggered when the server starts
    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully!");
    }
    
     public static void main(String[] args) {
        // Define the server address and port
        InetSocketAddress address = new InetSocketAddress("localhost", 8887);
        SimpleServer server = new SimpleServer(address);
        
        // Start the server
        server.start();
        System.out.println("WebSocket server is listening on port 8887");

        // Keep the server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutting down WebSocket server...");
                server.stop();
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));
    }
    
}
