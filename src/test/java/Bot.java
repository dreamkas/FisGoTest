import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Класс для формирования команды отправки Json и отправки команд на сервер
 */
public class Bot {

    private CashBox cashBox;

    private Keypad keypad;
    private KeypadMode keypadMode;

    public Bot(CashBox cashBox) {
        this.cashBox = cashBox;
        this.keypad = new Keypad(cashBox);
        this.keypadMode = new KeypadMode();
    }

    //данные для формирование команды, которая будет передана на сервер
    @Getter
    private int taskId;
    //объект для сравнения полученного экрана с ожидаемым
    private Screens screens = new Screens();
    //формирование данных
    private List<Tasks> tasksList = new ArrayList<>();
    private TCPSocket tcpSocket = new TCPSocket();
    private KeyEnum keyEnum = new KeyEnum();
    // добавляем таск на нажатие кнопки
    //  параметры:
    // key1 - первая кнопка, которая будет нажата;
    // key2 - вторая кнопка, которая будет нажата (сейчас не испоьзуется);
    // action - действие, которое будет выполнять сервер (возможные действия описаны в классе KeypadActionEnum)
    public void pressButton(int key1, int key2, KeypadActionEnum action) {
        taskId++;
        DataKeypad dataKeypad = new DataKeypad(key1, key2, action);
        Tasks task = new Tasks(taskId, CommandEnum.KEYPAD_ACTION, dataKeypad, null);
        tasksList.add(task);
    }

    // добавляем таск на получение экрана (используется без параметров)
    public void getScreenJson() {
        taskId++;
        Tasks task = new Tasks(taskId, CommandEnum.LCD_SCREEN, null, null);
        tasksList.add(task);
        tcpSocket.sendDataToSocket(getTaskId(), resultJson());
    }

    // добавляем таск на получение режима работы клавиатуры (используется без параметров)
    public void getKeypadModeJson() {
        taskId++;
        Tasks task = new Tasks(taskId, CommandEnum.KEYPAD_MODE, null, null);
        tasksList.add(task);
        tcpSocket.sendDataToSocket(getTaskId(), resultJson());
    }

    // добавляем таск на получение данных из конфига или биоса
    // параметры:
    // список полей, значение которх необходимо получить. Список полей можно получить в ConfigFieldsEnum
    public List<String> cfgGetJson(List<ConfigFieldsEnum> getFields) {
        //пока передаю в класс tcpSocket список полей так, подумаю как реализовать это лучше
        tcpSocket.setConfigFieldsEnum(getFields);
        taskId++;
        CfgData cfgData = new CfgData(getFields);
        Tasks task = new Tasks(taskId, CommandEnum.CFG_GET, null, cfgData);
        tasksList.add(task);
        tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        return tcpSocket.getValueConfigFields();
    }

    // добавляем таск на закрытие сессии (используется только в классе TCPSocket, в функции closeSocket()
    public void closeSessionJson() {
        taskId++;
        Tasks task = new Tasks(taskId, CommandEnum.CLOSE_SESSION, null, null);
        tasksList.add(task);
    }

    // составляем Json из сформированных тасок, используется только в классе  TCPSocket, в функцях sendDataToSocket() и closeSocket()
    public String resultJson() {
        CreateCommandJson createCommandJson = new CreateCommandJson(tasksList, cashBox.UUID);

        GsonBuilder builder  = new GsonBuilder();
        Gson gson = builder.create();

        String jsonStr = gson.toJson(createCommandJson);
        System.out.println("jsonStr = " + jsonStr);

        taskId = 0;
        tasksList.clear();
        return jsonStr;
    }

    //Чтение параметров сценария из файла
    public List<String> readDataScript(String fileName) {
        List<String> list = new ArrayList<>();
        try{
            FileInputStream fstream = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null){
                list.add(strLine);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return list;
    }

    //private Keypad keypad = new Keypad();

    //Ввод пароля
    public int enterPassword(List <String> keyWordArray) {
        writeLogFile("Выполняется функция ввода пароля.");
        getScreenJson();
        tcpSocket.sendDataToSocket(getTaskId(),resultJson());
        boolean compare = screens.compareScreen(ScreenPicture.PASSWORD);
        //если полученный экран с кассы совпадает с экраном ввода пароля, то выполняем if
        if (compare) {
            writeLogFile("Открыт экран ввода пароля.");
            String pass = searchForKeyword("password: ", keyWordArray);
            if (pass.equals("CANNOT FIND KEYWORD"))
                writeLogFile("Пароль не найден в файле сценария.");
            strToKeypadConvert(pass);
            tcpSocket.sendDataToSocket(getTaskId(),resultJson());
            getScreenJson();
            tcpSocket.sendDataToSocket(getTaskId(),resultJson());
            compare = screens.compareScreen(ScreenPicture.INCORRECT_PASSWORD);
            if (compare) {
                writeLogFile("Введен неверный пароль.");
                return -1;
            }
            else {
                writeLogFile("Введен верный пароль.");
                return 0;
            }
        } else {
            writeLogFile("Экран ввода пароля не открыт.");
            return -2;
        }
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
            strToKeypadConvert(line.get(0));
            tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        }
    }

