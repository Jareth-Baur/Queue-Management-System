package dorsu.jareth.queue;

import dorsu.jareth.auth.DatabaseConnection;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static javafx.application.Application.launch;

public class TicketsTable extends Application {

    private final static int rowsPerPage = 20;
    private TableView<Ticket> table = createTable();
    private ObservableList<Ticket> data = FXCollections.observableArrayList();

    // Database connection details (replace with your actual credentials)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/queue_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private TableView<Ticket> createTable() {
        TableView<Ticket> table = new TableView<>();

        TableColumn<Ticket, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().id.get()));
        idColumn.setPrefWidth(150);

        TableColumn<Ticket, String> ticketNumberColumn = new TableColumn<>("Ticket Number");
        ticketNumberColumn.setCellValueFactory(p -> p.getValue().ticketNumber);
        ticketNumberColumn.setPrefWidth(150);

        TableColumn<Ticket, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(p -> p.getValue().status);
        statusColumn.setPrefWidth(250);

        TableColumn<Ticket, String> createdAtColumn = new TableColumn<>("Created At");
        createdAtColumn.setCellValueFactory(p -> p.getValue().formattedCreatedAt);
        createdAtColumn.setPrefWidth(250);

        TableColumn<Ticket, String> officeNameColumn = new TableColumn<>("Office Name");
        officeNameColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().officeName));
        officeNameColumn.setPrefWidth(250); // Adjust width as needed

        table.getColumns().addAll(idColumn, ticketNumberColumn, statusColumn, createdAtColumn, officeNameColumn);
        return table;
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, data.size());
        table.setItems(FXCollections.observableArrayList(data.subList(fromIndex, toIndex)));
        return new BorderPane(table);
    }

    @Override
    public void start(Stage stage) throws Exception {
        fetchDataFromDatabase();

        Pagination pagination = new Pagination((data.size() + rowsPerPage - 1) / rowsPerPage, 0);
        pagination.setPageFactory(this::createPage);

        Scene scene = new Scene(new BorderPane(pagination), 1020, 600);
        stage.setScene(scene);
        stage.setTitle("Queue Management System - Ticket Table");
        stage.show();
    }

    private void fetchDataFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(
                "SELECT t.id, t.ticket_number, t.status, t.created_at, o.name AS office_name "
                + "FROM tickets t JOIN offices o ON t.office_id = o.id"
        )) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String ticketNumber = resultSet.getString("ticket_number");
                String status = resultSet.getString("status");
                Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
                LocalDateTime createdAt = LocalDateTime.ofInstant(createdAtTimestamp.toInstant(), ZoneId.systemDefault());
                String officeName = resultSet.getString("office_name");
                data.add(new Ticket(id, ticketNumber, status, createdAt, officeName));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching data from database: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public static class Ticket {

        public SimpleObjectProperty<Integer> id;
        public SimpleStringProperty ticketNumber;
        public SimpleStringProperty status;
        public LocalDateTime createdAt;
        public SimpleStringProperty formattedCreatedAt;
        public String officeName; // Added officeName field

        public Ticket(int id, String ticketNumber, String status, LocalDateTime createdAt, String officeName) {
            this.id = new SimpleObjectProperty<>(id);
            this.ticketNumber = new SimpleStringProperty(ticketNumber);
            this.status = new SimpleStringProperty(status);
            this.createdAt = createdAt;
            this.formattedCreatedAt = new SimpleStringProperty(formatDateTime(createdAt));
            this.officeName = officeName;
        }

        private String formatDateTime(LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a"));
        }
    }
}
