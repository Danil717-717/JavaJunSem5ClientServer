package org.example;


import java.io.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


public class ClientManager implements Runnable, Comparable<ClientManager> {
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    //список всех клиентов
    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //если имя успешно считано то
            name = bufferedReader.readLine();
            //то далее добавляем его в нашу коллекцию
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public ClientManager(Socket socket, String name) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //если имя успешно считано то
            name = bufferedReader.readLine();
            //то далее добавляем его в нашу коллекцию
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void run() {

        String massageFromClient;

        while (socket.isConnected()) {
            try {
                massageFromClient = bufferedReader.readLine();
                /*if (massageFromClient == null){
                    // для  macOS
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }*/
                broadcastMessage(massageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    //////////////////////////////////
    /**
     * Отправка сообщения всем слушателям
     *
     * @param message сообщение
     */
    private void broadcastMessage(String message) {
        String[] parts = message.split(" ");
        if (parts.length > 1 && parts[1].charAt(0) == '@' &&
                clients.stream().anyMatch(client -> client.name.equals(parts[1].substring(1)))) {
            var cln = clients.stream().filter(client -> client.name.equals(parts[1].substring(1))).findFirst();
            if (cln.isPresent()) {
                parts[1] = null;
                String newMessage = Arrays.stream(parts)
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.joining(" "));
                try {
                    cln.get().bufferedWriter.write(newMessage);
                    cln.get().bufferedWriter.newLine();
                    cln.get().bufferedWriter.flush();
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        } else {
            for (ClientManager client : clients) {
                try {
                    // Если клиент не равен по наименованию клиенту-отправителю,
                    // отправим сообщение
                    if (!client.name.equals(name) && message != null) {
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }
    }
    ////////////////////////////////

    //метод отправки сообщений
//    private void broadcastMessage(String message) {
//        //пройдет по всем клиентам и найдет клиента
//        //имя которого от кого пришло сообщение
//        //для того чтобы ему не пришло его же сообщение
//        for (ClientManager client : clients) {
//            try {
//                if (!client.name.equals(name)) {
//                    /*
//                    не сообразил как написать сообщение конкретному пользователю
//                    распарсил сообщение, выделил имя и все
//                    думал что может нужно создать новый поток или новый сокет,
//                    не получилось
//
//                     */
//                    if (message.contains("@")) {
//                        String[] person = message.split(" ");
//                        String nameKomu = person[1].substring(1);
//                        String mes = message.replaceAll(nameKomu, " ");
//                        Boolean count = false;
//                        for (ClientManager n : clients) {
//                            if (n.getName().equals(nameKomu)) {
//                                count = true;
//                            }
//                        }
//                        if (count) {
//                            client.bufferedWriter.write(mes);
//                            client.bufferedWriter.newLine();
//                            client.bufferedWriter.flush();
//
//                        }
//                    } else {
//                        client.bufferedWriter.write(message);
//                        client.bufferedWriter.newLine();
//                        client.bufferedWriter.flush();
//                    }
//                }
//            } catch (IOException e) {
//                closeEverything(socket, bufferedReader, bufferedWriter);
//            }
//        }
//    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Удаление клиента из коллекции
        removeClient();
        try {
            // Завершаем работу буфера на чтение данных
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            // Закрытие соединения с клиентским сокетом
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат.");
    }

    @Override
    public String toString() {
        return "ClientManager{" +
                "socket=" + socket +
                ", bufferedReader=" + bufferedReader +
                ", bufferedWriter=" + bufferedWriter +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(ClientManager o) {
        return this.name.compareTo(o.getName());
    }
}
