/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xemacscode.websockettest;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Admin
 */
public class MainServer extends Application{

    @Override
    public void start(Stage stage) throws Exception {
        VBox root = new VBox();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Server");
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
