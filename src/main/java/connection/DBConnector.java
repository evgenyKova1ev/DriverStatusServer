package connection;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DBConnector {
    private Connection connection;

    private Server server;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public DBConnector(Server server) {
        this.server = server;

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://88.85.89.17:5432/ptk", "postgres", "destroyerpostgres");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void ping() {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(DBQuery.selectDriversCount());
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Message addNewDriver(String nameAndCarNumber) {
        Message message = new Message();

        try (Statement statement = connection.createStatement()) {
//            ResultSet resultSet = statement.executeQuery(DBQuery.selectDriver(nameAndCarNumber));
//            resultSet.next();
            statement.execute(DBQuery.insertNewDriver(nameAndCarNumber));

            try (ResultSet rs = statement.executeQuery(DBQuery.selectDriver(nameAndCarNumber.split("/")[0]))) {
                rs.next();
                message.setMsg("addOK");
                Message generalMessage = new Message("newDriver", new ArrayList<>());
                generalMessage.getData().add(generalMessage.new Data(Integer.toString(rs.getInt("id")),
                        rs.getString("car_number"),
                        rs.getString("name"),
                        rs.getString("data"),
                        rs.getString("status")));
                server.getMessageQueue().add(generalMessage);

            }


        } catch (Exception e) {
            e.printStackTrace();
            message.setMsg("addERROR");
        }
        return message;
    }

    public Message deleteDriver(String name) {
        Message message = new Message();

        try (Statement statement = connection.createStatement()) {
            int counter = statement.executeUpdate(DBQuery.deleteDriver(name));
            if (counter != 0) {
                message.setMsg("deleteOK");
                Message generalMessage = new Message("deleteDriver", new ArrayList<>());
                Message.Data data = generalMessage.new Data();
                data.setName(name);
                generalMessage.getData().add(data);
                server.getMessageQueue().add(generalMessage);
            } else
                message.setMsg("deleteERROR");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }



    public Message loadDriversForAndroid() {
        Message message = new Message("drivers", new ArrayList<>());
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(DBQuery.selectIDAndNames())) {
                while (rs.next()) {
                    Message.Data data = message.new Data();
                    data.setId(Integer.toString(rs.getInt("id")));
                    data.setName(rs.getString("name"));
                    message.getData().add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return message;
    }

    public Message loadDriverHistory(String id) {
        Message message = new Message("driverHistory", new ArrayList<>());
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(DBQuery.selectDriverHistory(id))) {
                while (rs.next()) {
                    Message.Data data = message.new Data();
                    data.setDate(rs.getString("data"));
                    data.setStatus(rs.getString("status"));
                    message.getData().add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return message;
    }

    public Message loadDriversForDesktop() {
        Message message = new Message("drivers", new ArrayList<>());
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(DBQuery.selectDrivers())) {
                while (rs.next()) {
                    message.getData().add(message.new Data(Integer.toString(rs.getInt("id")),
                            rs.getString("car_number"),
                            rs.getString("name"),
                            rs.getString("data"),
                            rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return message;
    }

//    public StringBuilder updateDrivers(int quantity) {
//        StringBuilder stringBuilder = new StringBuilder();
//        try (Statement statement = connection.createStatement()) {
//            try (ResultSet rs = statement.executeQuery(DBQuery.selectDriversCount())) {
//                rs.next();
//                if (rs.getInt("count") == quantity) {
//                    try (ResultSet rsFirst = statement.executeQuery(DBQuery.selectDriversWithoutNames())) {
//                        while (rsFirst.next()) {
//                            stringBuilder.append(rsFirst.getInt("id"))
//                                    .append("!")
//                                    .append(rsFirst.getString("data"))
//                                    .append("!")
//                                    .append(rsFirst.getString("status"))
//                                    .append("/");
//                        }
//                    }
//                } else {
//                    try (ResultSet rsFirst = statement.executeQuery(DBQuery.selectDrivers())) {
//                        while (rsFirst.next()) {
//                            stringBuilder.append(rsFirst.getInt("id"))
//                                    .append("!")
//                                    .append(rs.getString("name"))
//                                    .append("!")
//                                    .append(rsFirst.getString("data"))
//                                    .append("!")
//                                    .append(rsFirst.getString("status"))
//                                    .append("/");
//                        }
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return stringBuilder;
//    }

    public Message updateStatus(Message message) {
        Message response = new Message();

        try (Statement statement = connection.createStatement()) {

            String currentDate = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Moscow")).format(formatter);

            statement.execute(DBQuery.updateStatus(message.getId(), message.getStatus(), currentDate));
            statement.execute(DBQuery.insertStatus(message.getId(), message.getStatus(), currentDate));

            response.setMsg("statusOK");
            response.setStatus(message.getStatus());

            Message generalMessage = new Message("status", new ArrayList<>());

            Message.Data generalData = generalMessage.new Data();
            generalData.setId(message.getId());
            generalData.setStatus(message.getStatus());
            generalData.setDate(currentDate);

            generalMessage.getData().add(generalData);

            server.getMessageQueue().add(generalMessage);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }
}
