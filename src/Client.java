import java.io.*;
import java.net.*;
import java.util.Scanner;
//now it's in new branch
/**
 * Написать текстовый чат для двух пользователей на сокетах.
 * Чат должен быть реализован по принципу клиент-сервер.
 * Один пользователь находится на сервере, второй --- на клиенте.
 * Адреса и порты задаются через командную строку:
 * клиенту --- куда соединяться, серверу --- на каком порту слушать.
 * При старте программы выводится текстовое приглашение,
 * в котором можно ввести одну из следующих команд:
 * 1.	Задать имя пользователя (@name Vasya)
 * 2.	Послать текстовое сообщение (Hello)
 * 3.	Выход (@quit)
 * Принятые сообщения автоматически выводятся на экран.
 * Программа работает по протоколу UDP.
 */
public class Client {
    private DatagramSocket socket;
    private String name, serverName;
    private BufferedReader keyboard;
    private Thread listener;

    public Client(String adr, int port) throws SocketException, UnknownHostException {
        InetAddress ipAddress = InetAddress.getByName(adr);
        socket = new DatagramSocket(port, ipAddress); // создаем сокет используя IP-адрес и порт сервера
        keyboard = new BufferedReader(new InputStreamReader(System.in));
        listener = new Thread(new FromServer());
        listener.start();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome!");
        System.out.print("Port # ");
        int port = new Scanner(System.in).nextInt();// порт, к которому привязывается сервер
        //String address = "localhost";//"127.0.0.1"// это IP-адрес компьютера, где исполняется наша серверная программа.
        new Client("localhost", port).run();
    }

    private void socketClose() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {//отправляет на сервер
        System.out.print("@name ");
        name = new Scanner(System.in).nextLine();
        try {
            byte[] m = name.getBytes();
            socket.send(new DatagramPacket(m, m.length));
            while (true) {
                String line;
                line = keyboard.readLine();
                //вот здесь он и ждёт ввода после закрытия со стороны сервера
                if (socket.isClosed())
                    break;
                byte[] mes = line.getBytes();
                socket.send(new DatagramPacket(mes, mes.length));
                if (line.equals("@quit")) {
                    socketClose();
                    break;
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
    public String read() throws IOException {//прием сообщений
        byte[] buf = new byte[1000];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        socket.receive(p);
        return new String(p.getData());
    }

    private class FromServer implements Runnable {//принимает сообщения

        public void run() {
            try {
                serverName = read();
                while (true) {
                    String line;
                    line = read(); // ждем пока сервер отошлет строку текста.
                    if (line.equals("@quit")) {
                        System.out.println("server is quited");
                        break;
                    }
                    System.out.println(serverName + ": " + line);
                }
            } catch (IOException e) {
                socketClose();
            } finally {
                socketClose();
            }
        }
    }

}
