import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
   // private Bot bot = new Bot();
    //сохранение режима ввода, полученного с кассы
    @Getter
    @Setter (AccessLevel.PRIVATE)
    private static int keypadMode = 0;
    //список запрашиваемых полей
    @Setter
    private List<ConfigFieldsEnum> configFieldsEnum;

    public void print() {
        System.out.println("TCPSocket print:");
        for (ConfigFieldsEnum str: configFieldsEnum)
            System.out.println(String.valueOf(str));
    }

    @Getter
    public List<String> valueConfigFields = new ArrayList<>();
    public void print2() {
        System.out.println("TCPSocket print valueConfigFields:");
        for (String str: valueConfigFields)
            System.out.println(str);
    }

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
     //       bot.closeSessionJson();
       //     String sendCloseSession = bot.resultJson();
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
            byte bufClose[] = new byte[65535];//1024 * 1024];
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
        tasksResponse.print();
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
                    print();
                    valueConfigFields = tasksResponse.getConfigValue(i, configFieldsEnum);
                    print2();
                }
            }
        }
    }
    //----------------------------------------------------------------------------
}
