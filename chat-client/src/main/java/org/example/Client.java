package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private final Socket socket;
    private final String name;

    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    public Client(Socket socket, String userName) {
        this.socket = socket;
        name = userName;

        try {
            // c помощью bufferedWriter и bufferedReader легко передовать
            // по сети текстовые сообщения
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    /**
     * Слушатель для входящих сообщений
     */
    public void listenForMessage(){
        //все входящие сообщения будут слушаться
        //в отдельном потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                //создаем переменную для сообщения
                String message;
                //вечный цикл пока есть соединение
                while (socket.isConnected()){
                    //может возникнуть любая ошибка поэтому в try
                    try {
                        message = bufferedReader.readLine();
                        System.out.println(message);
                    }
                    catch (IOException e){
                        // при возникновении исключения можно приложение
                        //сделать гибким, н-мер тут повторно сделать
                        //переподключение, сейчас просто завершаем поток
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    /**
     * Отправить сообщение
     */
    public void sendMessage(){
        try {
            //первое после подключения к серверу отправляем имя
            bufferedWriter.write(name);
            //переход на сл строку
            bufferedWriter.newLine();
            //принудительное отправление сообщения на сервер из потока
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                bufferedWriter.write(name + ": " + message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Закрыть соединение в случае исключения
     */
    private void closeEverything(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter){
        try {
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter !=null){
                bufferedWriter.close();
            }
            if(socket!=null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
