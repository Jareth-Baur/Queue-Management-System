import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DB extends Application {

    // Database connection details (replace with your actual credentials)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/queue_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @Override
    public void start(Stage stage) throws Exception {
        // Load data from the database
        Map<LocalDate, Map<String, Integer>> dataByDayAndStatus = loadDataFromDatabase();

        // Create charts
        BarChart<String, Number> groupedBarChart = createGroupedBarChart(dataByDayAndStatus);
        StackedAreaChart<String, Number> stackedAreaChart = createStackedAreaChart(dataByDayAndStatus);
        PieChart pieChart = createPieChart(dataByDayAndStatus);
        ScatterChart<Number, Number> scatterChart = createScatterChart(dataByDayAndStatus);

        // Create dashboard layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.add(groupedBarChart, 0, 0);
        grid.add(stackedAreaChart, 1, 0);
        grid.add(pieChart, 0, 1);
        grid.add(scatterChart, 1, 1);

        Scene scene = new Scene(grid, 1150, 800);
        stage.setScene(scene);
        stage.setTitle("Ticket Dashboard");
        stage.show();
    }

    private Map<LocalDate, Map<String, Integer>> loadDataFromDatabase() {
        Map<LocalDate, Map<String, Integer>> data = new TreeMap<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT DATE(created_at) as created_date, COUNT(*) as count, status FROM tickets GROUP BY created_date, status")) {
            while (resultSet.next()) {
                LocalDate date = LocalDate.parse(resultSet.getString("created_date"), DateTimeFormatter.ISO_DATE);
                int count = resultSet.getInt("count");
                String status = resultSet.getString("status");

                Map<String, Integer> dailyData = data.computeIfAbsent(date, k -> new HashMap<>());
                dailyData.put(status, dailyData.getOrDefault(status, 0) + count);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return new TreeMap<>(); //Return empty map in case of error
        }
        return data;
    }

    private BarChart<String, Number> createGroupedBarChart(Map<LocalDate, Map<String, Integer>> data) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Tickets");
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Ticket Status Over Time");

        data.keySet().stream().sorted().forEach(date -> {
            XYChart.Series<String, Number> seriesPending = new XYChart.Series<>();
            seriesPending.setName("PENDING");
            XYChart.Series<String, Number> seriesServed = new XYChart.Series<>();
            seriesServed.setName("SERVED");

            seriesPending.getData().add(new XYChart.Data<>(date.format(DateTimeFormatter.ISO_DATE), data.get(date).getOrDefault("PENDING", 0)));
            seriesServed.getData().add(new XYChart.Data<>(date.format(DateTimeFormatter.ISO_DATE), data.get(date).getOrDefault("SERVED", 0)));

            chart.getData().addAll(seriesPending, seriesServed);
        });
        return chart;
    }

    private StackedAreaChart<String, Number> createStackedAreaChart(Map<LocalDate, Map<String, Integer>> data) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Tickets");
        StackedAreaChart<String, Number> chart = new StackedAreaChart<>(xAxis, yAxis);
        chart.setTitle("Ticket Trend with Status Breakdown");

        XYChart.Series<String, Number> seriesPending = new XYChart.Series<>();
        seriesPending.setName("PENDING");
        XYChart.Series<String, Number> seriesServed = new XYChart.Series<>();
        seriesServed.setName("SERVED");

        data.keySet().stream().sorted().forEach(date -> {
            seriesPending.getData().add(new XYChart.Data<>(date.format(DateTimeFormatter.ISO_DATE), data.get(date).getOrDefault("PENDING", 0)));
            seriesServed.getData().add(new XYChart.Data<>(date.format(DateTimeFormatter.ISO_DATE), data.get(date).getOrDefault("SERVED", 0)));
        });

        chart.getData().addAll(seriesPending, seriesServed);
        return chart;
    }

    private PieChart createPieChart(Map<LocalDate, Map<String, Integer>> data) {
        Map<String, Integer> totalCounts = new HashMap<>();
        data.values().forEach(dailyData -> dailyData.forEach((status, count) -> totalCounts.put(status, totalCounts.getOrDefault(status, 0) + count)));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        totalCounts.forEach((status, count) -> pieChartData.add(new PieChart.Data(status, count)));

        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Overall Ticket Status Distribution");
        return pieChart;
    }

    private ScatterChart<Number, Number> createScatterChart(Map<LocalDate, Map<String, Integer>> data) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Day Number (since start)");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Tickets for the Day");
        ScatterChart<Number, Number> chart = new ScatterChart<>(xAxis, yAxis);
        chart.setTitle("Ticket Count vs. Day");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        int dayCount = 0;
        for (LocalDate date : data.keySet().stream().sorted().toList()) {
            int totalCount = data.get(date).values().stream().mapToInt(Integer::intValue).sum();
            series.getData().add(new XYChart.Data<>(++dayCount, totalCount));
        }
        chart.getData().add(series);
        return chart;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
