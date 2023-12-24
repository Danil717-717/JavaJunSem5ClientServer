package org.example;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class MainServer {
    public static void main(String[] args) {
        try {
            // клиент работает с портом 1400, соответственно
            // сервер тоже должен слушать порт 1400
            ServerSocket serverSocket = new ServerSocket(1400);

            // так же как и на клиенте создаем экземпляр класса
            // обвертки и вызываем метод запуска сервера
            Server server = new Server(serverSocket);
            server.runServer();

        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
