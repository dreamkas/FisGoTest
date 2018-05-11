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
import keypad.KeyEnum;
import keypad.Keypad;
import keypad.KeypadActionEnum;
import keypad.KeypadMode;
import lombok.Getter;
import remoteAccess.DataFromCashbox;
import remoteAccess.TCPSocket;
import screens.ScreenPicture;
import screens.Screens;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Класс для формирования команды отправки Json и отправки команд на сервер
 */

public class Bot implements IBot {

    private CashBox cashBox;

    private Keypad keypad;
    private KeypadMode keypadMode;
    private TCPSocket tcpSocket;

    public Bot(CashBox cashBox) {
        this.cashBox = cashBox;
        this.keypad = new Keypad(cashBox);
        this.keypadMode = new KeypadMode();
        this.tcpSocket = new TCPSocket();
    }

     //данные для формирование команды, которая будет передана на сервер
    @Getter
    private int taskId;
    //объект для сравнения полученного экрана с ожидаемым
    private Screens screens = new Screens();
    //формирование данных
    private List<TasksRequest> tasksRequestList = new ArrayList<>();

    private KeyEnum keyEnum = new KeyEnum();

    public void start(){
        tcpSocket.createSocket(cashBox.CASHBOX_IP, CashBox.CASHBOX_PORT);
    }

    public void stop(){
        closeSessionJson();
        tcpSocket.socketClose(resultJson());
    }

    public void getScreenJson() {
        taskId++;
        TasksRequest task = new TasksRequest(taskId, CommandEnum.LCD_SCREEN);
        tasksRequestList.add(task);
        String response = tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        Response tasksResponse = new Gson().fromJson(response, Response.class);
        savePicture(tasksResponse.getTaskResponseList().get(0).getLcdScreen());
    }

    public int getKeypadMode() {
        taskId++;
        TasksRequest task = new TasksRequest(taskId, CommandEnum.KEYPAD_MODE);
        tasksRequestList.add(task);
        String response = tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        Response tasksResponse = new Gson().fromJson(response, Response.class);
        return tasksResponse.getTaskResponseList().get(0).getKeypadMode();
    }

