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
    private ServerSocket ss;
    private Socket socket;
    private String name, clientName;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedReader keyboard;
    private Thread sender;

    public Server(int port) throws IOException {
        ss = new ServerSocket(port);
      }

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome!");
        System.out.print("Port # ");
        int port = new Scanner(System.in).nextInt();
        new Server(port).run(); //порт задаёт пользователь
    }

    public void run() {//принимает сообщения
        System.out.print("@name ");
        name = new Scanner(System.in).nextLine();
        // name = new Scanner(name).useDelimiter("@name\\s*").next();
        try {
            socket = ss.accept(); // заставляем сервер ждать подключений и выводим сообщение когда кто-то связался с сервером
            if (socket != null)
                System.out.println("Please type");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            sender = new Thread(new Sender());
            sender.start();
            // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиенту
            // Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
            clientName = in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("123");
        }
        while (!socket.isClosed()) {
            String line;
            try {
                line = in.readUTF(); // ожидаем пока клиент пришлет строку текста.
                System.out.println(clientName + ": " + line);
            } catch (EOFException e) {
                System.out.println("client is quited");
                close();
            } catch (IOException e) {
                if ("Socket closed".equals(e.getMessage()))
                    break;
                else e.printStackTrace();
                System.out.println("56++");
            }
        }
    }

    private void close() {
        try {
            ss.close();
            socket.close();
            sender.interrupt();//??????????????
        } catch (IOException e) {
            System.out.println("465");
            e.printStackTrace();
        }
    }

    private class Sender implements Runnable {//отправляет сообщения

        Sender() {
            keyboard = new BufferedReader(new InputStreamReader(System.in));
        }

        public void run() {
            try {
                out.writeUTF(name);
            } catch (IOException e) {
                System.out.println("45++");
                e.printStackTrace();
            }
            while (!ss.isClosed()) {
                String line;
                try {
                    // System.out.print(name + ": ");
                    line = keyboard.readLine();
                    if (line.equals("@quit")) {
                        close();
                        break;
                    }
                    if (line != null || !socket.isClosed()) {
                        out.writeUTF(line);
                        out.flush(); // заставляем поток закончить передачу данных.
                    }
                } catch (Exception e) {
                    System.out.println("789");
                    e.printStackTrace();
                    close();
                }
            }
        }
    }
}
