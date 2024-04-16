package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Класс ClientManager со свойствами socket, name,
 * bufferedWriter, bufferedReader, clients
 *
 * @author Строев Д.В.
 * @version 1.0
 */
public class ClientManager implements Runnable, Comparable<ClientManager> {

    /**
     * Поле socket
     */
    private final Socket socket;

    /**
     * Поле name
     */
    private String name;

    /**
     * Поле bufferedReader
     */
    private BufferedReader bufferedReader;

    /**
     * Поле bufferedWriter
     */
    private BufferedWriter bufferedWriter;

    /**
     * Поле clients
     */
    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    /**
     * Конструктор - создание нового объекта
     *
     * @param socket сокет
     * @see ClientManager#ClientManager(Socket, String)
     */
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

    /**
     * Конструктор - создание нового объекта
     *
     * @param socket сокет
     * @param name имя клиента
     * @see ClientManager#ClientManager(Socket, String)
     */
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

    /**
     * Геттер поля name
     *
     * @return имя
     */
    public String getName() {
        return name;
    }

    /**
     * Сеттер поля name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Метод запуска потока
     */
    @Override
    public void run() {

        String massageFromClient;

        while (socket.isConnected()) {
            try {
                massageFromClient = bufferedReader.readLine();
                broadcastMessage(massageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    /**
     * Метод отправки сообщений всем слушателям
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

    /**
     * Метод выхода клиента из чата
     *
     * @param socket         сокет
     * @param bufferedReader bufferedReader
     * @param bufferedWriter bufferedWriter
     */
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

    /**
     * Метод удаления клиента
     */
    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат.");
    }

    /**
     * Метод получения строкового представления объекта
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "ClientManager{" +
                "socket=" + socket +
                ", bufferedReader=" + bufferedReader +
                ", bufferedWriter=" + bufferedWriter +
                ", name='" + name + '\'' +
                '}';
    }

    /**
     * Метод сравнения объекта с объектом
     *
     * @param o the object to be compared.
     * @return числовой результат сравнения
     */
    @Override
    public int compareTo(ClientManager o) {
        return this.name.compareTo(o.getName());
    }
}

