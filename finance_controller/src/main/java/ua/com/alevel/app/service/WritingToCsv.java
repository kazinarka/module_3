package ua.com.alevel.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class WritingToCsv {

    private static final Logger LOGGER = LoggerFactory.getLogger("logs");
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public void exportInCSV(List<String> config) {

        Properties props = new Properties();

        try (InputStream input = WritingToCsv.class.getResourceAsStream("jdbc.properties")) {
            props.load(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        String username = config.get(0);
        String password = config.get(1);
        int userId = Integer.parseInt(config.get(2));
        String url = props.getProperty("url");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            ResultSet resultSet;
            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from users where id = ?")) {
                preparedStatement.setInt(1, userId);
                resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    LOGGER.error("No such user. Wrong id: " + userId);
                    throw new RuntimeException("No such user. Input was wrong");
                } else {
                    System.out.println("User signed in");
                    LOGGER.info("User signed in. Id: " + userId);
                }
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from accounts where user_id = ?")) {
                preparedStatement.setInt(1, userId);
                resultSet = preparedStatement.executeQuery();
                System.out.println("Choose account:");

                while (resultSet.next()) {
                    System.out.println("id:" + resultSet.getInt("id") + ", amount: " + resultSet.getLong("amount"));
                }

                int index = Integer.parseInt(reader.readLine());

                System.out.println("Choose the type of sorting by time\n" +
                        "1 >> ascending\n" +
                        "2 >> descending");

                String choice = reader.readLine();
                switch (choice) {
                    case "1": {
                        writer(connection, index, 1);
                    }
                    case "2": {
                        writer(connection, index, 2);
                    }
                    default: {
                        System.out.println("Wrong input");
                        LOGGER.warn("Incorrect input while choosing sorting type");
                    }
                }
            } catch (IOException e) {
                System.err.println("Something went wrong, please try again");
            }
        } catch (SQLException e) {
            LOGGER.error("No such user and accounts");
            throw new RuntimeException(e);
        }
    }

    private void writer(Connection connection, int index, int sortingType) {
        String query = "select operations.id, operations.time, operations.value, operations.account_id, categories.categories, categories.description " +
                "from operations join categories on categories.id = operations.category_id " +
                "where operations.account_id = ? and operations.time between ? and ?";

        System.out.println("Input start time for operations list (e.g. 31/07/2021 00:00)");
        String from, to;
        Date dateFrom, dateTo;
        Instant iFrom, iTo;

        try {
            from = reader.readLine();
            LOGGER.info("From time: " + from);
            dateFrom = new SimpleDateFormat("dd/MM/yyyy hh:mm").parse(from);
            iFrom = dateFrom.toInstant();

            System.out.println("Input finish time for operations list (e.g. 31/07/2021 00:00)");
            to = reader.readLine();
            LOGGER.info("To time: " + to);
            dateTo = new SimpleDateFormat("dd/MM/yyyy hh:mm").parse(to);
            iTo = dateTo.toInstant();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        if (sortingType == 1) {
            query += " order by operations.time asc";
        } else {
            query += " order by operations.time desc";
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, index);
            preparedStatement.setTimestamp(2, Timestamp.from(iFrom));
            preparedStatement.setTimestamp(3, Timestamp.from(iTo));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("info.csv", false))) {
                ResultSet resultSet = preparedStatement.executeQuery();
                writer.write("operation id, time, amount, category, description\n");
                while (resultSet.next()) {
                    writer.write(
                            resultSet.getInt("id") + "," +
                                    resultSet.getTimestamp("time") + "," +
                                    resultSet.getLong("difference") + "," +
                                    resultSet.getString("categories") + "," +
                                    resultSet.getString("description") + "\n");
                }
            }
        } catch (SQLException | IOException e) {
            LOGGER.error("Failed writing in csv file");
            throw new RuntimeException(e);
        }
    }
}