    public Map<ConfigFieldsEnum, String> getConfig(ConfigFieldsEnum ... configFieldsEnums) {
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

    public List<String> readDataScript(String fileName) {
        List<String> list = new ArrayList<>();
        try {
            FileInputStream fstream = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                list.add(strLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void enterData(String str) {

        int[] numberKey = new int[str.length()];
        for (int i = 0; i < str.length() ; i++) {
            System.out.println(str.charAt(i));
            numberKey[i] = str.charAt(i) - '0';
            System.out.println(numberKey[i]);

        }
        for (int i = 0; i < numberKey.length ; i++) {
            if(numberKey[i] == 1){
                pressKey( 0x12, 0, 1);
            continue;}
            if(numberKey[i] == 2){
                pressKey( 0x13, 0, 1);
                continue;}
            if(numberKey[i] == 3){
                pressKey( 0x14, 0, 1);
                continue;}
            if(numberKey[i] == 4){
                pressKey( 0x0C, 0, 1);
                continue;}
            if(numberKey[i] == 5){
                pressKey( 0x0D, 0, 1);
                continue;}
            if(numberKey[i] == 6){
                pressKey( 0x0E, 0, 1);
                continue;}
            if(numberKey[i] == 7){
                pressKey( 0x06, 0, 1);
                continue;}
            if(numberKey[i] == 8){
                pressKey( 0x07, 0, 1);
                continue;}
            if(numberKey[i] == 9){
                pressKey( 0x08, 0, 1);
                continue;}
            if(numberKey[i] == 0){
                pressKey( 0x18, 0, 1);
                continue;}

        }
        System.out.println(3);
        sendData();
       // enterStringData(str);
    }

    // в функции проверяется экран на дисплее кассы,
    // если экран совпадает, то выполняется выборка из БД пользователей и вводдится пароль
    public void enterPasswordIfScreenOpen() {
        //проверяем, что открыт экран ввода пароля
        getScreenJson();
        boolean compare = screens.compareScreen(ScreenPicture.PASSWORD);
        //если полученный экран с кассы совпадает с экраном ввода пароля, то выполняем if
        if (compare) {
            //делаем выборку их БД users на кассе, получаем пароль одного из них
            String getPassCommand = "echo \"attach '/FisGo/usersDb.db' as users; " +
                    "select PASS from users.USERS limit 1;\" | sqlite3 /FisGo/usersDb.db\n";
            DataFromCashbox dataFromCashbox = new DataFromCashbox();
            dataFromCashbox.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
            List<String> line = dataFromCashbox.executeListCommand(getPassCommand);
            dataFromCashbox.disconnectSession();
            //вводим пароль на кассе
            enterStringData(line.get(0));
            tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        }
    }

    private void trySleep(int milisec) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Открытие смены
//    public int openShift(int stage) {
//        // делаем выборку их конфига на кассе, проверем, открыта смена или нет
//        // в зависимости от режима статус открытия смены проверяется по разным флагам
//        // в учебном режиме и енвд используется флаг SHIFT_TIMER
//        // для зареганной кассы выгружаем флаг из биоса IS_SHIFT_OPEN
//        List<ConfigFieldsEnum> line = new ArrayList<>();
//        if ((stage == CashboxStagesEnum.STUDY) || (stage == CashboxStagesEnum.ENVD))
//            line.add(ConfigFieldsEnum.SHIFT_TIMER);
//        if (stage == CashboxStagesEnum.REGISTRED)
//            line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);
//
//        List<String> valueConfigList = getConfig(line);
//
//        if (valueConfigList.get(0).equals("0")) {
//            pressKey(keyEnum.keyMenu, 0, 1);
//            pressKey(keyEnum.key1, 0, 1);
//            sendData();
//            trySleep(2000);
//            getScreenJson();
//            boolean compare = screens.compareScreen(ScreenPicture.OPEN_SHIFT_MENU);
//            if (compare) {
//                //FIXME добавить обработку даты открытия смены
//                String getPassCommand = "date '+%d%m%y%H%M'\n";
//                DataFromCashbox dataFromCashbox = new DataFromCashbox();
//                dataFromCashbox.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
//                List<String> dateStr = dataFromCashbox.executeListCommand(getPassCommand);
//                dataFromCashbox.disconnectSession();
//                pressKey(keyEnum.keyEnter, 0, 2);
//                sendData();
//                System.out.println("gggggggg");
//                // trySleep(20000);
//                try {
//                    System.out.println("1111");
//                    Thread.sleep(15000);
//                    System.out.println("2222");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("ttttttt");
//                getScreenJson();
//                compare = screens.compareScreen(ScreenPicture.FREE_SALE_MODE);
//                if (compare) {
//                    return 0;
//                } else {
//                    return -3;
//                }
//            } else {
//                writeLogFile("Пункт открытия смены не доступен");
//                return -2;
//            }
//        } else
//            return -1;
//    }

    //Продажа, приход
    public int checkPrintSaleComming(List<String> keyWordArray, ScreenPicture screen) {
        pressKey(keyEnum.keyMenu, 0, 1);
        pressKey(keyEnum.keyCancel, 0, 1);
        sendData();
        trySleep(2000);
        getScreenJson();
        boolean compare = screens.compareScreen(ScreenPicture.FREE_SALE_MODE);
        if (compare) {
            String countCheckStr = searchForKeyword("check_count: ", keyWordArray);
            if (countCheckStr.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("В сценарии не указано количество чеков, считаем, что необходимо напечатать один чек...");
                countCheckStr = "1";
            }
            String tmpGoodsStr = searchForKeyword("Good ", keyWordArray);
            if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("Не найдены товары, которые необходимо добавить в чек");
                return -2;
            } else {
                int countChecks = Integer.parseInt(countCheckStr);
                for (int i = 0; i < countChecks; i++) {
                    int resPrintCheck = checkPrint(keyWordArray, screen);
                    switch (resPrintCheck) {
                        case -1:
                            return -3;
                        case -2:
                            return -4;
                        case -3:
                            return -5;
                        case -5:
                            return -6;
                        case -4:
                            return -7;
                        case -6:
                            return -8;
                        default:
                            break;
                    }
                }
            }
        } else {
            writeLogFile("Не открыт экран продажи (режим свободной цены)");
            return -1;
        }
        return 1;
    }

    //Формирование и печать чека
    private int checkPrint(List<String> keyWordArray, ScreenPicture screen) {
        int countGoods = 0;
        for (String tmpStr : keyWordArray) {
            if (tmpStr.contains("Good "))
                countGoods++;
        }
        if (countGoods == 0)
            return -1;

        for (int j = 0; j < countGoods; j++) {
            String tmpGoodsStr = searchForKeyword("good_from_" + (j + 1) + ": ", keyWordArray);
            if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("Не указан способ добавления товара " + (j + 1) + "в чек");
                return -2;
            } else {
                if (tmpGoodsStr.equals("good_from_base")) {
                    pressKey(keyEnum.keyGoods, 0, 1);
                    tmpGoodsStr = searchForKeyword("good_code_" + (j + 1) + ": ", keyWordArray);
                    enterStringData(tmpGoodsStr);
                    pressKey(keyEnum.keyEnter, 0, 1);
                    sendData();
                    //товар из базы со свободной ценой
                    tmpGoodsStr = searchForKeyword("good_base_free_price_" + (j + 1) + ": ", keyWordArray);
                    if (!tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                        enterStringData(tmpGoodsStr);
                        pressKey(keyEnum.keyEnter, 0, 1);
                        sendData();
                    }
                    //весовой товар из базы
                    tmpGoodsStr = searchForKeyword("good_type_" + (j + 1) + ": ", keyWordArray);
                    if (tmpGoodsStr.equals("weighted")) {
                        tmpGoodsStr = searchForKeyword("good_weight_" + (j + 1) + ": ", keyWordArray);
                        if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                            writeLogFile("Не указан тип товара во входном файле сценария");
                            return -3;
                        }
                        enterStringData(tmpGoodsStr);
                        pressKey(keyEnum.keyEnter, 0, 1);
                        sendData();
                    }
                }
                if (tmpGoodsStr.equals("good_free_price")) {
                    tmpGoodsStr = searchForKeyword("good_price_" + (j + 1) + ": ", keyWordArray);
                    enterStringData(tmpGoodsStr);
                    pressKey(keyEnum.keyEnter, 0, 1);
                    sendData();
                }
                //если штучный товар, то смотрим еолимчество
                tmpGoodsStr = searchForKeyword("good_type_" + (j + 1) + ": ", keyWordArray);
                if (tmpGoodsStr.equals("countable")) {
                    tmpGoodsStr = searchForKeyword("good_count_" + (j + 1) + ": ", keyWordArray);
                    if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                        writeLogFile("Не указано количество товара во входном файле сценария");
                        return -4;
                    }
                    if (Integer.parseInt(tmpGoodsStr) > 1) {
                        pressKey(keyEnum.keyQuantity, 0, 1);
                        enterStringData(tmpGoodsStr);
                        pressKey(keyEnum.keyEnter, 0, 1);
                    }
                    sendData();
                }

                tmpGoodsStr = searchForKeyword("spacial_form_" + (j + 1) + ": ", keyWordArray);
                if (!tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                    holdKey(keyEnum.keyMenu, 0, 1);
                    if (tmpGoodsStr.equals("ПР"))
                        pressKey(keyEnum.key1, 0, 1);
                    if (tmpGoodsStr.equals("ЧП")) {
                        pressKey(keyEnum.key2, 0, 1);
                        tmpGoodsStr = searchForKeyword("special_form_prepayment_sum_" + (j + 1) + ": ", keyWordArray);
                        enterStringData(tmpGoodsStr);
                        pressKey(keyEnum.keyEnter, 0, 1);
                    }
                    if (tmpGoodsStr.equals("А"))
                        pressKey(keyEnum.key3, 0, 1);
                    if (tmpGoodsStr.equals("П")) {
                        pressKey(keyEnum.key4, 0, 1);
                        tmpGoodsStr = searchForKeyword("special_form_offset_of_prepayment_sum_" + (j + 1) + ": ", keyWordArray);
                        enterStringData(tmpGoodsStr);
                        pressKey(keyEnum.keyEnter, 0, 1);
                    }
                    if (tmpGoodsStr.equals("КР"))
                        pressKey(keyEnum.key5, 0, 1);
                    if (tmpGoodsStr.equals("ЧК")) {
                        pressKey(keyEnum.key6, 0, 1);
                        tmpGoodsStr = searchForKeyword("special_form_credit_sum_" + (j + 1) + ": ", keyWordArray);
                        enterStringData(tmpGoodsStr);
                        pressKey(keyEnum.keyEnter, 0, 1);
                    }
                    if (tmpGoodsStr.equals("К")) {
                        pressKey(keyEnum.key7, 0, 1);
                        tmpGoodsStr = searchForKeyword("special_form_credit_pay_sum_" + (j + 1) + ": ", keyWordArray);
                        enterStringData(tmpGoodsStr);
                        pressKey(keyEnum.keyEnter, 0, 1);
                    }
                    sendData();
                }
            }
        }

        //Тип оплаты чека
        String tmpGoodsStr = searchForKeyword("type_pay: ", keyWordArray);
        System.out.println("type_pay: " + tmpGoodsStr);
        if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Не указан способ оплаты во входном файле сценария");
            return -5;
        } else {
            if (tmpGoodsStr.equals("cash_pay")) {
                pressKey(keyEnum.keyPayByCash, 0, 1);
                tmpGoodsStr = searchForKeyword("sum_pay: ", keyWordArray);
                enterStringData(tmpGoodsStr);
            }
            if (tmpGoodsStr.equals("card_pay")) {
                pressKey(keyEnum.keyPayByCard, 0, 1);
            }

            pressKey(keyEnum.keyEnter, 0, 1);
            sendData();
        }
        trySleep(3500);
        getScreenJson();
        boolean compare = screens.compareScreen(screen);
        if (!compare) {
            //получить дату из кассы
            DataFromCashbox dataFromCashbox = new DataFromCashbox();
            dataFromCashbox.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
            String getDateCommand = " date '+%d%m%y%H%M'\n";
            dateStr = dataFromCashbox.executeListCommand(getDateCommand);
            trySleep(2000);
        } else {
            writeLogFile("Не совпадает дисплей кассы с ожидаемым экраном сдачи");
            return -6;
        }
        return 0;
    }

    //Внесение
    public int insertion(List<String> keyWordArray) {
        pressKey(keyEnum.keyMenu, 0, 1);
        pressKey(keyEnum.key1, 0, 1);
        sendData();
        getScreenJson();
        boolean compare = screens.compareScreen(ScreenPicture.SHIFT_MENU_OPEN_SHIFT);
        if (compare) {
            pressKey(keyEnum.key5, 0, 1);
            String sumInsertion = searchForKeyword("sum_insertion: ", keyWordArray);
            if (sumInsertion.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("В сценарии не задана сумма внесения.\nЗавершение выполнения внесения невозможно");
                return -2;
            }
            enterStringData(sumInsertion);
            pressKey(keyEnum.keyEnter, 0, 2);
            sendData();
            return 0;
        } else {
            writeLogFile("Смена закрыта. Пункт меню внесения не доступен");
            return -1;
        }
    }

    public int reserve(List<String> keyWordArray) throws IOException {
        pressKey(keyEnum.keyMenu, 0, 1);
        pressKey(keyEnum.key1, 0, 1);
        //добавить проверку, что смена открыта
        pressKey(keyEnum.key4, 0, 1);
        //sendData();
        String sumReserve = searchForKeyword("sum_reserve: ", keyWordArray);
        if (sumReserve.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ reserve in input file");
            return -1;
        }
        enterStringData(sumReserve);
        pressKey(keyEnum.keyEnter, 0, 2);
        sendData();
        return 0;
    }


    public void xCount() {
        pressKey(keyEnum.keyMenu, 0, 1);
        pressKey(keyEnum.key1, 0, 1);
        //добавить проверку, что смена открыта
        pressKey(keyEnum.key3, 0, 1);
        sendData();
    }

    public List<String> dateStr = new ArrayList<>();

    //Регистрация
