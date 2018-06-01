package cashbox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import json.request.Request;
import json.request.TasksRequest;
import json.request.data.CfgData;
import json.request.data.KeypadData;
import json.request.data.enums.CommandEnum;
import json.request.data.enums.ConfigFieldsEnum;
import json.request.data.enums.CountersFieldsEnum;
import json.response.Response;
import json.response.data.CountersResponse;
import keypad.Keypad;
import keypad.KeypadActionEnum;
import keypad.KeypadMode;
import lombok.Getter;
import remoteAccess.DataFromCashbox;
import remoteAccess.TCPSocket;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Класс для формирования команды отправки Json и отправки команд на сервер
 */

public class Bot implements BotFunctional {

    private CashBox cashBox;

    private Keypad keypad;
    private KeypadMode keypadMode;
    private TCPSocket tcpSocket;
    @Getter
    private int taskId;
    //формирование данных
    private List<TasksRequest> tasksRequestList = new ArrayList<>();

    public Bot(CashBox cashBox) {
        this.cashBox = cashBox;
        this.keypad = new Keypad(cashBox);
        this.keypadMode = new KeypadMode();
        this.tcpSocket = new TCPSocket();
    }

    public void start() {
        tcpSocket.createSocket(cashBox.CASHBOX_IP, CashBox.CASHBOX_PORT);
    }

    public void stop() {
        closeSessionJson();
        tcpSocket.socketClose(resultJson());
    }

    public String getScreenJson() {
        taskId++;
        TasksRequest task = new TasksRequest(taskId, CommandEnum.LCD_SCREEN);
        tasksRequestList.add(task);
        String response = tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        Response tasksResponse = new Gson().fromJson(response, Response.class);
        return savePicture(tasksResponse.getTaskResponseList().get(0).getLcdScreen());
    }

    public int getKeypadMode() {
        taskId++;
        TasksRequest task = new TasksRequest(taskId, CommandEnum.KEYPAD_MODE);
        tasksRequestList.add(task);
        String response = tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        Response tasksResponse = new Gson().fromJson(response, Response.class);
        return tasksResponse.getTaskResponseList().get(0).getKeypadMode();
    }

    public Map<ConfigFieldsEnum, String> getConfig(ConfigFieldsEnum... configFieldsEnums) {
        List<ConfigFieldsEnum> fieldsList = new ArrayList<>(Arrays.asList(configFieldsEnums));
        taskId++;
        CfgData cfgData = new CfgData(fieldsList);
        TasksRequest task = new TasksRequest(taskId, CommandEnum.CFG_GET, cfgData);
        tasksRequestList.add(task);
        String response = tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        Response tasksResponse = new Gson().fromJson(response, Response.class);
        return tasksResponse.getTaskResponseList().get(0).getConfigData();
    }

    public CountersResponse getCounters(CountersFieldsEnum... countersFieldsEnums) {
        List<CountersFieldsEnum> fieldsList = new ArrayList<>(Arrays.asList(countersFieldsEnums));
        taskId++;
        TasksRequest tasks = new TasksRequest(taskId, CommandEnum.COUNTERS_GET, fieldsList);
        tasksRequestList.add(tasks);
        String response = tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        Response tasksResponse = new Gson().fromJson(response, Response.class);
        return tasksResponse.getTaskResponseList().get(0).getCountersData();
    }

    public boolean isLoaderScreen() {
        taskId++;
        TasksRequest tasks = new TasksRequest(taskId, CommandEnum.LOADER_STATUS);
        tasksRequestList.add(tasks);
        String response = tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        Response tasksResponse = new Gson().fromJson(response, Response.class);
        return tasksResponse.getTaskResponseList().get(0).getLoaderStatus().equals("ON");
    }

    //TODO разобраться почему -1
//    public CashboxConfig getConfig(){
//        taskId++;
//        TasksRequest tasksRequest = new TasksRequest(taskId, CommandEnum.CFG_GET);
//        tasksRequestList.add(tasksRequest);
//        String response = tcpSocket.sendDataToSocket(getTaskId(), resultJson());
//        Response tasksResponse = new Gson().fromJson(response, Response.class);
//        return tasksResponse.getTaskResponseList().get(0).getConfigData();
//    }

    public List<String> sendCommandSsh(String command) {
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        return ssh.executeListCommand(command);
    }

    // добавляем таск на закрытие сессии (используется только в классе remoteAccess.TCPSocket, в функции closeSocket()
    private void closeSessionJson() {
        taskId++;
        TasksRequest task = new TasksRequest(taskId, CommandEnum.CLOSE_SESSION);
        tasksRequestList.add(task);
    }

    // составляем Json из сформированных тасок, используется только в классе  remoteAccess.TCPSocket, в функцях sendDataToSocket() и closeSocket()
    private String resultJson() {
        Request request = new Request(cashBox.UUID, tasksRequestList);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        String jsonStr = gson.toJson(request);
        System.out.println("jsonStr = " + jsonStr);

        taskId = 0;
        tasksRequestList.clear();
        return jsonStr;
    }

