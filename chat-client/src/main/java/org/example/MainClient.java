package org.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) {

        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите свое имя: ");
            //Укажите свое имя
            String name = scanner.nextLine();
            Socket socket = new Socket("localhost", 1400);

            //создаем экземпляр класса обвертки
            Client client = new Client(socket,name);

            // порт 1400 используем только для подключения
            //далее используется очередной стек выделенных портов
            InetAddress inetAddress = socket.getInetAddress();
            System.out.println("InetAddress: " + inetAddress);
            String remoteIp = inetAddress.getHostAddress();
            System.out.println("Remote IP: " + remoteIp);
            System.out.println("LocalPort: " + socket.getLocalPort());

            //используем методы класса обвертки
            client.listenForMessage();
            client.sendMessage();


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}