//    public int registration(List<String> keyWordArray) {
//        writeLogFile("Выполняется функция регистрации.");
//        pressKey(keyEnum.keyMenu, 0, 1);
//        pressKey(keyEnum.key5, 0, 1);
//        pressKey(keyEnum.key1, 0, 1);
//        sendData();
//        //если открыто меню Регистрация ККТ и доступен пункт меню Регистрация
//        getScreenJson();
//        boolean compare = screens.compareScreen(ScreenPicture.MENU_REGISTRATION);
//        if (compare) {
//            writeLogFile("Пункт меню Регистрация доступен.");
//            pressKey(keyEnum.keyEnter, 0, 1);
//            sendData();
//            //тип регистрации
//            String registrationData = searchForKeyword("registration_type: ", keyWordArray);
//            // проверка экрана загрузки данных из кабинета
//            //TODO: доделать регистрацию из кабинета
//            //TODO: доделать облачную регистрацию
//            getScreenJson();
//            compare = screens.compareScreen(ScreenPicture.GET_REGISTRATION_DATA_FROM_CABINET);
//            if (compare) {
//                writeLogFile("Открыто окно загрузки рег. данных из кабинета");
//                if (!registrationData.equals("Регистрация через Кабинет"))
//                    pressKey(keyEnum.keyCancel, 0, 1);
//                else
//                    pressKey(keyEnum.keyEnter, 0, 1);
//                sendData();
//            }
//
//            pressKey(keyEnum.keyEnter, 0, 1);
//            sendData();
//            //Выбор режима работы ККТ
//            changeKktMode("mode_kkt: ", keyWordArray);
//
//            //ИНН организации
//            pressKey(keyEnum.keyEnter, 0, 2);
//            sendData();
//            registrationData = searchForKeyword("organization_inn: ", keyWordArray);
//            if (registrationData.equals("CANNOT FIND KEYWORD")) {
//                writeLogFile("В сценарии не указано ИНН организации\n");
//                getScreenJson();
//                compare = screens.compareScreen(ScreenPicture.EMPTY_SCREEN);
//                if (compare) {
//                    writeLogFile("Экран ИНН пустой, завершение регистрации невозможно\n");
//                    return -2;
//                } else
//                    pressKey(keyEnum.keyEnter, 0, 1);
//            } else {
//                //получаем значения поя ИНН
//                List<ConfigFieldsEnum> list = new ArrayList<>();
//                list.add(ConfigFieldsEnum.ORGANIZATION_INN);
//                List<String> valueFieldsList = getConfig(list);
//
//                if (valueFieldsList.size() != 0) {
//                    if (registrationData.equals(valueFieldsList.get(0)))
//                        pressKey(keyEnum.keyEnter, 0, 1);
//                    else {
//                        //очистка ранее введенных данных
//                        clearDisplay(valueFieldsList.get(0).length());
//                        //Ввод данных из сценария
//                        enterStringData(registrationData);
//                        pressKey(keyEnum.keyEnter, 0, 1);
//                    }
//                } else {
//                    //Ввод данных из сценария
//                    enterStringData(registrationData);
//                    pressKey(keyEnum.keyEnter, 0, 1);
//                }
//            }
//
//            //Наименование организации
//            pressKey(keyEnum.keyEnter, 0, 1);
//            registrationData = searchForKeyword("organization_name: ", keyWordArray);
//            sendData();
//            if (registrationData.equals("CANNOT FIND KEYWORD")) {
//                writeLogFile("В сценарии не указано наименование организации\n");
//                getScreenJson();
//                compare = screens.compareScreen(ScreenPicture.EMPTY_SCREEN);
//                if (compare) {
//                    writeLogFile("Экран наименования организации пустой, завершение регистрации невозможно\n");
//                    return -3;
//                } else
//                    pressKey(keyEnum.keyEnter, 0, 1);
//                sendData();
//            } else {
//                //получаем значения поля Наименование организации
//                List<ConfigFieldsEnum> list = new ArrayList<>();
//                list.add(ConfigFieldsEnum.ORGANIZATION_NAME);
//                List<String> valueFieldsList = getConfig(list);
//                if (valueFieldsList.size() != 0) {
//                    if (registrationData.equals(valueFieldsList.get(0)))
//                        pressKey(keyEnum.keyEnter, 0, 1);
//                    else {
//                        //очистка ранее введенных данных
//                        clearDisplay(valueFieldsList.get(0).length());
//                        //Ввод данных из сценария
//                        enterStringData(registrationData);
//                        pressKey(keyEnum.keyEnter, 0, 1);
//                    }
//                } else {
//                    //Ввод данных из сценария
//                    enterStringData(registrationData);
//                    pressKey(keyEnum.keyEnter, 0, 1);
//                }
//            }
//            //Адрес расчетов
//            pressKey(keyEnum.keyEnter, 0, 1);
//            registrationData = searchForKeyword("calculation_address: ", keyWordArray);
//            sendData();
//            if (registrationData.equals("CANNOT FIND KEYWORD")) {
//                writeLogFile("В сценарии не указан адрес расчетов\n");
//                getScreenJson();
//                compare = screens.compareScreen(ScreenPicture.EMPTY_SCREEN);
//                if (compare) {
//                    writeLogFile("Экран адреса расчетов пустой, завершение регистрации невозможно\n");
//                    return -4;
//                } else
//                    pressKey(keyEnum.keyEnter, 0, 1);
//                sendData();
//            } else {
//                //получаем значения поля Адрес рассчетов
//                List<ConfigFieldsEnum> list = new ArrayList<>();
//                list.add(ConfigFieldsEnum.CALCULATION_ADDRESS);
//                List<String> valueFieldsList = getConfig(list);
//                if (valueFieldsList.size() != 0) {
//                    if (registrationData.equals(valueFieldsList.get(0)))
//                        pressKey(keyEnum.keyEnter, 0, 1);
//                    else {
//                        //очистка ранее введенных данных
//                        clearDisplay(valueFieldsList.get(0).length());
//                        //Ввод данных из сценария
//                        enterStringData(registrationData);
//                        pressKey(keyEnum.keyEnter, 0, 1);
//                    }
//                } else {
//                    //Ввод данных из сценария
//                    enterStringData(registrationData);
//                    pressKey(keyEnum.keyEnter, 0, 1);
//                }
//                sendData();
//            }
//
//            //Место расчетов
//            pressKey(keyEnum.keyEnter, 0, 1);
//            registrationData = searchForKeyword("calculation_place: ", keyWordArray);
//            sendData();
//            if (registrationData.equals("CANNOT FIND KEYWORD")) {
//                writeLogFile("В сценарии не указано место расчетов\n");
//                getScreenJson();
//                compare = screens.compareScreen(ScreenPicture.EMPTY_SCREEN);
//                if (compare) {
//                    writeLogFile("Экран места расчетов пустой, завершение регистрации невозможно\n");
//                    return -5;
//                } else
//                    pressKey(keyEnum.keyEnter, 0, 1);
//                sendData();
//            } else {
//                //получаем значения поля Место рассчетов
//                List<ConfigFieldsEnum> list = new ArrayList<>();
//                list.add(ConfigFieldsEnum.CALCULATION_PLACE);
//                List<String> valueFieldsList = getConfig(list);
//                if (valueFieldsList.size() != 0) {
//                    if (registrationData.equals(valueFieldsList.get(0)))
//                        pressKey(keyEnum.keyEnter, 0, 1);
//                    else {
//                        //очистка ранее введенных данных
//                        clearDisplay(valueFieldsList.get(0).length());
//                        //Ввод данных из сценария
//                        enterStringData(registrationData);
//                        pressKey(keyEnum.keyEnter, 0, 1);
//                    }
//                } else {
//                    //Ввод данных из сценария
//                    enterStringData(registrationData);
//                    pressKey(keyEnum.keyEnter, 0, 1);
//                }
//                sendData();
//            }
//
//            //РН ККТ
//            pressKey(keyEnum.keyEnter, 0, 1);
//            sendData();
//            registrationData = searchForKeyword("reg_num: ", keyWordArray);
//            if (registrationData.equals("CANNOT FIND KEYWORD")) {
//                writeLogFile("В сценарии не указан РН ККТ\n");
//                getScreenJson();
//                compare = screens.compareScreen(ScreenPicture.EMPTY_SCREEN);
//                if (compare) {
//                    writeLogFile("Экран ввода РН ККТ пустой, завершение регистрации невозможно\n");
//                    return -6;
//                } else
//                    pressKey(keyEnum.keyEnter, 0, 1);
//                sendData();
//            } else {
//                //получаем значения поля Рег. номер
//                List<ConfigFieldsEnum> list = new ArrayList<>();
//                list.add(ConfigFieldsEnum.KKT_REG_NUM);
//                List<String> valueFieldsList = getConfig(list);
//                if (valueFieldsList.size() != 0) {
//                    if (registrationData.equals(valueFieldsList.get(0)))
//                        pressKey(keyEnum.keyEnter, 0, 1);
//                    else {
//                        //очистка ранее введенных данных
//                        clearDisplay(valueFieldsList.get(0).length());
//                    }
//                }
//                //Ввод данных из сценария
//                enterStringData(registrationData);
//                pressKey(keyEnum.keyEnter, 0, 1);
//                sendData();
//            }
//            getScreenJson();
//            compare = screens.compareScreen(ScreenPicture.WRONG_REG_NUMBER);
//            if (compare)
//                return -7;
//
//            //Версия ФФД
//            pressKey(keyEnum.keyEnter, 0, 1);
//            sendData();
///*            pressKey(keyEnum.keyEnter, 0, 1);
//            registrationData = searchForKeyword("ffd_ver: ", keyWordArray);
//            if (registrationData.equals("CANNOT FIND KEYWORD")) {
//                writeLogFile("В сценарии не указана версия ФФД, выставляем версию 1.05\n");
//                pressKey(keyEnum.key1, 0, 1);
//            }
//            else {
//                if (registrationData.equals("1.05"))
//                    pressKey(keyEnum.key1, 0, 1);
//                if (registrationData.equals("1.1"))
//                    pressKey(keyEnum.key2, 0, 1);
//            }
//            pressKey(keyEnum.keyEnter, 0, 1);*/
//
//            //СНО
//            pressKey(keyEnum.keyEnter, 0, 1);
//            sendData();
//            changeTaxSystems("tax_systems: ", keyWordArray);
//            pressKey(keyEnum.keyEnter, 0, 1);
//
//            //Признаки
//            pressKey(keyEnum.keyEnter, 0, 1);
//            sendData();
//            changeSignsKkt("signs: ", true, keyWordArray);
//            pressKey(keyEnum.keyEnter, 0, 1);
//            pressKey(keyEnum.keyEnter, 0, 1);
//            sendData();
//            return 0;
//        } else {
//            writeLogFile("Пункт регистрации не доступен");
//            return -1;
//        }
//    }

    //Выбор СНО
