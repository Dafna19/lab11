import java.io.*;
import java.net.*;
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
public class Client {
    private Socket socket;
    private String name, serverName;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedReader keyboard;
    private Thread listener, mainThread;

    public Client(String adr, int port) throws IOException {
        InetAddress ipAddress = InetAddress.getByName(adr); // создаем объект который отображает вышеописанный IP-адрес
        socket = new Socket(ipAddress, port); // создаем сокет используя IP-адрес и порт сервера
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        keyboard = new BufferedReader(new InputStreamReader(System.in));
        listener = new Thread(new FromServer());
        listener.start();// создаем и запускаем нить асинхронного чтения из сокета
    }

    private class FromServer implements Runnable {//принимает сообщения

        public void run() {
            try {
                if (!socket.isClosed())
                    serverName = in.readUTF();
            } catch (IOException e) {
                if ("Socket closed".equals(e.getMessage()))
                    socketClose();
            }
            while (!socket.isClosed()) {
                String line;
                try {
                    line = in.readUTF(); // ждем пока сервер отошлет строку текста.
                    System.out.println(serverName + ": " + line);
                } catch (EOFException e) {
                    System.out.println("server is quited");
                    socketClose();
                } catch (IOException e) {
                    if ("Socket closed".equals(e.getMessage()))
                        break;
                    else e.printStackTrace();
                }
            }
        }
    }

    private void socketClose() {
        try {
            socket.close();
            mainThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {//отправляет на сервер
        mainThread = Thread.currentThread();
        System.out.print("@name ");
        name = new Scanner(System.in).nextLine();
      //  name = new Scanner(name).useDelimiter("@name\\s*").next();
        try {
            out.writeUTF(name);
            while (true) {
                String line;
                // System.out.print(name + ": ");
                line = keyboard.readLine();
                if (socket.isClosed())
                    break;
                if (line.equals("@quit")) {
                    listener.interrupt();
                    socketClose();
                    break;
                }
                out.writeUTF(line); // отсылаем введенную строку текста серверу.
                out.flush(); // заставляем поток закончить передачу данных.
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome!");
        System.out.print("Port # ");
        int port = new Scanner(System.in).nextInt();// порт, к которому привязывается сервер
        //String address = "localhost";//"127.0.0.1"// это IP-адрес компьютера, где исполняется наша серверная программа.
        new Client("localhost", port).run();
    }
}
