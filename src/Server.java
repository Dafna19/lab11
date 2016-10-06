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
/*
* SERVERSOCKET?
* UDP
* */
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
            socket = ss.accept(); // заставляем сервер ждать подключений и выводим сообщение, когда кто-то связался с сервером
            //подключились, всё норм
            System.out.println("Please type");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            sender = new Thread(new Sender());
            sender.start();
            // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиенту
            // Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
            clientName = in.readUTF();
            try {
                while (true) {
                    String line;
                    line = in.readUTF(); // ожидаем пока клиент пришлет строку текста.
                    if (line.equals("@quit")) {
                        System.out.println("client is quited");
                        break;
                    }
                    System.out.println(clientName + ": " + line);
                    // catch (EOFException e) {
                    //close();
                    //} catch (IOException e) {
                    //if ("Socket closed".equals(e.getMessage()))
                    //break;
                    //else e.printStackTrace();
                    //}
                }
            } catch (SocketException e) {//socket closed
                // e.printStackTrace();
                close();
            }
        } catch (IOException e) {//внешний
            e.printStackTrace();
        } catch (Exception v) {
            System.out.println(v.getMessage());
        } finally {
            close();
        }
    }

    private void close() {
        try {
            ss.close();
            socket.close(); //если он ещё не открылся - NullPointerException
        } catch (IOException e) {
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
                e.printStackTrace();
            }
            while (!ss.isClosed()) {
                String line;
                try {
                    // System.out.print(name + ": ");
                    line = keyboard.readLine();
                    if (line != null && !socket.isClosed()) {
                        out.writeUTF(line);
                        out.flush(); // заставляем поток закончить передачу данных.
                        if (line.equals("@quit")) {
                            close();
                            break;
                        }
                    }
                } catch (Exception e) {
                    if ("Socket closed".equals(e.getMessage())) {
                        close();
                        break;
                    }
                    e.printStackTrace();
                    close();
                }
            }
        }
    }
}