//    private void changeTaxSystems(String keyWord, List<String> keyWordList) {
//        String taxSystems = searchForKeyword(keyWord, keyWordList);
//        if (taxSystems.equals("CANNOT FIND KEYWORD")) {
//            writeLogFile("В файле сценария не выбраны СНО.\n Невозможно завершить регистрацию");
//            return;
//        }
//
//        int taxMaskLength = 6;
//        //Получаем выбранные на кассе СНО
//        List<ConfigFieldsEnum> list = new ArrayList<>();
//        list.add(ConfigFieldsEnum.TAX_SYSTEMS);
//        List<String> valueFieldsList = getConfig(list);
//
//        if (valueFieldsList.size() != 0) {
//            char[] changeTaxMask = getMaskFromConfigDbCashbox(taxMaskLength, valueFieldsList.get(0));
//
//            //оптимизировать ??
//            if (changeTaxMask[taxMaskLength - 1] == '1')
//                pressKey(keyEnum.key1, 0, 1);
//            if (changeTaxMask[taxMaskLength - 2] == '1')
//                pressKey(keyEnum.key2, 0, 1);
//            if (changeTaxMask[taxMaskLength - 3] == '1')
//                pressKey(keyEnum.key3, 0, 1);
//            if (changeTaxMask[taxMaskLength - 4] == '1')
//                pressKey(keyEnum.key4, 0, 1);
//            if (changeTaxMask[taxMaskLength - 5] == '1')
//                pressKey(keyEnum.key5, 0, 1);
//            if (changeTaxMask[taxMaskLength - 6] == '1')
//                pressKey(keyEnum.key6, 0, 1);
//            sendData();
//        }
//
//        Vector<String> taxSystemsTable = multiplieChoice(taxSystems);
//
//        //TODO check on workilng version!!!
//        for (String tmpStr : taxSystemsTable) {
//            switch (tmpStr) {
//                case "ОСН":
//                    pressKey(keyEnum.key1, 0, 1);
//                    break;
//                case "УСН доход":
//                    pressKey(keyEnum.key2, 0, 1);
//                    break;
//                case "УСН дох./расх.":
//                    pressKey(keyEnum.key3, 0, 1);
//                    break;
//                case "ЕНВД":
//                    pressKey(keyEnum.key4, 0, 1);
//                    break;
//                case "ЕСХН":
//                    pressKey(keyEnum.key5, 0, 1);
//                    break;
//                case "Патент":
//                    pressKey(keyEnum.key6, 0, 1);
//                    break;
//                default:
//                    break;
//            }
//        }
//        sendData();
//    }

    //Получение масок с кассы
    private char[] getMaskFromConfigDbCashbox(int maskLength, String resultFromCash) {
        char[] maskBin = new char[maskLength];
        for (int i = 0; i < maskLength; i++)
            maskBin[i] = '0';

        String binMaskString = Integer.toBinaryString(Integer.parseInt(resultFromCash));

        int charCount = maskLength - binMaskString.length();
        for (int i = 0; i < charCount; i++)
            maskBin[i] = '0';
        for (int i = 0; i < binMaskString.length(); i++) {
            maskBin[charCount] = binMaskString.charAt(i);
            charCount++;
        }

        return maskBin;
    }

    //Выбор признаков регистрации для опрелеленной версии, с определенным режимом
    private void changeSignsMode(Vector<String> signsTable, String modeKkt, String version, List<String> keyWordList) {
        boolean agentChange = false;
        boolean autoModeChange = false;

        for (String tmpStr : signsTable) {
            switch (tmpStr) {
                case "Шифрования": {
                    if (modeKkt.equals("Автономный"))
                        writeLogFile("В автономном режиме признак шифрования скрыт.\n");
                    if (modeKkt.equals("Передачи данных"))
                        pressKey(keyEnum.key1, 0, 1);
                    break;
                }
                case "Подакциз.товар": {
                    if (modeKkt.equals("Автономный"))
                        pressKey(keyEnum.key1, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKey(keyEnum.key2, 0, 1);
                    break;
                }
                case "Расч. за услуги": {
                    if (modeKkt.equals("Автономный"))
                        pressKey(keyEnum.key2, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKey(keyEnum.key3, 0, 1);
                    break;
                }
                case "Азартн.игры": {
                    if (modeKkt.equals("Автономный"))
                        pressKey(keyEnum.key3, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKey(keyEnum.key4, 0, 1);
                    break;
                }
                case "Лотерея": {
                    if (modeKkt.equals("Автономный"))
                        pressKey(keyEnum.key4, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKey(keyEnum.key5, 0, 1);
                    break;
                }
                case "Пл. агент": {
                    agentChange = true;
                    if (modeKkt.equals("Автономный"))
                        pressKey(keyEnum.key5, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKey(keyEnum.key6, 0, 1);
                    break;
                }
                case "БСО": {
                    if (modeKkt.equals("Автономный"))
                        pressKey(keyEnum.key6, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKey(keyEnum.key7, 0, 1);
                    break;
                }
            }
           /* if (version.equals("1.1")) {
                switch (tmpStr) {
                    case "Автомат. режим": {
                        autoModeChange = true;
                        if (modeKkt.equals("Автономный"))
                            pressKey(keyEnum.key8, 0, 1);
                        if (modeKkt.equals("Передачи данных"))
                            pressKey(keyEnum.key9, 0, 1);
                        break;
                    }

                    case "Прод интернет": {
                        if (modeKkt.equals("Автономный"))
                            pressKey(keyEnum.key, 0, 1);
                        if (modeKkt.equals("Передачи данных"))
                            pressKey(keyEnum.key9, 0, 1);
                        break;
                    }
                    case "Уст. принт. в автомате": {
                        if (modeKkt.equals("Автономный"))
                            pressKey(keyEnum.key9, 0, 1);
                        if (modeKkt.equals("Передачи данных"))
                            pressKey(keyEnum.key0, 0, 1);
                        break;
                    }
                }*/
        }
        sendData();

        if (agentChange) {
            pressKey(keyEnum.keyEnter, 0, 1);
            //Типы агентов
            String agentType;
            agentType = searchForKeyword("agent_type: ", keyWordList);
            if (agentType.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("Выбран признак агента, но не выбраны типы агентов, нажимаем продолжить без указания типов...\n");
                pressKey(keyEnum.keyEnter, 0, 2);
            }

            Vector<String> agentTypeTable = multiplieChoice(agentType);
            for (String agentTypeStr : agentTypeTable) {
                switch (agentTypeStr) {
                    case "Банковский платежный агент":
                        pressKey(keyEnum.key1, 0, 1);
                        break;
                    case "Банковский платежный субагент":
                        pressKey(keyEnum.key2, 0, 1);
                        break;
                    case "Платежный агент":
                        pressKey(keyEnum.key3, 0, 1);
                        break;
                    case "Платежный субагент":
                        pressKey(keyEnum.key4, 0, 1);
                        break;
                    case "Поверенный":
                        pressKey(keyEnum.key5, 0, 1);
                        break;
                    case "Комиссионер":
                        pressKey(keyEnum.key6, 0, 1);
                        break;
                    case "Агент":
                        pressKey(keyEnum.key7, 0, 1);
                        break;
                }
            }
            sendData();
            if (version.equals("1.1")) {
                if (autoModeChange) {
                    pressKey(keyEnum.keyEnter, 0, 2);
                    enterStringData(searchForKeyword("automat_number: ", keyWordList));
                    sendData();
                }
            }
        }
    }


//    //Выбор признаков регистрации
//    private void changeSignsKkt(String keyWord, boolean registrationFlag, List<String> keyWordList) {
//        String kktSigns = searchForKeyword(keyWord, keyWordList);
//        if (kktSigns.equals("CANNOT FIND KEYWORD"))
//            writeLogFile("Признаки ККТ не выбраны.\n");
//        else {
//            Vector<String> signsTable = multiplieChoice(kktSigns);
//            String registrationVer = null;
//            String registrationMode = null;
//            //Если выставлен флаг регистрации, то экраны смотрим в соответствии с файлом сценария
//            if (registrationFlag) {
//                registrationVer = searchForKeyword("ffd_ver: ", keyWordList);
//                registrationMode = searchForKeyword("mode_kkt: ", keyWordList);
//
//            } else {
//                //получаем версию ФФД и Режим работы ККТ с кассы
//                List<ConfigFieldsEnum> list = new ArrayList<>();
//                list.add(ConfigFieldsEnum.FFD_KKT_VER);
//                list.add(ConfigFieldsEnum.KKT_MODE);
//                List<String> valueFieldsList = getConfig(list);
//                if (valueFieldsList.size() != 0) {
//                    registrationVer = valueFieldsList.get(0);
//                    registrationMode = valueFieldsList.get(1);
//
//                    int ENCRYPTION_SIGN = 0, EXCISABLE_SIGN = 0, CLC_SERVICE_SIGN = 0, GAMBLING_SIGN = 0, LOTTERY_SIGN = 0, PAYING_AGENT_SIGN = 0;
//                    if (registrationVer.equals("2")) {
//                        registrationVer = "1.05";
//                        //убираем все ранее выбранные признаки
//                        list.clear();
//                        list.add(ConfigFieldsEnum.ENCRYPTION_SIGN);
//                        list.add(ConfigFieldsEnum.ENCRYPTION_SIGN);
//                        list.add(ConfigFieldsEnum.ENCRYPTION_SIGN);
//                        list.add(ConfigFieldsEnum.ENCRYPTION_SIGN);
//                        list.add(ConfigFieldsEnum.ENCRYPTION_SIGN);
//                        list.add(ConfigFieldsEnum.EXCISABLE_SIGN);
//                        list.add(ConfigFieldsEnum.CLC_SERVICE_SIGN);
//                        list.add(ConfigFieldsEnum.GAMBLING_SIGN);
//                        list.add(ConfigFieldsEnum.LOTTERY_SIGN);
//                        list.add(ConfigFieldsEnum.PAYING_AGENT_SIGN);
//
//                        valueFieldsList = getConfig(list);
//
//                        //TODO: Проверка граничных значений
//                        ENCRYPTION_SIGN = Integer.parseInt(valueFieldsList.get(0));
//                        EXCISABLE_SIGN = Integer.parseInt(valueFieldsList.get(1));
//                        CLC_SERVICE_SIGN = Integer.parseInt(valueFieldsList.get(2));
//                        GAMBLING_SIGN = Integer.parseInt(valueFieldsList.get(3));
//                        LOTTERY_SIGN = Integer.parseInt(valueFieldsList.get(4));
//                        PAYING_AGENT_SIGN = Integer.parseInt(valueFieldsList.get(5));
//                    }
//                    if (registrationVer.equals("3"))
//                        registrationVer = "1.1";
//
//                    if (registrationMode.equals("0"))
//                        registrationMode = "Передачи данных";
//                    if (registrationMode.equals("1"))
//                        registrationMode = "Автономный";
//
//                    if (registrationMode.equals("Передачи данных")) {
//                        if (ENCRYPTION_SIGN == 1)
//                            pressKey(keyEnum.key1, 0, 1);
//                        if (EXCISABLE_SIGN == 1)
//                            pressKey(keyEnum.key2, 0, 1);
//                        if (CLC_SERVICE_SIGN == 1)
//                            pressKey(keyEnum.key3, 0, 1);
//                        if (GAMBLING_SIGN == 1)
//                            pressKey(keyEnum.key4, 0, 1);
//                        if (LOTTERY_SIGN == 1)
//                            pressKey(keyEnum.key5, 0, 1);
//                        if (PAYING_AGENT_SIGN == 1)
//                            pressKey(keyEnum.key6, 0, 1);
//                    }
//                    if (registrationMode.equals("Автономный")) {
//                        if (EXCISABLE_SIGN == 1)
//                            pressKey(keyEnum.key1, 0, 1);
//                        if (CLC_SERVICE_SIGN == 1)
//                            pressKey(keyEnum.key2, 0, 1);
//                        if (GAMBLING_SIGN == 1)
//                            pressKey(keyEnum.key3, 0, 1);
//                        if (LOTTERY_SIGN == 1)
//                            pressKey(keyEnum.key4, 0, 1);
//                        if (PAYING_AGENT_SIGN == 1)
//                            pressKey(keyEnum.key5, 0, 1);
//                    }
//                }
//            }
//            changeSignsMode(signsTable, registrationMode, registrationVer, keyWordList);
//        }
//    }

    //множественный выбор
    private Vector<String> multiplieChoice(String str) {
        Vector<String> line = new Vector<>();
        String[] parts = (str + " ").split(";");
        for (String tmpStr : parts)
            line.add(tmpStr);

        return line;
    }

    //отчистка диспея
    private void clearDisplay(int length) {
        for (int i = 0; i < length; i++)
            pressKey(keyEnum.keyReversal, 0, 1);
    }

    //смена режима кассы
    private void changeKktMode(String keyWord, List<String> keyWordList) {
        //автономный режим
        String kktMode = searchForKeyword(keyWord, keyWordList);
        if (kktMode.equals("Автономный"))
            pressKey(keyEnum.key1, 0, 1);
        else {
            pressKey(keyEnum.key2, 0, 1);
            pressKey(keyEnum.keyEnter, 0, 1);
            String OFDName = searchForKeyword("ofd_name: ", keyWordList);
            if (OFDName.equals("CANNOT FIND KEYWORD"))
                writeLogFile("The input file does not contain the name of the OFD!\n");

            sendData();
            changeOFDName(OFDName, keyWordList);
        }
    }

    //смена ОФД
    private void changeOFDName(String OFDName, List<String> keyWordList) {
        pressKey(keyEnum.keyEnter, 0, 1);
        sendData();
        switch (OFDName) {
            case "Яндекс.ОФД": {
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                pressKey(keyEnum.keyEnter, 0, 2);
                pressKey(keyEnum.key2, 0, 1);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                break;
            }
            case "Первый ОФД": {
                pressKey(keyEnum.keyDown, 0, 1);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "ОФД-Я": {
                pressKey(keyEnum.keyDown, 0, 2);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "Такском": {
                pressKey(keyEnum.keyDown, 0, 3);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "СБИС ОФД": {
                pressKey(keyEnum.keyDown, 0, 4);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "КАЛУГА АСТРАЛ": {
                pressKey(keyEnum.keyDown, 0, 5);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "Корус ОФД": {
                pressKey(keyEnum.keyDown, 0, 6);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "Эвотор": {
                pressKey(keyEnum.keyDown, 0, 7);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "Электронный экспресс": {
                pressKey(keyEnum.keyDown, 0, 8);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "OFD.RU": {
                pressKey(keyEnum.keyDown, 0, 9);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "СКБ Контур": {
                pressKey(keyEnum.keyDown, 0, 10);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                autoEnterDataOFD();
                break;
            }
            case "Другой": {
                pressKey(keyEnum.keyUp, 0, 1);
                pressKey(keyEnum.keyEnter, 0, 1);
                sendData();
                String regOFDNameOther = searchForKeyword("reg_OFD_name_other: ", keyWordList);
                if (regOFDNameOther.equals("CANNOT FIND KEYWORD")) {
                    writeLogFile("The input file does not contain the name of the fiscal data operator for the selected item \"other\"!\n");
                }
                enterStringData(searchForKeyword("ofd_other_name: ", keyWordList));
                pressKey(keyEnum.keyEnter, 0, 2);
                sendData();
                //ввод данных, если выбран "Другой" ОФД
                manualEnterDataOFD(keyWordList);
                break;
            }
            default:
                break;
        }
    }

    //автоввод данных
    private void autoEnterDataOFD() {
        pressKey(keyEnum.keyEnter, 0, 7);
        sendData();
    }

    //ручной ввод данных ОФД
    private void manualEnterDataOFD(List<String> keyWordList) {
        //------------------------------------------------ИНН ОФД-------------------------------------------------------
        String innOFD = searchForKeyword("ofd_inn: ", keyWordList);
        if (innOFD.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("The input file does not contain INN of the fiscal data operator for the selected item.\n");
            return;
        }
        enterStringData(innOFD);
        pressKey(keyEnum.keyEnter, 0, 2);
        sendData();
        //------------------------------------------------Адрес сервера ОФД-------------------------------------------------------
        String addressOFD = searchForKeyword("ofd_server_address: ", keyWordList);
        if (addressOFD.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("The input file does not contain server address of the fiscal data operator for the selected item.\n");
            return;
        }
        enterStringData(addressOFD);
        pressKey(keyEnum.keyEnter, 0, 1);
        //добавить проверку экрана: если "адрес ОФД не найден, то добавить дополнительное нажатие на кнопку ввода

        pressKey(keyEnum.keyEnter, 0, 2);
        sendData();
        //------------------------------------------------Порт ОФД-------------------------------------------------------
        String portOFD = searchForKeyword("ofd_server_port: ", keyWordList);
        if (portOFD.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("The input file does not contain port of the fiscal data operator for the selected item.\n");
            return;
        }
        enterStringData(portOFD);
        pressKey(keyEnum.keyEnter, 0, 2);
        sendData();
        //---------------------------------------------Адрес проверки чека------------------------------------------------
        String checkReceiptOFD = searchForKeyword("ofd_check_reciept_address: ", keyWordList);
        if (checkReceiptOFD.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("The input file does not contain port of the fiscal data operator for the selected item.\n");
            return;
        }
        enterStringData(checkReceiptOFD);
        pressKey(keyEnum.keyEnter, 0, 2);
        sendData();
    }


    //Нажатие на кнопку (ввод данных) в зависимости от символа
    public void enterStringData(String str) {
        int keypadMode = getKeypadMode();

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

                /* // Английские символы + цифры
                if (keypadMode == ENGLISH + NUMBERS)
                {
                } */
            }
        }
        sendData();
    }



    //Поиск ключевого слова в заданной коллекции
    public String searchForKeyword(String keyWord, List<String> keyWordArray) {
        String tmpStr;

        for (String keyWordStr : keyWordArray) {
            tmpStr = keyWordStr;
            int tmpFind = tmpStr.indexOf(keyWord);

            if (tmpFind != -1) {
                int lenKeyWord;
                lenKeyWord = keyWord.length();
                tmpStr = tmpStr.substring(lenKeyWord);
                return tmpStr;
            }
        }
        return "CANNOT FIND KEYWORD";
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

    //Запись в лог
    private void writeLogFile(String text) {
        try {
            //Текущая дата + текст
            String strToFile = Calendar.getInstance().getTime() + "    " + text + "\n";
            Files.write(Paths.get("log.txt"), strToFile.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Преобразование полученного массива от сервера в картинку
    private void savePicture(int[] lcdScreen){
        if (lcdScreen != null) {
            byte byteToFile[] = new byte[lcdScreen.length];
            for (int i = 0; i < lcdScreen.length; i++)
                byteToFile[i] = (byte) lcdScreen[i];
            try {
                FileOutputStream fos = new FileOutputStream(new File("reciveData\\tmpScreen.bmp"));
                fos.write(byteToFile);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void rebootCashBox(){
        sendCommandSsh("reboot");
        trySleep(20000);
        start();
    }
}
