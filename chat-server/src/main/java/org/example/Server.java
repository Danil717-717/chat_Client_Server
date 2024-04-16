package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Класс Сервер со свойством serverSocket
 *
 * @author Строев Д.В.
 * @version 1.0
 */
public class Server {

    /**
     * Поле serverSocket
     */
    private final ServerSocket serverSocket;

    /**
     * Конструктор - создание нового объекта
     *
     * @param serverSocket серверный сокет
     * @see Server#Server(ServerSocket)
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Метод запуска сервера
     */
    public void runServer(){
        try {
            //вечный цикл пока сервер не закрыт
            while (!serverSocket.isClosed()) {
                //пока все работает вызываем главный метод .accept()
                //в данном случае этот метод переводит наш главный поток
                //в режим ожидания подключения нового сокета
                //как только он подключается, создается новый
                //обьект сокета
                Socket socket = serverSocket.accept();
                //создадим обвертку где будет храниться инфо о клиентах
                //он будет на прямую связан с подключенным сокетом
                ClientManager clientManager = new ClientManager(socket);
                //Выводим сообщение о подключении нового клиента
                System.out.println("Подключен новый клиент!");
                //и сразу создаем новый поток для вызова некоторых инструкций
                //в классе обвертки в методе run
                Thread thread = new Thread(clientManager);
                thread.start();
            }
        }
        catch (IOException e){
            closeSocket();
        }
    }

    private void closeSocket(){
        try{
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
