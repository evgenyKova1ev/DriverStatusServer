import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Client {
    public static void main(String[] args) {
        while (true) {
            process();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void process() {
        SocketAddress address = new InetSocketAddress("192.168.0.106", 5959);

        Socket socket = new Socket();

        try {
            socket.connect(address);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream())) {

            objOut.writeObject("drivers");
            objOut.flush();

            String str = (String) objIn.readObject();
            System.out.println(str);


            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
