import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketQueueTableView extends Application { // Added Application

    private TableView<Ticket> ticketTableView;

    private void initializeColumns() {
        TableColumn<Ticket, String> ticketNumberColumn = new TableColumn<>("Ticket Number");
        ticketNumberColumn.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));
        ticketNumberColumn.setSortable(true);

        TableColumn<Ticket, String> officeNameColumn = new TableColumn<>("Office Name");
        officeNameColumn.setCellValueFactory(new PropertyValueFactory<>("officeName"));
        officeNameColumn.setSortable(true);

        TableColumn<Ticket, LocalDateTime> timeAddedColumn = new TableColumn<>("Time Added");
        timeAddedColumn.setCellValueFactory(new PropertyValueFactory<>("timeAdded"));
        timeAddedColumn.setSortable(true);

        ticketTableView.getColumns().addAll(ticketNumberColumn, officeNameColumn, timeAddedColumn);
    }

    private void populateData() {
        ObservableList<Ticket> ticketData = FXCollections.observableArrayList(
                new Ticket("Ticket-123", "Office A", LocalDateTime.now().minusMinutes(10)),
                new Ticket("Ticket-456", "Office A", LocalDateTime.now().minusMinutes(5)),
                new Ticket("Ticket-789", "Office B", LocalDateTime.now().minusMinutes(20)),
                new Ticket("Ticket-012", "Office C", LocalDateTime.now().minusMinutes(30))
        );
        ticketTableView.setItems(ticketData);
    }

    public TableView<Ticket> getTicketTableView() {
        return ticketTableView;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        ticketTableView = new TableView<>();
        initializeColumns();
        populateData();

        Scene scene = new Scene(ticketTableView, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static class Ticket {
        private String ticketNumber;
        private String officeName;
        private LocalDateTime timeAdded;

        public Ticket(String ticketNumber, String officeName, LocalDateTime timeAdded) {
            this.ticketNumber = ticketNumber;
            this.officeName = officeName;
            this.timeAdded = timeAdded;
        }

        public String getTicketNumber() {
            return ticketNumber;
        }

        public String getOfficeName() {
            return officeName;
        }

        public LocalDateTime getTimeAdded() {
            return timeAdded;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