    //Открытие смены
    public int openShift() {
        //делаем выборку их конфига на кассе, проверем, открыта смена или нет
        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);
        line.add(ConfigFieldsEnum.SHIFT_TIMER);
        List<String> valueConfigList = cfgGetJson(line);

        if (valueConfigList.get(0).equals("0") && (Integer.parseInt(valueConfigList.get(1)) == 0)) {
            pressKeyBot(keyEnum.keyMenu, 0, 1);
            pressKeyBot(keyEnum.key1, 0, 1);
            tcpSocket.sendDataToSocket(getTaskId(),resultJson());
            getScreenJson();
            boolean compare = screens.compareScreen(ScreenPicture.OPEN_SHIFT_MENU);
            if (compare) {
                //добавить обработку даты открытия смены
                String getPassCommand = "date '+%d%m%y%H%M'\n";
                DataFromCashbox dataFromCashbox = new DataFromCashbox();
                dataFromCashbox.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
                List<String> dateStr = dataFromCashbox.executeListCommand(getPassCommand);
                dataFromCashbox.disconnectSession();
                pressKeyBot(keyEnum.keyEnter, 0, 2);
                tcpSocket.sendDataToSocket(getTaskId(),resultJson());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getScreenJson();
                compare = screens.compareScreen(ScreenPicture.FREE_SALE_MODE);
                if (compare) {
                    return 0;
                } else {
                    return -3;
                }
            } else {
                writeLogFile("Пункт открытия смены не доступен");
                return -2;
            }
        }
        else
            return -1;
    }



    //Нажатие на кнопку (ввод данных) в зависимости от символа
    private void strToKeypadConvert(String str) {
        getKeypadModeJson();
        tcpSocket.sendDataToSocket(getTaskId(), resultJson());
        //tcpSocket..serverGetKepadMode();
        int keypad_mode = TCPSocket.getKeypadMode();

        Short keyNumPrew = 40, keyNum = 0, pressCount;
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
                if (keypad_mode == keypadMode.CYRILLIC) {
                    for (int k = 0; k < keys[j].rus_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].rus_code.get(k)) {
                            keyNum = keys[j].key_code;
                            //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                            /*if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);*/
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                }

                //Если англ
                if (keypad_mode == keypadMode.ENGLISH) {
                    for (int k = 0; k < keys[j].eng_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].eng_code.get(k)) {
                            keyNum = keys[j].key_code;
                            //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                            /*if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);*/
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                }

                // Если спец символы
                if (keypad_mode == keypadMode.SPEC_SYMBOLS) {
                    for (int k = 0; k < keys[j].spec_sym_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].spec_sym_code.get(k)) {
                            keyNum = keys[j].key_code;
                            //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                            /*if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);*/
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                }

                // Если цифры или Свободный режим
                if ((keypad_mode == keypadMode.NUMBERS) || (keypad_mode == keypadMode.FREE_MODE)) {
                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount = 1;
                        pressKeyBot(keyNum, 0, pressCount);
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
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                        //+
                        if (charsetNumber == 43) {
                            keyNum = 22;
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                        //-
                        if (charsetNumber == 45) {
                            keyNum = 30;
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                        ///
                        if (charsetNumber == 42) {
                            keyNum = 38;
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                }

                //Если русский + цифры, keypadMode == 5
                if (keypad_mode == keypadMode.CYRILLIC + keypadMode.NUMBERS) {
                    for (int k = 0; k < keys[j].rus_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].rus_code.get(k)) {
                            keyNum = keys[j].key_code;
                            //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                            /*if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);*/
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }

                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount++;
                        //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                        /*if (keyNumPrew.equals(keyNum))
                            sleepMiliSecond(1500);*/
                        pressKeyBot(keyNum, 0, pressCount);
                        exit = true;
                    }
                }

                // Смешанный ввод CYRILLIC + ENGLISH + NUMBERS = 7;
                if (keypad_mode == (keypadMode.CYRILLIC + keypadMode.ENGLISH + keypadMode.NUMBERS)) {
                    // проверяем таблицу спецсимволов
                    for (int k = 0; k < keys[j].spec_sym_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].spec_sym_code.get(k)) {
                            keyNum = keys[j].key_code;
                            //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                           /* if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);*/
                            pressKeyBot(keyNum, 0, pressCount);
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
                            //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                            /*if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);*/
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                    for (int k = 0; k < keys[j].eng_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].eng_code.get(k)) {
                            keyNum = keys[j].key_code;
                            //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                            /*if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);*/
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount++;
                        //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                        /*if (keyNumPrew.equals(keyNum))
                            sleepMiliSecond(1500);*/
                        pressKeyBot(keyNum, 0, pressCount);
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
    }

    public void sendTasks(String jsonCommand){
        tcpSocket.sendDataToSocket(getTaskId(), jsonCommand);
    }

    //Поиск ключевого слова в заданной коллекции
    private String searchForKeyword(String keyWord, List <String> keyWordArray) {
        String tmpStr;

        for (String keyWordStr: keyWordArray) {
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

    public void pressKeyBot(int keyNum, int keyNum2,  int pressCount) {
        for (int i = 0; i < pressCount; i++) {
            pressButton(keyNum, keyNum2, KeypadActionEnum.KEY_DOWN);
        }
       // tcpSocket.sendDataToSocket(getTaskId(),resultJson());
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
}
