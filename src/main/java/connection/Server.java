package connection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;

public class Server {

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DBConnector connector = new DBConnector(this);

    private final Set<Connection> connections = new CopyOnWriteArraySet<>();
    private final BlockingDeque<Message> messageQueue = new LinkedBlockingDeque<>();
    private final BlockingDeque<Message> mailQueue = new LinkedBlockingDeque<>();

    private void start() {
        new Thread(new Writer()).start();

        new Thread(new Ping()).start();

        try(ServerSocket serverSocket = new ServerSocket(9351)) {
            while (true) {
                Socket socket = serverSocket.accept();

                Connection connection = new Connection(socket);

                connections.add(connection);

                new Thread(new Reader(socket, connection)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Ping implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(60_000);
                    connector.ping();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Reader implements Runnable {
        private final Socket socket;

        Connection connection;

        private Reader(Socket socket, Connection connection) {
            this.socket = socket;
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Message msg = (Message) connection.objIn.readObject();

                    if (msg.getMsg() == null)
                        continue;

                    switch (msg.getMsg()) {
                        case "selectAllDrivers":
                            sendMessage(connection, connector.loadDriversForDesktop());
                            break;
                        case "driverHistory":
                            sendMessage(connection, connector.loadDriverHistory(msg.getId()));
                            break;
                        case "add":
                            sendMessage(connection, connector.addNewDriver(msg.getStatus()));
                            break;
                        case "delete":
                            sendMessage(connection, connector.deleteDriver(msg.getStatus()));
                            break;
                        case "driversForAndroid":
                            sendMessage(connection, connector.loadDriversForAndroid());
                            break;
                        case "status":
                            sendMessage(connection, connector.updateStatus(msg));
                            break;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                connections.removeIf(connection -> connection.socket == socket);
                try {
                    connection.objIn.close();
                    connection.objOut.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Connection {
        Socket socket;
        ObjectOutputStream objOut;
        ObjectInputStream objIn;

        Connection(Socket socket) {
            this.socket = socket;
            try {
                objOut = new ObjectOutputStream(socket.getOutputStream());
                objIn = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Writer implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Message msg = messageQueue.take();

                    for (Connection connection : connections) {
                        try {
                            sendMessage(connection, msg);
                        }
                        catch (Exception e) {
                            System.err.printf("Error sending message %s to %s\n", msg, connection.socket);

                            connections.remove(connection);

                            try {
                                connection.socket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void sendMessage(Connection connection, Message message) {
        try {
            connection.objOut.writeObject(message);
            connection.objOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BlockingDeque<Message> getMessageQueue() {
        return messageQueue;
    }

    public static void main(String[] args) {

        Server server = new Server();

        server.start();

//        try {
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("test"));
//            Message message = new Message();
//            message.setDate(new ArrayList<>());
//            message.getDate().add(message.new Data());
//
//            objectOutputStream.writeObject(new Message());
//            objectOutputStream.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

//        System.out.println(ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Moscow")).format(formatter));
//        try {
//            try (ServerSocket ssocket = new ServerSocket(5959)) {
//                while (true) {
//                    socket = ssocket.accept();
//
//                    System.out.println(socket.getInetAddress());
//
//                    process();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void process() {
//        try (ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
//             ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream())) {
//
//            Message msg = (Message) objIn.readObject();
//
//            System.out.println(msg.getMsg());

//            String response;

//            if (msg.split("!")[0].equals("driverHistory")) {
//                response = connector.loadDriverHistory(msg.split("!")[1]).toString();
//                objOut.writeObject(response);
//                objOut.flush();
//                return;
//            } else if (msg.split("!")[0].equals("updateDrivers")) {
//                response = connector.updateDrivers(Integer.parseInt(msg.split("!")[1])).toString();
//                objOut.writeObject(response);
//                objOut.flush();
//                return;
//            }
//
//
//            switch (msg) {
//                case "drivers":
//                    response = connector.loadDriversForAndroid().toString();
//                    break;
//                case "selectAllDrivers":
//                    response = connector.loadDriversForDesktop().toString();
//                    break;
//                default:
//                    response = connector.updateStatus(msg);
//                    break;
//            }



//            objOut.writeObject(new Message("OK"));
//            objOut.flush();
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
