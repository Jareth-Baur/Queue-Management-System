/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package dorsu.jareth.queue;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Talong PC
 */
public class MainDashboardController implements Initializable {

    @FXML
    private AnchorPane officesView;
    @FXML
    private TableView<?> officesTable;
    @FXML
    private TableView<?> ticketsTable;
    @FXML
    private Button editOfficeButton;
    @FXML
    private Button deleteOfficeButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
