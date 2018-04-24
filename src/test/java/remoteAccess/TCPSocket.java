package remoteAccess;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import cashbox.ConfigFieldsEnum;
import json.fromSever.TasksResponse;

/**
 * Класс работы с сокетом
 * Функции:
 * открытия сокета
 * закрытия сокета
 * передачи Json-а в сокет
 * парсинга ответа от сервера
 */

public class TCPSocket {
    //объект для полчения Json-ов тасок
   // private cashbox.Bot bot = new cashbox.Bot();
    //сохранение режима ввода, полученного с кассы
    @Getter
    @Setter (AccessLevel.PRIVATE)
    private static int keypadMode = 0;
    //список запрашиваемых полей
    @Setter
    private List<ConfigFieldsEnum> configFieldsEnum;
    @Getter
    public List<String> valueConfigFields = new ArrayList<>();

    //локальная переменная, в которой храним количество тасок, которые передаем на сервер
    @Setter
    private int taskId;
    //сокет для соединения с сервером
    private static Socket workSocket;
    // открываем сокет, коннектимся к IP кассы, порт 3245 и получаем сокет сервера
    // запускается поток получения экрана
    public void createSocket(String host, int port) {
        try {
            workSocket = new Socket(host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------
    //закрытие сокета
    public void socketClose(String sendCloseSession ) {
        try {
            // передаем команду закрытия соединения
            workSocket.getOutputStream().write(sendCloseSession.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------
    // передаем Json в сокет
    public void sendDataToSocket(int taskCount, String jsonCommand) {
        try {
            taskId = taskCount;
            // Берем входной и выходной потоки сокета
            InputStream sin = workSocket.getInputStream();
            OutputStream sout = workSocket.getOutputStream();
            // передаем данные, читаем ответ
            sout.write(jsonCommand.getBytes());
            byte bufClose[] = new byte[65535];                                                           //1024 * 1024];
            int rClose = 0;
            while (rClose == 0)
                rClose = sin.read(bufClose);
            String data = new String(bufClose, 0, rClose);
            System.out.println("data = " + data);
            parseJson(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------
    //Парсинг ответа от сервера
    private void parseJson(String json) {
        TasksResponse tasksResponse = new Gson().fromJson(json, TasksResponse.class);
        for (int i = 0; i < taskId; i++) {
            if (!tasksResponse.getTaskResult(i).equals("OK"))
                System.out.println("Task complite with result not OK. task id = " + (i+1));
            else {
                if (json.contains("lcd_screen")) {
                    tasksResponse.savePicture(i);
                }
                if (json.contains("keypad_mode")) {
                    setKeypadMode(tasksResponse.getKeypadMode(i));
                }
                if (json.contains("cfg_data")) {
                    valueConfigFields = tasksResponse.getConfigValue(i, configFieldsEnum);
                }
            }
        }
    }
    //----------------------------------------------------------------------------
}