    public void sendData() {
        tcpSocket.sendDataToSocket(getTaskId(), resultJson());
    }

    public void enterData(String str) {

        int[] numberKey = new int[str.length()];
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.charAt(i));
            numberKey[i] = str.charAt(i) - '0';
            System.out.println(numberKey[i]);

        }
        for (int i = 0; i < numberKey.length; i++) {
            if (numberKey[i] == 1) {
                pressKey(0x12, 0, 1);
                continue;
            }
            if (numberKey[i] == 2) {
                pressKey(0x13, 0, 1);
                continue;
            }
            if (numberKey[i] == 3) {
                pressKey(0x14, 0, 1);
                continue;
            }
            if (numberKey[i] == 4) {
                pressKey(0x0C, 0, 1);
                continue;
            }
            if (numberKey[i] == 5) {
                pressKey(0x0D, 0, 1);
                continue;
            }
            if (numberKey[i] == 6) {
                pressKey(0x0E, 0, 1);
                continue;
            }
            if (numberKey[i] == 7) {
                pressKey(0x06, 0, 1);
                continue;
            }
            if (numberKey[i] == 8) {
                pressKey(0x07, 0, 1);
                continue;
            }
            if (numberKey[i] == 9) {
                pressKey(0x08, 0, 1);
                continue;
            }
            if (numberKey[i] == 0) {
                pressKey(0x18, 0, 1);
                continue;
            }

        }
        System.out.println(3);
        sendData();
        // enterStringData(str);
    }

    //Нажатие на кнопку (ввод данных) в зависимости от символа
    public void enterStringData(String str) {

        System.out.println("keypadMode.SPEC_SYMBOLS " + keypadMode.SPEC_SYMBOLS);
        System.out.println("keypadMode.ACTION_MODE " + keypadMode.ACTION_MODE);
        System.out.println("keypadMode.NUMBERS " + keypadMode.NUMBERS);
        System.out.println("keypadMode.FREE_MODE " + keypadMode.FREE_MODE);
        System.out.println("keypadMode.CYRILLIC " + keypadMode.CYRILLIC);
        System.out.println("keypadMode.ENGLISH " + keypadMode.ENGLISH);

        int keypadMode = getKeypadMode();
        System.out.println(keypadMode);
        Short keyNumPrew = 40, keyNum = 0;
        Integer pressCount = 0;
        boolean exit;

        Keypad[] keys = new Keypad[Keypad.keys_table_size];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new Keypad(cashBox);
        }

        keypad.initKey(keys);

        Charset cset = Charset.forName("CP866");
        ByteBuffer buf = cset.encode(str);
        byte[] charsCp866 = buf.array();

        for (Short i = 0; i < charsCp866.length; i++) {
            int charsetNumber = (int) charsCp866[i];
            if (charsetNumber < 0) {
                charsetNumber += 256;
            }
            exit = false;

            for (int j = 0; j < Keypad.keys_table_size; j++) {
                if (exit)
                    break;
                pressCount = 0;

                //Если русский
                if (keypadMode == this.keypadMode.CYRILLIC) {

                    for (int k = 0; k < keys[j].rus_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].rus_code.get(k)) {
                            keyNum = keys[j].key_code;
                            pressKey(keyNum, 0, pressCount);
                            //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                            if (keyNumPrew.equals(keyNum)) {
                                sendData();
                            }
                            exit = true;
                        }
                    }
                }

                //Если англ
                if (keypadMode == this.keypadMode.ENGLISH) {

                    for (int k = 0; k < keys[j].eng_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].eng_code.get(k)) {
                            keyNum = keys[j].key_code;
                            pressKey(keyNum, 0, pressCount);
                            //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                            if (keyNumPrew.equals(keyNum)) {
                                sendData();
                            }
                            exit = true;
                        }
                    }
                }

                // Если спец символы
                if (keypadMode == this.keypadMode.SPEC_SYMBOLS) {
                    for (int k = 0; k < keys[j].spec_sym_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].spec_sym_code.get(k)) {
                            keyNum = keys[j].key_code;
                            //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                            if (keyNumPrew.equals(keyNum)) {
                                sendData();
                            }
                            pressKey(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                }

                // Если цифры или Свободный режим
                if ((keypadMode == this.keypadMode.NUMBERS) || (keypadMode == this.keypadMode.FREE_MODE)) {
                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount = 1;
                        pressKey(keyNum, 0, pressCount);
                        exit = true;
                    }

                    //если спец. символы
                    for (int k = 0; k < keys[j].spec_sym_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        //.
                        if (charsetNumber == 46) {
                            keyNum = 37;// keys[j].key_code;
                            pressKey(keyNum, 0, pressCount);
                            exit = true;
                        }
                        //+
                        if (charsetNumber == 43) {
                            keyNum = 22;
                            pressKey(keyNum, 0, pressCount);
                            exit = true;
                        }
                        //-
                        if (charsetNumber == 45) {
                            keyNum = 30;
                            pressKey(keyNum, 0, pressCount);
                            exit = true;
                        }
                        ///
                        if (charsetNumber == 42) {
                            keyNum = 38;
                            pressKey(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                }

                //Если русский + цифры, keypadMode == 5
                if (keypadMode == this.keypadMode.CYRILLIC + this.keypadMode.NUMBERS) {
                    for (int k = 0; k < keys[j].rus_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].rus_code.get(k)) {
                            keyNum = keys[j].key_code;

                            //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                            if (keyNumPrew.equals(keyNum)) {
                                sendData();
                            }
                            pressKey(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }

                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount++;
                        pressKey(keyNum, 0, pressCount);
                        //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                        if (keyNumPrew.equals(keyNum)) {
                            sendData();
                        }
                        exit = true;
                    }
                }

                // Смешанный ввод CYRILLIC + ENGLISH + NUMBERS = 7;
                if (keypadMode == (this.keypadMode.CYRILLIC + this.keypadMode.ENGLISH + this.keypadMode.NUMBERS)) {
                    // проверяем таблицу спецсимволов
                    for (int k = 0; k < keys[j].spec_sym_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].spec_sym_code.get(k)) {
                            keyNum = keys[j].key_code;
                            pressKey(keyNum, 0, pressCount);
                            //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                            if (keyNumPrew.equals(keyNum)) {
                                sendData();
                            }
                            exit = true;
                        }
                    }
                    if (!exit) pressCount = 0;

                    // проверяем русские буквы
                    for (int k = 0; k < keys[j].rus_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].rus_code.get(k)) {
                            keyNum = keys[j].key_code;
                            //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                            if (keyNumPrew.equals(keyNum)) {
                                sendData();
                            }
                            pressKey(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                    for (int k = 0; k < keys[j].eng_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].eng_code.get(k)) {
                            keyNum = keys[j].key_code;
                            pressKey(keyNum, 0, pressCount);
                            //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                            if (keyNumPrew.equals(keyNum)) {
                                sendData();
                            }
                            exit = true;
                        }
                    }
                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount++;
                        pressKey(keyNum, 0, pressCount);
                        //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                        if (keyNumPrew.equals(keyNum)) {
                            sendData();
                        }
                        exit = true;
                    }
                }
                keyNumPrew = keyNum;

                // Английские символы + цифры
                if (keypadMode == this.keypadMode.ENGLISH + this.keypadMode.NUMBERS) {
                    for (int k = 0; k < keys[j].eng_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].eng_code.get(k)) {
                            keyNum = keys[j].key_code;

                            //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                            if (keyNumPrew.equals(keyNum)) {
                                sendData();
                            }
                            pressKey(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }

                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount++;
                        pressKey(keyNum, 0, pressCount);
                        //если 2 буквы в строке находятся на одной кнопке отправляем нажатие на кассу, тем самым делаем паузу
                        if (keyNumPrew.equals(keyNum)) {
                            sendData();
                        }
                        exit = true;
                    }
                }
            }
            sendData();
        }
        //sendData();
    }

    public void pressKey(int keyNum, int keyNum2, int pressCount) {
        for (int i = 0; i < pressCount; i++) {
            pressButton(keyNum, keyNum2, KeypadActionEnum.KEY_DOWN);
        }
    }

    private void pressButton(int key1, int key2, KeypadActionEnum action) {
        taskId++;
        KeypadData keypadData = new KeypadData(key1, key2, action);
        TasksRequest task = new TasksRequest(taskId, CommandEnum.KEYPAD_ACTION, keypadData);
        tasksRequestList.add(task);
    }


    //удержание кнопки
    private void holdKey(int keyNum, int keyNum2, int pressCount) {
        for (int i = 0; i < pressCount; i++) {
            pressButton(keyNum, keyNum2, KeypadActionEnum.KEY_HOLD);
        }
    }

    //Преобразование полученного массива от сервера в картинку
    private String savePicture(int[] lcdScreen) {
        if (lcdScreen != null) {
            byte byteToFile[] = new byte[lcdScreen.length];
            for (int i = 0; i < lcdScreen.length; i++)
                byteToFile[i] = (byte) lcdScreen[i];
            try {
                FileOutputStream fos = new FileOutputStream(new File("reciveData\\tmpScreen.bmp"));
                fos.write(byteToFile);
                fos.close();
                FileInputStream fstream = new FileInputStream("reciveData\\tmpScreen.bmp");
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                return br.readLine();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    public void rebootCashBox() {
        sendCommandSsh("/sbin/reboot");
        try {
            Thread.sleep(85_000);
            System.out.println("SLEEP IS OVER");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        start();
    }
}
