import java.net.*;
import java.io.*;
import java.util.Scanner;

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

public class Server {
    private DatagramSocket socket;
    private String name = "server", clientName = "client";
    private BufferedReader keyboard;
    private Thread sender;
    private InetAddress ipAddress;
    private int port;

    public Server(int port) throws IOException {
        socket = new DatagramSocket(port);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome!");
        int port = Integer.parseInt(args[0]);
        new Server(port).run(); //порт задаёт пользователь
    }

    private String read() throws IOException {//прием сообщений
        byte[] buf = new byte[1000];
        //сделать, чтобы строка не была длиной 1000
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        socket.receive(p);
        ipAddress = p.getAddress();
        port = p.getPort();
        return new String(p.getData(), 0, p.getLength());
    }

    public void run() {//принимает сообщения
        try {
            clientName = read();
            //изначально сервер не знает, куда отправлять
            sender = new Thread(new Sender());
            sender.start();
            while (true) {
                String line;
                line = read(); // ожидаем пока клиент пришлет строку текста.
                if (line.equals("@quit")) {
                    System.out.println("client is quited");
                    break;
                }
                if (line.contains("@name"))
                    clientName = line.substring("@name".length() + 1);
                else
                    System.out.println(clientName + ": " + line);
            }
        } catch (IOException e) {
            socket.close();
        } finally {
            socket.close();
        }
    }

    private void send(String s) throws IOException {//отправляет сообщение
        byte[] m = s.getBytes();
        DatagramPacket p = new DatagramPacket(m, m.length, ipAddress, port);//адрес и порт сервера
        socket.send(p);
    }

    private class Sender implements Runnable {//отправляет сообщения

        Sender() {
            keyboard = new BufferedReader(new InputStreamReader(System.in));
        }

        public void run() {
            try {
                send(name);
                while (!socket.isClosed()) {
                    String line;
                    line = keyboard.readLine();
                    if (line != null && !socket.isClosed()) {
                        send(line);
                        if (line.equals("@quit")) {
                            socket.close();
                            break;
                        }
                        if (line.contains("@name"))
                            name = line.substring("@name".length() + 1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
