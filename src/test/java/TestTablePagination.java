
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

public class TestTablePagination extends Application {

    private final static int rowsPerPage = 1000;
    private TableView<Ticket> table = createTable();
    private ObservableList<Ticket> data = FXCollections.observableArrayList(); // Use ObservableList

    // Database connection details (replace with your actual credentials)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/queue_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";


    private TableView<Ticket> createTable() {
        TableView<Ticket> table = new TableView<>();

        TableColumn<Ticket, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().id.get()));
        idColumn.setPrefWidth(200);

        TableColumn<Ticket, String> ticketNumberColumn = new TableColumn<>("Ticket Number");
        ticketNumberColumn.setCellValueFactory(p -> p.getValue().ticketNumber);
        ticketNumberColumn.setPrefWidth(200);

        TableColumn<Ticket, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(p -> p.getValue().status);
        statusColumn.setPrefWidth(280);

        TableColumn<Ticket, Timestamp> createdAtColumn = new TableColumn<>("Created At");
        createdAtColumn.setCellValueFactory(p -> p.getValue().createdAt);
        createdAtColumn.setPrefWidth(340);

        table.getColumns().addAll(idColumn, ticketNumberColumn, statusColumn, createdAtColumn);
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
        // Fetch data from the database
        fetchDataFromDatabase();

        Pagination pagination = new Pagination((data.size() + rowsPerPage - 1) / rowsPerPage, 0); //Corrected pagination calculation
        pagination.setPageFactory(this::createPage);

        Scene scene = new Scene(new BorderPane(pagination), 1024, 768);
        stage.setScene(scene);
        stage.setTitle("Ticket Table");
        stage.show();
    }

    private void fetchDataFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, ticket_number, status, created_at FROM tickets")) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String ticketNumber = resultSet.getString("ticket_number");
                String status = resultSet.getString("status");
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                data.add(new Ticket(id, ticketNumber, status, createdAt));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching data from database: " + e.getMessage());
            //Consider adding more robust error handling here
        }
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public static class Ticket {
        public SimpleObjectProperty<Integer> id;
        public SimpleStringProperty ticketNumber;
        public SimpleStringProperty status;
        public SimpleObjectProperty<Timestamp> createdAt;

        public Ticket(int id, String ticketNumber, String status, Timestamp createdAt) {
            this.id = new SimpleObjectProperty<>(id);
            this.ticketNumber = new SimpleStringProperty(ticketNumber);
            this.status = new SimpleStringProperty(status);
            this.createdAt = new SimpleObjectProperty<>(createdAt);
        }
    }
}
