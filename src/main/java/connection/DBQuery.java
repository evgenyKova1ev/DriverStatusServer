package connection;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DBQuery {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String selectDriversWithoutNames() {
        return "SELECT id, data, status FROM drivers";
    }

    public static String selectDriversCount() {
        return "SELECT COUNT(*) FROM drivers";
    }

    public static String insertNewDriver(String nameAndCarNumber) {
        String[] data = nameAndCarNumber.split("/");
        return "INSERT INTO drivers (name, car_number) VALUES ('" + data[0] + "', '" + data[1] + "')";
    }

    public static String deleteDriver(String name) {
        return "DELETE FROM drivers WHERE name = '" + name + "'";
    }

    public static String selectDriver(String name) {
        return "SELECT * FROM drivers WHERE name = '" + name + "'";
    }

    public static String selectDrivers() {
        return "SELECT * FROM drivers";
    }

    public static String selectDriverHistory(String id) {
        return "SELECT * FROM status_history WHERE driver_id = " + id;
    }

    public static String selectIDAndNames() {
        return "SELECT id, name FROM drivers";
    }

    public static String updateStatus(String id, String status, String date) {
        return "UPDATE drivers SET status = '" + status + "' , data = '" + date + "' WHERE id = " + id;
    }

    public static String insertStatus(String id, String status, String date) {
        return "INSERT INTO status_history (driver_id, status, data) VALUES (" + id + ", '" + status + "', '" + date + "')";
    }

    private static String getCurrentData() {
        return ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Moscow")).format(formatter);
    }
}
