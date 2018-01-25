import org.junit.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by v.bochechko on 04.12.2017.
 */
public class CashTest {
    Calendar curdate = Calendar.getInstance();

    private static final String CashBoxType = "DreamkasF"; //DreamkasRF
    private static final String CashboxIP = "192.168.243.6";//15";
    private static final String CashboxPort = "3425";

    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private static final int PORT = 22;

    private Config config = new Config();

    private Screens screens = new Screens();
    private ScreenPicture screenPicture;

    private Vector<String> instruction_table = new Vector<String>();
    private Keypad keypad = new Keypad();
    private KeypadMode keypadMode = new KeypadMode();
    private static int keypad_mode = 0; //= keypadMode.FREE_MODE;//SPEC_SYMBOLS;//ENGLISH;//CYRILLIC;
    private KeyEnum keyEnum = new KeyEnum();

   // private Sale sale = new Sale();
    private TCPSocket tcpSocket = new TCPSocket();

    private int countGoods = 0;

    public CashTest() throws FileNotFoundException {
    }

    public void setCountGoods(int countGoodsTmp) {
        countGoods = countGoodsTmp;
    }

    DateFromCashbox manager = new DateFromCashbox();

    FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

    @Before
    public void before_test_clear_cashbox() {
        if (CashBoxType.equals("DreamkasF")) config.setCashType(0);
        if (CashBoxType.equals("DreamkasRF")) config.setCashType(1);

        keyEnum.initKeyEnum();
        tcpSocket.setFlagGetScreen(true);
        tcpSocket.createSocket(CashboxIP, Integer.parseInt(CashboxPort));
        manager.initSession(CashboxIP, USERNAME, PORT, PASSWORD);
        /*****************************************************************
         ****************************Предусловия**************************
         *************Сброс кассы (выполнение тех. обнуления)*************
         ****************************************************************/
        //проверяем, что открыт экран ввода пароля
        /*
        boolean compare = compareScreen(screenPicture.PASSWORD);
        //если полученный экран с кассы совпадает с экраном ввода пароля, то выполняем if
        if (compare) {
            //делаем выборку их БД users на кассе, получаем пароль одного из них
            String getPassCommand = "echo \"attach '/FisGo/usersDb.db' as users; " +
                    "select PASS from users.USERS limit 1;\" | sqlite3 /FisGo/usersDb.db\n";
            List <String> line = CashBoxConnect(getPassCommand);
            //вводим пароль на кассе
            strToKeypadConvert(line.get(0));
        }
        //делаем тех. обнуление на кассе
        techNull();
        sleepMiliSecond(20000);
        tcpSocket.socketClose();

        //перезапускаем фискат
        CashBoxConnect("/sbin/reboot");
        sleepMiliSecond(25000);

        manager.initSession(CashboxIP, USERNAME, PORT, PASSWORD);
        tcpSocket.setReadAllInstruction(false);
        tcpSocket.setFlagGetScreen(true);
        tcpSocket.createSocket(CashboxIP, Integer.parseInt(CashboxPort));
        /*****************************************************************/
    }

    @After
    public void after_test_clear_cashbox() {
        manager.disconnectSession();
        tcpSocket.socketClose();
    }

    private List <String> CashBoxConnect (String command) {
        return manager.executeListCommand(command);
    }

    /***********************************Тесты на ввод пароля********************************************/
    @Test
    public void incorrect_password() throws IOException {
        List <String> listScript = new ArrayList<>();
        listScript.add("password: 1235");
        enterPassword(listScript);

        String strFromFile = br.readLine();
        assertEquals(screens.incorrectPasswodScreen, strFromFile);
    }
    /***********************************Тесты на ввод пароля********************************************/
    /***************************Тест на ввод пароля, ККТ не зарегистирована*****************************/
    @Test
    public void correct_password() throws IOException {
        List <String> listScript = new ArrayList<>();
        listScript.add("password: 1234");
        enterPassword(listScript);

        String strFromFile = br.readLine();
        assertEquals(screens.menuAfterPasswdScreen, strFromFile);
    }
    /***************************************************************************************************/

    /***********************************Тесты на регистрацию********************************************/
    /************************Тест на регистрацию через ККТ, в автономном режиме*************************/
    @Test
    public void correct_registration_autonomic() throws IOException {
        //проверяем, что stage кассы = 0
        String getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                "select STAGE from config.CONFIG;\"" +
                " | sqlite3 /FisGo/configDb.db\n";
        List <String> line = CashBoxConnect(getPassCommand);
        if (!line.get(0).equals("2")) {
            //проверяем, что открыт экран ввода пароля
            boolean compare = compareScreen(screenPicture.PASSWORD);
            //если полученныйэкран с кассы совпадает с экраном ввода пароля, то выполняем if
            if (compare) {
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                getPassCommand = "echo \"attach '/FisGo/usersDb.db' as users; " +
                        "select PASS from users.USERS limit 1;\" | sqlite3 /FisGo/usersDb.db\n";
                line = CashBoxConnect(getPassCommand);
                //вводим пароль на кассе
                strToKeypadConvert(line.get(0));
            }

            List<String> listScript = readDataScript("src\\test\\resourses\\registration_correct_autonomic.txt");

            int testResul = registration(listScript);
            System.out.println("testResul = " + testResul);
            if (testResul == -1)
                fail("Пункт регистрации недоступен в меню");
            if (testResul == -2)
                fail("Пустой ИНН в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -3)
                fail("Пустое наименование организации в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -4)
                fail("Пустой адрес рассчетов в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -5)
                fail("Пустое место рассчетов в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -6)
                fail("Пустой РН ККТ в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -7)
                fail("Неверный РН ККТ в пункте регистрации.\nЗавершить регистрацию невнозможно.");

            getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                    "select STAGE from config.CONFIG;\"" +
                    " | sqlite3 /FisGo/configDb.db\n";
            line = CashBoxConnect(getPassCommand);
            if (line.get(0).equals("2")) {
                pressKeyBot(keyEnum.key1, 0, 1);
                String strFromFile = br.readLine();
                assertEquals(strFromFile, screens.reRegistrationMenuScreen);
            }
            else
                fail("После регистрации stage != 2 ");
        }
        else
            fail("Stage = 2, Касса зарегистрирована!");
    }
    /************************Тест на регистрацию через ККТ, в автономном режиме*************************/
    @Test
    public void correct_registration_not_autonomic() throws IOException {
        //проверяем, что stage кассы = 0
        String getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                "select STAGE from config.CONFIG;\"" +
                " | sqlite3 /FisGo/configDb.db\n";
        List <String> line = CashBoxConnect(getPassCommand);
        if (!line.get(0).equals("2")) {
            //проверяем, что открыт экран ввода пароля
            boolean compare = compareScreen(screenPicture.PASSWORD);
            //если полученныйэкран с кассы совпадает с экраном ввода пароля, то выполняем if
            if (compare) {
                //    System.out.println("in compare");
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                getPassCommand = "echo \"attach '/FisGo/usersDb.db' as users; " +
                        "select PASS from users.USERS limit 1;\" | sqlite3 /FisGo/usersDb.db\n";
                line = CashBoxConnect(getPassCommand);
                //вводим пароль на кассе
                strToKeypadConvert(line.get(0));
            }

            List<String> listScript = readDataScript("src\\test\\resourses\\correct_registration_not_autonomic.txt");

            int testResul = registration(listScript);
            if (testResul == -1)
                fail("Пункт регистрации недоступен в меню");
            if (testResul == -2)
                fail("Пустой ИНН в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -3)
                fail("Пустое наименование организации в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -4)
                fail("Пустой адрес рассчетов в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -5)
                fail("Пустое место рассчетов в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -6)
                fail("Пустой РН ККТ в пункте регистрации.\nЗавершить регистрацию невнозможно.");
            if (testResul == -7)
                fail("Неверный РН ККТ в пункте регистрации.\nЗавершить регистрацию невнозможно.");

            getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                    "select STAGE from config.CONFIG;\"" +
                    " | sqlite3 /FisGo/configDb.db\n";
            line = CashBoxConnect(getPassCommand);
            if (line.get(0).equals("2")) {
                pressKeyBot(keyEnum.key1, 0, 1);
                String strFromFile = br.readLine();
                assertEquals(strFromFile, screens.reRegistrationMenuNotAutonomicScreen);
            }
            else
                fail("После регистрации stage != 2 ");
        }
        else
            fail("Stage = 2, Касса зарегистрирована!");
    }
    /***************************************************************************************************/

    /***********************************Тесты на перерегистрацию****************************************/
    /*********************ККТ зарегана в автономном режиме, изменяем ланные юр.лица*********************/
    @Test
    public void correct_re_registration_legal_entity() throws IOException {
        //проверяем, что stage кассы = 2
        String getStage = "echo \"attach '/FisGo/configDb.db' as config; " +
                "select STAGE from config.CONFIG;\"" +
                " | sqlite3 /FisGo/configDb.db\n";
        List<String> line = CashBoxConnect(getStage);

        //проверяем, что открыт экран ввода пароля
        boolean compare = compareScreen(screenPicture.PASSWORD);
        //если полученныйэкран с кассы совпадает с экраном ввода пароля, то выполняем if
        if (compare) {
            //делаем выборку их БД users на кассе, получаем пароль одного из них
            String getPass = "echo \"attach '/FisGo/usersDb.db' as users; " +
                    "select PASS from users.USERS limit 1;\" | sqlite3 /FisGo/usersDb.db\n";
            line = CashBoxConnect(getPass);
            //вводим пароль на кассе
            strToKeypadConvert(line.get(0));
        }

        //регистрируем кассу, если она не зарегистирована
        if (!line.get(0).equals("2")) {
            List<String> listScript = readDataScript("src\\test\\resourses\\registration_correct_autonomic.txt");
            int testResul = registration(listScript);
            switch (testResul) {
                case -1:
                    fail("Пункт регистрации недоступен в меню");
                    break;
                case -2:
                    fail("Пустой ИНН в пункте регистрации.\nЗавершить регистрацию невнозможно.");
                    break;
                case -3:
                    fail("Пустое наименование организации в пункте регистрации.\nЗавершить регистрацию невнозможно.");
                    break;
                case -4:
                    fail("Пустой адрес рассчетов в пункте регистрации.\nЗавершить регистрацию невнозможно.");
                    break;
                case -5:
                    fail("Пустое место рассчетов в пункте регистрации.\nЗавершить регистрацию невнозможно.");
                    break;
                case -6:
                    fail("Пустой РН ККТ в пункте регистрации.\nЗавершить регистрацию невнозможно.");
                    break;
                case -7:
                    fail("Неверный РН ККТ в пункте регистрации.\nЗавершить регистрацию невнозможно.");
                    break;
                default:
                    break;
            }
        }

        line = CashBoxConnect(getStage);
        if (line.get(0).equals("2")) {
            List<String> listScript = readDataScript("src\\test\\resourses\\correct_reregistration_legal_entity.txt");
            int testResul = re_registrationLegalEntity(listScript);

            line.clear();
            String getData = "echo \"attach '/FisGo/configDb.db' as config; " +
                    "select ORGANIZATION_NAME from config.CONFIG;" +
                    "select CALCULATION_ADDRESS from config.CONFIG;" +
                    "select CALCULATION_PLACE from config.CONFIG;\"" +
                    " | sqlite3 /FisGo/configDb.db\n";
            line = CashBoxConnect(getData);

            List<String> dataFromFile = new ArrayList<>();
            String tmpAddDataStr = searchForKeyword("organization_name: ", listScript);
            dataFromFile.add(tmpAddDataStr);
            tmpAddDataStr = searchForKeyword("calculation_address: ", listScript);
            dataFromFile.add(tmpAddDataStr);
            tmpAddDataStr = searchForKeyword("calculation_place: ", listScript);
            dataFromFile.add(tmpAddDataStr);

            if (line.equals(dataFromFile)) {
                line.clear();
                String strFromFile = br.readLine();
                if (testResul == 0)
                    assertEquals(strFromFile, screens.reRegistrationMenuScreen);
                else
                    fail("Результат перерегистрации не равен 0.");
            }
            else
                fail("Данные в конфиге не совпадают с данными в сценарии.");
        }
        else
            fail("Stage != 2, Касса незарегистрирована!");
    }
    /***************************************Тесты на продажу********************************************/
    /****************ККТ в учебном режиме, продажа - приход, наличные, все виды товаров*****************/
    @Test
    public void sale_advent_learning_mode() {
        //проверяем, что stage кассы = 0
        String getStageCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                "select STAGE from config.CONFIG;\"" +
                " | sqlite3 /FisGo/configDb.db\n";
        List<String> line = CashBoxConnect(getStageCommand);
  //      if (line.get(0).equals("0")) {
            //проверяем, что открыт экран ввода пароля
            boolean compare = compareScreen(screenPicture.PASSWORD);
            //если полученный экран с кассы совпадает с экраном ввода пароля, то выполняем if
            if (compare) {
                //    System.out.println("in compare");
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                String getPassCommand = "echo \"attach '/FisGo/usersDb.db' as users; " +
                        "select PASS from users.USERS limit 1;\" | sqlite3 /FisGo/usersDb.db\n";
                line = CashBoxConnect(getPassCommand);
                //вводим пароль на кассе
                strToKeypadConvert(line.get(0));
            }

            line.clear();
            //делаем выборку их конфига на кассе, проверем, открыта смена или нет
            String getOpenShiftCommand = "echo \"attach '/FisGo/configDb.db' as users; " +
                    "select OPEN_SHIFT_DATE from config.CONFIG;\" | sqlite3 /FisGo/configDb.db\n";
            line = CashBoxConnect(getOpenShiftCommand);
            if (line.isEmpty())
                openShift();

            //добавляем в БД товаров все виды товаров
            addGoodsOnCash();

            line.clear();

            //читаем из файла сценарий пробития чека
            List<String> listScript = readDataScript("src\\test\\resourses\\sale_advent_learning_mode.txt");

    /*    }
        else {
            fail("Касса не в учебном режиме");
        }*/
    }

    /***************************************************************************************************/

    //Чтение параметров сценария из файла
    private List<String> readDataScript(String fileName) {
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
    //Ввод пароля
    private void enterPassword(List <String> keyWordArray) {
        writeLogFile("Выполняется функция ввода пароля.");
        boolean compare = compareScreen(screenPicture.PASSWORD);
        //если полученный экран с кассы совпадает с экраном ввода пароля, то выполняем if
        if (compare) {
            writeLogFile("Открыт экран ввода пароля.");
            String pass = searchForKeyword("password: ", keyWordArray);
            if (pass.equals("CANNOT FIND KEYWORD"))
                writeLogFile("Пароль не найден в файле сценария.");
            strToKeypadConvert(pass);
            compare = compareScreen(screenPicture.INCORRECT_PASSWORD);
            if (compare) writeLogFile("Введен неверный пароль.");
            else writeLogFile("Введен верный пароль.");
        } else {
            writeLogFile("Экран ввода пароля не открыт.");
        }
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e ) {
            e.printStackTrace();
        }
    }
    //Регистрация
    private int registration(List <String> keyWordArray) {
        writeLogFile("Выполняется функция регистрации.");
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        //если открыто меню Регистрация ККТ и доступен пункт меню Регистрация
        boolean compare = compareScreen(screenPicture.MENU_REGISTRATION);
        if (compare) {
            writeLogFile("Пункт меню Регистрация доступен.");
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            sleepMiliSecond(1000);

            //тип регистрации
            String registrationData = searchForKeyword("registration_type: ", keyWordArray);
            // проверка экрана загрузки данных из кабинета
            //TODO: доделать регистрацию из кабинета
            compare = compareScreen(screenPicture.GET_REGISTRATION_DATA_FROM_CABINET);
            if (compare) {
                writeLogFile("Открыто окно загрузки рег. данных из кабинета");
                if (!registrationData.equals("Регистрация через Кабинет"))
                    pressKeyBot(keyEnum.keyCancel, 0, 1);
                else
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
            }

            pressKeyBot(keyEnum.keyEnter, 0, 1);
            //Выбор режима работы ККТ
            changeKktMode("mode_kkt: ", keyWordArray);

            //ИНН организации
            pressKeyBot(keyEnum.keyEnter, 0, 2);
            registrationData = searchForKeyword("organization_inn: ", keyWordArray);
            if (registrationData.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("В сценарии не указано ИНН организации\n");
                compare = compareScreen(screenPicture.EMPTY_SCREEN);
                if (compare) {
                    writeLogFile("Экран ИНН пустой, завершение регистрации невозможно\n");
                    return -2;
                } else
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            else {
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                String getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                        "select ORGANIZATION_INN from config.CONFIG;\"" +
                        " | sqlite3 /FisGo/configDb.db\n";
                List <String> line = CashBoxConnect(getPassCommand);
                if (line.size() != 0) {
                    System.out.println("line.get(0) = " + line.get(0));
                    if (registrationData.equals(line.get(0)))
                        pressKeyBot(keyEnum.keyEnter, 0, 1);
                    else {
                        //очистка ранее введенных данных
                        clearDisplay(line.get(0).length());
                        //Ввод данных из сценария
                        strToKeypadConvert(registrationData);
                        pressKeyBot(keyEnum.keyEnter, 0, 1);
                    }
                }
                else {
                    //Ввод данных из сценария
                    strToKeypadConvert(registrationData);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
            }

            //Наименование организации
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            registrationData = searchForKeyword("organization_name: ", keyWordArray);
            if (registrationData.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("В сценарии не указано наименование организации\n");
                compare = compareScreen(screenPicture.EMPTY_SCREEN);
                if (compare) {
                    writeLogFile("Экран наименования организации пустой, завершение регистрации невозможно\n");
                    return -3;
                }
                else
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            else {
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                String getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                        "select ORGANIZATION_NAME from config.CONFIG;\"" +
                        " | sqlite3 /FisGo/configDb.db\n";
                List<String> line = CashBoxConnect(getPassCommand);
                if (line.size() != 0) {
                    if (registrationData.equals(line.get(0)))
                        pressKeyBot(keyEnum.keyEnter, 0, 1);
                    else {
                        //очистка ранее введенных данных
                        clearDisplay(line.get(0).length());
                        //Ввод данных из сценария
                        strToKeypadConvert(registrationData);
                        pressKeyBot(keyEnum.keyEnter, 0, 1);
                    }
                }
                else {
                    //Ввод данных из сценария
                    strToKeypadConvert(registrationData);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
            }

            //Адрес расчетов
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            registrationData = searchForKeyword("calculation_address: ", keyWordArray);
            if (registrationData.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("В сценарии не указан адрес расчетов\n");
                compare = compareScreen(screenPicture.EMPTY_SCREEN);
                if (compare) {
                    writeLogFile("Экран адреса расчетов пустой, завершение регистрации невозможно\n");
                    return -4;
                }
                else
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            else {
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                String getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                        "select CALCULATION_ADDRESS from config.CONFIG;\"" +
                        " | sqlite3 /FisGo/configDb.db\n";
                List<String> line = CashBoxConnect(getPassCommand);
                if (line.size() != 0) {
                    if (registrationData.equals(line.get(0)))
                        pressKeyBot(keyEnum.keyEnter, 0, 1);
                    else {
                        //очистка ранее введенных данных
                        clearDisplay(line.get(0).length());
                        //Ввод данных из сценария
                        strToKeypadConvert(registrationData);
                        pressKeyBot(keyEnum.keyEnter, 0, 1);
                    }
                }
                else {
                    //Ввод данных из сценария
                    strToKeypadConvert(registrationData);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
            }

            //Место расчетов
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            registrationData = searchForKeyword("calculation_place: ", keyWordArray);
            if (registrationData.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("В сценарии не указано место расчетов\n");
                compare = compareScreen(screenPicture.EMPTY_SCREEN);
                if (compare) {
                    writeLogFile("Экран места расчетов пустой, завершение регистрации невозможно\n");
                    return -5;
                }
                else
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            else {
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                String getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                        "select CALCULATION_PLACE from config.CONFIG;\"" +
                        " | sqlite3 /FisGo/configDb.db\n";
                List<String> line = CashBoxConnect(getPassCommand);
                if (line.size() != 0) {
                    System.out.println("line.get(0) = " + line.get(0));
                    if (registrationData.equals(line.get(0)))
                        pressKeyBot(keyEnum.keyEnter, 0, 1);
                    else {
                        //очистка ранее введенных данных
                        clearDisplay(line.get(0).length());
                        //Ввод данных из сценария
                        strToKeypadConvert(registrationData);
                        pressKeyBot(keyEnum.keyEnter, 0, 1);
                    }
                }
                else {
                    //Ввод данных из сценария
                    strToKeypadConvert(registrationData);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
            }

            //РН ККТ
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            registrationData = searchForKeyword("reg_num: ", keyWordArray);
            if (registrationData.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("В сценарии не указан РН ККТ\n");
                compare = compareScreen(screenPicture.EMPTY_SCREEN);
                if (compare) {
                    writeLogFile("Экран ввода РН ККТ пустой, завершение регистрации невозможно\n");
                    return -6;
                }
                else
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            else {
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                String getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                        "select KKT_REG_NUM from config.CONFIG;\"" +
                        " | sqlite3 /FisGo/configDb.db\n";
                List<String> line = CashBoxConnect(getPassCommand);
                if (line.size() != 0) {
                    if (registrationData.equals(line.get(0)))
                        pressKeyBot(keyEnum.keyEnter, 0, 1);
                    else {
                        //очистка ранее введенных данных
                        clearDisplay(line.get(0).length());
                    }
                }
                //Ввод данных из сценария
                strToKeypadConvert(registrationData);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            compare = compareScreen(screenPicture.WRONG_REG_NUMBER);
            if (compare)
                return -7;

            //Версия ФФД
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            registrationData = searchForKeyword("ffd_ver: ", keyWordArray);
            if (registrationData.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("В сценарии не указана версия ФФД, выставляем версию 1.05\n");
                pressKeyBot(keyEnum.key1, 0, 1);
            }
            else {
                if (registrationData.equals("1.05"))
                    pressKeyBot(keyEnum.key1, 0, 1);
                if (registrationData.equals("1.1"))
                    pressKeyBot(keyEnum.key2, 0, 1);
            }
            pressKeyBot(keyEnum.keyEnter, 0, 1);

            //СНО
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            changeTaxSystems("tax_systems: ", keyWordArray);
            pressKeyBot(keyEnum.keyEnter, 0, 1);

            //Признаки
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            changeSignsKkt("signs: ", true, keyWordArray);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            tcpSocket.setFlagPause(true, 25);
            sleepMiliSecond(25000);
            return 0;
        }
        else {
            writeLogFile("Пункт регистрации не доступен");
            return -1;
        }
    }
    //задержка, милисекунды
    private void sleepMiliSecond(int delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //смена режима кассы
    private void changeKktMode(String keyWord, List <String> keyWordList) {
       // System.out.println("changeKktMode");
        //автономный режим
        String kktMode = searchForKeyword(keyWord, keyWordList);
       // System.out.println("kktMode = " + kktMode);
        if ( kktMode.equals("Автономный"))
            pressKeyBot(keyEnum.key1, 0, 1);
        else {
            pressKeyBot(keyEnum.key2, 0, 1);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            String OFDName = searchForKeyword( "ofd_name: ", keyWordList);
            if (OFDName.equals("CANNOT FIND KEYWORD"))
                writeLogFile("The input file does not contain the name of the OFD!\n");

            changeOFDName(OFDName, keyWordList);
        }
    }
    //смена ОФД
    private void changeOFDName (String OFDName, List <String> keyWordList) {
        //System.out.println("OFDName = " + OFDName);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        if (OFDName.equals("Яндекс.ОФД")) {
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
            pressKeyBot(keyEnum.keyEnter, 0, 2);
            pressKeyBot(keyEnum.key2, 0, 1);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
        }
        if (OFDName.equals("Первый ОФД")) {
            pressKeyBot(keyEnum.keyDown, 0, 1);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("ОФД-Я")) {
            pressKeyBot(keyEnum.keyDown, 0, 2);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("Такском")) {
            pressKeyBot(keyEnum.keyDown, 0, 3);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("СБИС ОФД")) {
            pressKeyBot(keyEnum.keyDown, 0, 4);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("КАЛУГА АСТРАЛ")) {
            pressKeyBot(keyEnum.keyDown, 0, 5);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("Корус ОФД")) {
            pressKeyBot(keyEnum.keyDown, 0, 6);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("Эвотор")) {
            pressKeyBot(keyEnum.keyDown, 0, 7);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("Электронный экспресс")) {
            pressKeyBot(keyEnum.keyDown, 0, 8);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("OFD.RU")) {
            pressKeyBot(keyEnum.keyDown, 0, 9);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("СКБ Контур")) {
            pressKeyBot(keyEnum.keyDown, 0, 10);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            autoEnterDataOFD();
        }
        if (OFDName.equals("Другой")) {
            pressKeyBot(keyEnum.keyUp, 0, 1);
            pressKeyBot(keyEnum.keyEnter, 0, 1);

            String regOFDNameOther = searchForKeyword("reg_OFD_name_other: ", keyWordList);
            if (regOFDNameOther.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("The input file does not contain the name of the fiscal data operator for the selected item \"other\"!\n");
                //     exit(0);
            }
            strToKeypadConvert(searchForKeyword("ofd_other_name: ", keyWordList));
            pressKeyBot(keyEnum.keyEnter, 0, 2);

            //ввод данных, если выбран "Другой" ОФД
            manualEnterDataOFD(keyWordList);
        }
    }
    //автоввод данных
    private void autoEnterDataOFD() {
        pressKeyBot(keyEnum.keyEnter, 0, 7);
    }
    //ручной ввод данных ОФД
    private void manualEnterDataOFD(List <String> keyWordList) {
        //------------------------------------------------ИНН ОФД-------------------------------------------------------
        String innOFD = searchForKeyword( "ofd_inn: ", keyWordList);
        if ( innOFD.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("The input file does not contain INN of the fiscal data operator for the selected item.\n");
            return;
        }
        strToKeypadConvert(innOFD);
        pressKeyBot(keyEnum.keyEnter, 0, 2);

        //------------------------------------------------Адрес сервера ОФД-------------------------------------------------------
        String addressOFD = searchForKeyword("ofd_server_address: ", keyWordList);
        if ( addressOFD.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("The input file does not contain server address of the fiscal data operator for the selected item.\n");
            return;
        }
        strToKeypadConvert( addressOFD );
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        //добавить проверку экрана: если "адрес ОФД не найден, то добавить дополнительное нажатие на кнопку ввода

        pressKeyBot(keyEnum.keyEnter, 0, 2);

        //------------------------------------------------Порт ОФД-------------------------------------------------------
        String portOFD = searchForKeyword( "ofd_server_port: ",keyWordList );
        if ( portOFD.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("The input file does not contain port of the fiscal data operator for the selected item.\n");
            return;
        }
        strToKeypadConvert( portOFD  );
        pressKeyBot(keyEnum.keyEnter, 0, 2);

        //---------------------------------------------Адрес проверки чека------------------------------------------------
        String checkReceiptOFD = searchForKeyword( "ofd_check_reciept_address: ", keyWordList);
        if ( checkReceiptOFD.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("The input file does not contain port of the fiscal data operator for the selected item.\n");
            return;
        }
        strToKeypadConvert(checkReceiptOFD );
        pressKeyBot(keyEnum.keyEnter, 0, 2);
    }
    //отчистка диспея
    private void clearDisplay(int length) {
        for (int i = 0; i < length; i++)
            pressKeyBot(keyEnum.keyReversal, 0,1 );
    }
    //Выбор СНО
    private void changeTaxSystems(String keyWord, List <String> keyWordList) {
        String taxSystems = searchForKeyword(keyWord, keyWordList);
        //System.out.println("taxSystems = " + taxSystems);
        if ( taxSystems.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("В файле сценария не выбраны СНО.\n Невозможно завершить регистрацию");
            return;
        }

        int taxMaskLength = 6;
        String getTaxCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                "select TAX_SYSTEMS from config.CONFIG;\" " + "| sqlite3 /FisGo/configDb.db\n";
        char[] changeTaxMask = getMaskFromConfigDbCashbox(taxMaskLength, getTaxCommand);

        //оптимизировать ??
        if (changeTaxMask[taxMaskLength - 1] == '1')
            pressKeyBot(keyEnum.key1, 0, 1);
        if (changeTaxMask[taxMaskLength - 2] == '1')
            pressKeyBot(keyEnum.key2, 0, 1);
        if (changeTaxMask[taxMaskLength - 3] == '1')
            pressKeyBot(keyEnum.key3, 0, 1);
        if (changeTaxMask[taxMaskLength - 4] == '1')
            pressKeyBot(keyEnum.key4, 0, 1);
        if (changeTaxMask[taxMaskLength - 5] == '1')
            pressKeyBot(keyEnum.key5, 0, 1);
        if (changeTaxMask[taxMaskLength - 6] == '1')
            pressKeyBot(keyEnum.key6, 0, 1);

        Vector <String> taxSystemsTable = multiplieChoice (taxSystems);

        for (int i = 0; i < taxSystemsTable.size(); i++){
            if (taxSystemsTable.get(i).equals("ОСН"))
                pressKeyBot(keyEnum.key1, 0, 1);
            if (taxSystemsTable.get(i).equals("УСН доход"))
                pressKeyBot(keyEnum.key2, 0, 1);
            if (taxSystemsTable.get(i).equals("УСН дох./расх."))
                pressKeyBot(keyEnum.key3, 0, 1);
            if (taxSystemsTable.get(i).equals("ЕНВД"))
                pressKeyBot(keyEnum.key4, 0, 1);
            if (taxSystemsTable.get(i).equals("ЕСХН"))
                pressKeyBot(keyEnum.key5, 0, 1);
            if (taxSystemsTable.get(i).equals("Патент"))
                pressKeyBot(keyEnum.key6, 0, 1);
        }
    }
    //Получение масок с кассы
    private char[] getMaskFromConfigDbCashbox(int maskLength, String command) {
        char[] maskBin = new char[maskLength];
        for (int i = 0; i < maskLength; i++)
            maskBin[i] = '0';

        List <String> line = CashBoxConnect(command);
        String binMaskString = Integer.toBinaryString(Integer.parseInt(line.get(0)));

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
    private void changeSignsMode (Vector<String> signsTable, String modeKkt, String version, List<String> keyWordList) {
        boolean agentChange = false;
        boolean autoModeChange = false;

        for (int i = 0; i < signsTable.size(); i++) {
            if (signsTable.get(i).equals("Шифрования")) {
                if (modeKkt.equals("Автономный"))
                    writeLogFile("В автономном режиме признак шифрования скрыт.\n");
                if (modeKkt.equals("Передачи данных"))
                    pressKeyBot(keyEnum.key1, 0, 1);
            }
            if (signsTable.get(i).equals("Подакциз.товар")) {
                if (modeKkt.equals("Автономный"))
                    pressKeyBot(keyEnum.key1, 0, 1);
                if (modeKkt.equals("Передачи данных"))
                    pressKeyBot(keyEnum.key2, 0, 1);
            }
            if (signsTable.get(i).equals("Расч. за услуги")) {
                if (modeKkt.equals("Автономный"))
                    pressKeyBot(keyEnum.key2, 0, 1);
                if (modeKkt.equals("Передачи данных"))
                    pressKeyBot(keyEnum.key3, 0, 1);
            }
            if (signsTable.get(i).equals("Азартн.игры")) {
                if (modeKkt.equals("Автономный"))
                    pressKeyBot(keyEnum.key3, 0, 1);
                if (modeKkt.equals("Передачи данных"))
                    pressKeyBot(keyEnum.key4, 0, 1);
            }
            if (signsTable.get(i).equals("Лотерея")) {
                if (modeKkt.equals("Автономный"))
                    pressKeyBot(keyEnum.key4, 0, 1);
                if (modeKkt.equals("Передачи данных"))
                    pressKeyBot(keyEnum.key5, 0, 1);
            }
            if (signsTable.get(i).equals("Пл. агент")) {
                agentChange = true;
                if (modeKkt.equals("Автономный"))
                    pressKeyBot(keyEnum.key5, 0, 1);
                if (modeKkt.equals("Передачи данных"))
                    pressKeyBot(keyEnum.key6, 0, 1);
            }
            if (version.equals("1.1")) {
                if (signsTable.get(i).equals("Автомат. режим")) {
                    autoModeChange = true;
                    if (modeKkt.equals("Автономный"))
                        pressKeyBot(keyEnum.key6, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKeyBot(keyEnum.key7, 0, 1);
                }
                if (signsTable.get(i).equals("БСО")) {
                    if (modeKkt.equals("Автономный"))
                        pressKeyBot(keyEnum.key7, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKeyBot(keyEnum.key8, 0, 1);
                }
                if (signsTable.get(i).equals("Прод интернет")) {
                    if (modeKkt.equals("Автономный"))
                        pressKeyBot(keyEnum.key8, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKeyBot(keyEnum.key9, 0, 1);
                }
                if (signsTable.get(i).equals("Уст. принт. в автомате")) {
                    if (modeKkt.equals("Автономный"))
                        pressKeyBot(keyEnum.key9, 0, 1);
                    if (modeKkt.equals("Передачи данных"))
                        pressKeyBot(keyEnum.key0, 0, 1);
                }
            }

            if (agentChange) {
                pressKeyBot(keyEnum.keyEnter, 0, 1);
                //Типы агентов
                String agentType;
                agentType = searchForKeyword("agent_type: ", keyWordList);
                if (agentType.equals("CANNOT FIND KEYWORD")) {
                    writeLogFile("Выбран признак агента, но не выбраны типы агентов, нажимаем продолжить без указания типов...\n");
                    pressKeyBot(keyEnum.keyEnter, 0, 2);
                }

                Vector<String> agentTypeTable = multiplieChoice(agentType);
                for (int k = 0; k < agentTypeTable.size(); k++) {
                    if (agentTypeTable.get(k).equals("Банковский платежный агент"))
                        pressKeyBot(keyEnum.key1, 0, 1);
                    if (agentTypeTable.get(k).equals("Банковский платежный субагент"))
                        pressKeyBot(keyEnum.key2, 0, 1);
                    if (agentTypeTable.get(k).equals("Платежный агент"))
                        pressKeyBot(keyEnum.key3, 0, 1);
                    if (agentTypeTable.get(k).equals("Платежный субагент"))
                        pressKeyBot(keyEnum.key4, 0, 1);
                    if (agentTypeTable.get(k).equals("Поверенный"))
                        pressKeyBot(keyEnum.key5, 0, 1);
                    if (agentTypeTable.get(k).equals("Комиссионер"))
                        pressKeyBot(keyEnum.key6, 0, 1);
                    if (agentTypeTable.get(k).equals("Агент"))
                        pressKeyBot(keyEnum.key7, 0, 1);
                }
            }
            if (version.equals("1.1")) {
                if (autoModeChange) {
                    pressKeyBot(keyEnum.keyEnter, 0, 2);
                    strToKeypadConvert(searchForKeyword("automat_number: ", keyWordList));
                }
            }
        }
    }
    //Выбор признаков регистрации
    private  void changeSignsKkt(String keyWord, boolean registrationFlag, List <String> keyWordList) {
        String kktSigns = searchForKeyword(keyWord, keyWordList);
        if (kktSigns.equals("CANNOT FIND KEYWORD"))
            writeLogFile("Признаки ККТ не выбраны.\n");
        else {
            Vector<String> signsTable = multiplieChoice(kktSigns);
            //Если выставлен флаг регистрации, то экраны смотрим в соответствии с файлом сценария
            if (registrationFlag) {
                String registrationVer = searchForKeyword("ffd_ver: ", keyWordList);
                String registrationMode = searchForKeyword("mode_kkt: ", keyWordList);
                changeSignsMode(signsTable, registrationMode, registrationVer, keyWordList);
            } else {

                //получаем версию ФФД и Режим работы ККТ
                String command = "echo \"attach '/FisGo/configDb.db' as config; " +
                        "select FFD_KKT_VER from config.CONFIG; " +
                        "select KKT_MODE from config.CONFIG;\" " +
                        "| sqlite3 /FisGo/configDb.db\n";
                List<String> line = CashBoxConnect(command);
                //for (int i = 0; i < line.size(); i++)
                //  System.out.println("line[i] = " + line.get(i));

                String registrationVer = line.get(0);

                int ENCRYPTION_SIGN = 0, EXCISABLE_SIGN = 0, CLC_SERVICE_SIGN  = 0, GAMBLING_SIGN = 0, LOTTERY_SIGN = 0, PAYING_AGENT_SIGN = 0;

                if (registrationVer.equals("2")) {
                    registrationVer = "1.05";
                    //убираем все ранее выбранные признаки
                    command = "echo \"attach '/FisGo/configDb.db' as config; " +
                            "select ENCRYPTION_SIGN from config.CONFIG; " +
                            "select EXCISABLE_SIGN from config.CONFIG; " +
                            "select CLC_SERVICE_SIGN from config.CONFIG; " +
                            "select GAMBLING_SIGN from config.CONFIG; " +
                            "select LOTTERY_SIGN from config.CONFIG; " +
                            "select PAYING_AGENT_SIGN from config.CONFIG; \" " +
                            "| sqlite3  /FisGo/configDb.db\n";

                    List<String> lineSigns = CashBoxConnect(command);

                    //TODO: Проверка граничных значений
                    ENCRYPTION_SIGN = Integer.parseInt(lineSigns.get(0));
                    EXCISABLE_SIGN = Integer.parseInt(lineSigns.get(1));
                    CLC_SERVICE_SIGN  = Integer.parseInt(lineSigns.get(2));
                    GAMBLING_SIGN = Integer.parseInt(lineSigns.get(3));
                    LOTTERY_SIGN = Integer.parseInt(lineSigns.get(4));
                    PAYING_AGENT_SIGN = Integer.parseInt(lineSigns.get(5));
                }
                if (registrationVer.equals("3"))
                    registrationVer = "1.1";

                String registrationMode = line.get(1);
                if (registrationMode.equals("0"))
                    registrationMode = "Передачи данных";
                if (registrationMode.equals("1"))
                    registrationMode = "Автономный";

                if (registrationMode.equals("Передачи данных")) {
                    if (ENCRYPTION_SIGN == 1)
                        pressKeyBot(keyEnum.key1, 0, 1);
                    if (EXCISABLE_SIGN == 1)
                        pressKeyBot(keyEnum.key2, 0, 1);
                    if (CLC_SERVICE_SIGN == 1)
                        pressKeyBot(keyEnum.key3, 0, 1);
                    if (GAMBLING_SIGN == 1)
                        pressKeyBot(keyEnum.key4, 0, 1);
                    if (LOTTERY_SIGN == 1)
                        pressKeyBot(keyEnum.key5, 0, 1);
                    if (PAYING_AGENT_SIGN == 1)
                        pressKeyBot(keyEnum.key6, 0, 1);
                }
                if (registrationMode.equals("Автономный")) {
                    if (EXCISABLE_SIGN == 1)
                        pressKeyBot(keyEnum.key1, 0, 1);
                    if (CLC_SERVICE_SIGN == 1)
                        pressKeyBot(keyEnum.key2, 0, 1);
                    if (GAMBLING_SIGN == 1)
                        pressKeyBot(keyEnum.key3, 0, 1);
                    if (LOTTERY_SIGN == 1)
                        pressKeyBot(keyEnum.key4, 0, 1);
                    if (PAYING_AGENT_SIGN == 1)
                        pressKeyBot(keyEnum.key5, 0, 1);
                }
                changeSignsMode(signsTable, registrationMode, registrationVer, keyWordList);
            }
        }
    }
    //множественный выбор
    private Vector <String> multiplieChoice (String str) {
        Vector <String> line = new Vector<>();
        String[] parts = (str + " ").split(";");

        for (String tmpStr: parts)
            line.add(tmpStr);

        return line;
    }
    //Тех. обнуление
    private void techNull() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key4, 0, 1);
        pressKeyBot(keyEnum.key7, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 2);
        tcpSocket.setFlagPause(true, 17);
    }
    //Смена данных юрлица
    private int re_registrationLegalEntity(List <String> keyWordArray) {
        System.out.println("in re_registrationLegalEntity");
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);

        boolean compare = compareScreen(screenPicture.RE_REGISTRATION_MENU);
        System.out.println("compare = " + compare);
        if (compare) {
            //делаем выборку их БД users на кассе, получаем пароль одного из них
            String getPassCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                    "select ORGANIZATION_NAME from config.CONFIG;" +
                    "select CALCULATION_ADDRESS from config.CONFIG;" +
                    "select CALCULATION_PLACE from config.CONFIG;\"" +
                    " | sqlite3 /FisGo/configDb.db\n";
            List <String> line = CashBoxConnect(getPassCommand);

            writeLogFile("Пункты меню Перерегистрация доступны.");
            pressKeyBot(keyEnum.key2, 0, 1);

            pressKeyBot(keyEnum.keyEnter, 0, 1);
            String registrationData = searchForKeyword("organization_name: ", keyWordArray);
            if (registrationData.equals("CANNOT FIND KEYWORD"))
                writeLogFile("Смена данных юр. лица. В сценарии не указано наименование организации\n");
            else {
                //очистка ранее введенных данных
                clearDisplay(line.get(0).length());
                //Ввод данных из сценария
                strToKeypadConvert(registrationData);
            }
            pressKeyBot(keyEnum.keyEnter, 0, 1);

            pressKeyBot(keyEnum.keyEnter, 0, 1);
            registrationData = searchForKeyword("calculation_address: ", keyWordArray);
            if (registrationData.equals("CANNOT FIND KEYWORD"))
                writeLogFile("Смена данных юр. лица. В сценарии не указан адрес расчетов\n");
            else {
                //отчистка ранее введенных данных
                clearDisplay(line.get(1).length());
                //Ввод данных из сценария
                strToKeypadConvert(registrationData);
            }
            pressKeyBot(keyEnum.keyEnter, 0, 1);

            pressKeyBot(keyEnum.keyEnter, 0, 1);
            registrationData = searchForKeyword("calculation_place: ", keyWordArray);
            if (registrationData.equals("CANNOT FIND KEYWORD"))
                writeLogFile("Смена данных юр. лица. В сценарии не указано место расчетов\n");
            else {
                //отчистка ранее введенных данных
                clearDisplay(line.get(2).length());
                //Ввод данных из сценария
                strToKeypadConvert(registrationData);
            }
            pressKeyBot(keyEnum.keyEnter, 0, 1);

            pressKeyBot(keyEnum.keyEnter, 0, 1);
            tcpSocket.setFlagPause(true, 10);
            sleepMiliSecond(10000);
        }
        else {
            writeLogFile("Пункт меню перерегистраций не доступен");
            return -1;
        }
        return 0;
    }
    //Открытие смены
    private int openShift() {
        System.out.println("open shift");
        //делаем выборку их конфига на кассе, проверем, открыта смена или нет
        String getOpenShiftCommand = "echo \"attach '/FisGo/configDb.db' as users; " +
                "select OPEN_SHIFT_DATE from config.CONFIG;\" | sqlite3 /FisGo/configDb.db\n";
        List <String> line = CashBoxConnect(getOpenShiftCommand);
        if (line.isEmpty()) {
            pressKeyBot(keyEnum.keyMenu, 0, 1);
            pressKeyBot(keyEnum.key1, 0, 1);
            boolean compare = compareScreen(screenPicture.OPEN_SHIFT_MENU);
            if (compare) {
                pressKeyBot(keyEnum.keyEnter, 0, 2);
                tcpSocket.setFlagPause(true, 10);
            } else {
                writeLogFile("Пункт открытия смены не доступен");
                return -2;
            }
            return 0;
        }
        else
            return -1;
    }
    //Продажа, приход
    private int checkPrintSaleComming(List <String> keyWordArray) {
        System.out.println("in checkPrintSaleComming");
        sleepMiliSecond(1000);

        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.keyCancel, 0, 1);
        sleepMiliSecond(1000);
        boolean compare = compareScreen(screenPicture.FREE_SALE_MODE);
        System.out.println("compare = " + compare);
        if (compare) {
            String countCheckStr = searchForKeyword("check_count: ", keyWordArray);
            if (countCheckStr.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("В сценарии не указано количество чеков, считаем, что необходимо напечатать один чек...");
                countCheckStr = "1";
            }
            String tmpGoodsStr = searchForKeyword("Good ", keyWordArray);
            if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("Не найдены товары, которые необходимо добавить в чек");
                return -1;
            } else {
                int countChecks = Integer.parseInt(countCheckStr);
                for (int i = 0; i < countChecks; i++) {
                    checkPrint(keyWordArray);
                }
            }
        } else {
            writeLogFile("Не открыт экран продажи (режим свободной цены)");
        }
        return 0;
    }
    private void checkPrint(List <String> keyWordArray) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!in checkPrint");
        String tmpGoodsStr;
       // System.out.println("countGoods = " + sale.getCountGoods());
        for (int j = 0; j < 3 /*sale.getCountGoods()*/; j++) {
            //bool compare = compareScreen(SCREEN);
            //if (compare)
            //{
            tmpGoodsStr = searchForKeyword("good_from_" + (j + 1) + ": ", keyWordArray);
            System.out.println("good_from_ = " + tmpGoodsStr);
            if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("Goods not found in input file");
                return;
            }
            if (tmpGoodsStr.equals("good_from_base")) {
                pressKeyBot(keyEnum.keyGoods, 0, 1);
                tmpGoodsStr = searchForKeyword("good_code_" + (j + 1) + ": ", keyWordArray);
                System.out.println();
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
                tmpGoodsStr = searchForKeyword("good_base_free_price_" + (j + 1) + ": ", keyWordArray);
                //       System.out.println("(free price base) tmpGoodsStr = " + tmpGoodsStr);
                if (!tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                    System.out.println("tmpGoodsStr = " + tmpGoodsStr);
                    strToKeypadConvert(tmpGoodsStr);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
                tmpGoodsStr = searchForKeyword("good_type_" + (j + 1) + ": ", keyWordArray);
                if (tmpGoodsStr.equals("weighted")) {
                    tmpGoodsStr = searchForKeyword("good_weight_" + (j + 1) + ": ", keyWordArray);
                    if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                        writeLogFile("Goods type not found in input file");
                        return;
                    }
                    strToKeypadConvert(tmpGoodsStr);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
            }
            if (tmpGoodsStr.equals("good_free_price")) {
                System.out.println("tmpGoodsStr.equals(\"good_free_price\")");
                tmpGoodsStr = searchForKeyword("good_price_" + (j + 1) + ": ", keyWordArray);
                System.out.println("tmpGoodsStr = " + tmpGoodsStr);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            tmpGoodsStr = searchForKeyword("good_type_" + (j + 1) + ": ", keyWordArray);
            if (tmpGoodsStr.equals("countable")) {
                tmpGoodsStr = searchForKeyword("good_count_" + (j + 1) + ": ", keyWordArray);
                if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                    writeLogFile("Goods count not found in input file");
                    return;
                }
                if (Integer.parseInt(tmpGoodsStr) > 1) {
                    pressKeyBot(keyEnum.keyQuantity, 0, 1);
                    strToKeypadConvert(tmpGoodsStr);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
            }

            tmpGoodsStr = searchForKeyword("spacial_form_" + (j + 1) + ": ", keyWordArray);
            if (!tmpGoodsStr.equals("CANNOT FIND KEYWORD"))
                holdKey(keyEnum.keyMenu, 0, 1);
            if (tmpGoodsStr.equals("ПР"))
                pressKeyBot(keyEnum.key1, 0, 1);
            if (tmpGoodsStr.equals("ЧП")) {
                pressKeyBot(keyEnum.key2, 0, 1);
                tmpGoodsStr = searchForKeyword("special_form_prepayment_sum_" + (j + 1) + ": ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            if (tmpGoodsStr.equals("А"))
                pressKeyBot(keyEnum.key3, 0, 1);
            if (tmpGoodsStr.equals("П")){
                pressKeyBot(keyEnum.key4, 0, 1);
                tmpGoodsStr = searchForKeyword("special_form_offset_of_prepayment_sum_" + (j + 1) + ": ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            if (tmpGoodsStr.equals("КР"))
                pressKeyBot(keyEnum.key5, 0, 1);
            if (tmpGoodsStr.equals("ЧК")) {
                pressKeyBot(keyEnum.key6, 0, 1);
                tmpGoodsStr = searchForKeyword("special_form_credit_sum_" + (j + 1) + ": ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            if (tmpGoodsStr.equals("К")) {
                pressKeyBot(keyEnum.key7, 0, 1);
                tmpGoodsStr = searchForKeyword("special_form_credit_pay_sum_" + (j + 1) + ": ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            pressKeyBot(keyEnum.keyEnter, 0, 1);
        }

        //Тип оплаты чека
        tmpGoodsStr = searchForKeyword("type_pay: ", keyWordArray);
        if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Pay type not found in input file");
            return;
        } else {
            if (tmpGoodsStr.equals("cash_pay")) {
                pressKeyBot(keyEnum.keyPayByCash, 0, 1);
                tmpGoodsStr = searchForKeyword("sum_pay: ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
            }
            if (tmpGoodsStr.equals("card_pay")) {
                pressKeyBot(keyEnum.keyPayByCard, 0, 1);
            }

            pressKeyBot(keyEnum.keyEnter, 0, 1);
        }
        tcpSocket.setFlagPause(true, 20);
    }


    //Добавление товаров на кассу
    //FIXME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!разобраться с русскими символами!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void addGoodsOnCash() {
        //вставляем товары из файла
        StringBuilder addGoodCmd = new StringBuilder();
        addGoodCmd.append("echo \"attach '/FisGo/goodsDb.db' as goods; ");
        List <String> insertsGoods = readDataScript("src\\test\\resourses\\insert_to_goodsDb.txt");
        for (String tmpStr: insertsGoods)
            addGoodCmd.append(tmpStr);
        addGoodCmd.append("\" | sqlite3 /FisGo/goodsDb.db\n");
        CashBoxConnect(addGoodCmd.toString());

        //получаем список ID и артикулов из базы goods для всавки hash в goods_code
        StringBuilder selectGoodIDCmd = new StringBuilder();
        selectGoodIDCmd.append("echo \"attach '/FisGo/goodsDb.db' as goods; ");
        for (int i = 0; i<100; i++) {
            selectGoodIDCmd.append("select ID, ARTICUL from goods.GOODS where ARTICUL = '" + (i + 1) + "';");
        }
        selectGoodIDCmd.append("\" | sqlite3 /FisGo/goodsDb.db\n");
        List<String> line = CashBoxConnect(selectGoodIDCmd.toString());

        //заполнение таблицы goods_code
        List<String> hashArticle = readDataScript("src\\test\\resourses\\hashTable.txt");
        for (String strLine: line) {
            String idLine, articleLine;
            String[] parts = (strLine).split("\\|");
            idLine = parts[0];
            articleLine = parts[1];

            StringBuilder insertGoodCodeCmd = new StringBuilder();
            insertGoodCodeCmd.append("echo \"attach '/FisGo/goodsDb.db' as goods; ");
            insertGoodCodeCmd.append("insert into goods.GOODS_CODE (GOODS_ID, HASH_VAL, TYPE) values (");
            insertGoodCodeCmd.append(idLine);
            insertGoodCodeCmd.append(", ");
            insertGoodCodeCmd.append(hashArticle.get(Integer.parseInt(articleLine) - 1));
            insertGoodCodeCmd.append(", 2);");
            insertGoodCodeCmd.append("\" | sqlite3 /FisGo/goodsDb.db\n");

            CashBoxConnect(insertGoodCodeCmd.toString());
        }
    }

   // @Test
 /*   public void testCash () {
        System.out.println("Type testing CashBox: " + CashBoxType);

        if (CashBoxType.equals("DreamkasF")) config.setCashType(0);
        if (CashBoxType.equals("DreamkasRF")) config.setCashType(1);

        keyEnum.initKeyEnum();
        tcpSocket.setFlagGetScreen(true);
      //  controller.setUpdateImgFlag(true);
        tcpSocket.createSocket(CashboxIP, Integer.parseInt(CashboxPort));

        try {
            System.out.println("run instr tread" );
            tcpSocket.setReadAllInstruction(false);
            //ищем инструкции
            searchInstruction();
            String instruction;

            // Инструкция -> instruction
            for (short i = 0; i < instruction_table.size(); i++) {
                instruction = instruction_table.get(i);
                System.out.println("instruction = " + instruction);
                //чеки - продажа, приход
                if (instruction.equals("Check_print_sale_comming!"))
                    checkPrintSaleComming();


                //закрытие смены
                if (instruction.equals("Close_shift!"))
                    closeShift();
                //печать X-отчета
                if (instruction.equals("X_count!"))
                    xCount();
                //изъятие
                if (instruction.equals("Reserve!"))
                    reserve();
                //внесение
                if (instruction.equals("Insertion!"))
                    insertion();
                //отчет о тек. состоянии
                if (instruction.equals("Current_status_report_print!"))
                    currentStatusReportPrint();
                //чек коррекции
                if (instruction.equals("Correction_receipt!"))
                    correctionReceipt();
                //все документы из ФН
                if (instruction.equals("Print_all_documents_FN!"))
                    printAllDocumentsFN();
                // запись документов
                if (instruction.equals("Record_documents_FN!"))
                    recordAllDocumentsFN();
                //1 документ из ФН
                if (instruction.equals("Documents_FN!"))
                    printDocumentNumberFN();
                //все итоги регистрации
                if (instruction.equals("Print_all_registration_result!"))
                    printAllRegistrationResult();
                //1 итог регистрации
                if (instruction.equals("Registration_result!"))
                    printNumberRegistrationResult();

                if (instruction.equals("Check_print_sale_consumption!"))
                    checkPrintSaleConsumption();
                if (instruction.equals("Check_print_refund_comming!"))
                    checkPrintRefundComming();
                if (instruction.equals("Check_print_refund_consumption!"))
                    checkPrintRefundConsumption();
                //регистрация
                if (instruction.equals("Registration!"))
                    registration();
                //перерегистрация - смена данных юрлица
                if (instruction.equals("Re_Registration_legal_entity!"))
                    re_registrationLegalEntity();
                //перерегистрация - смена ОФД
                if (instruction.equals("Re_Registration_change_ofd!"))
                    re_registrationChangeOFD();
                //перерегистрация - смена режима ККТ
                if (instruction.equals("Re_Registration_mode_kkt!"))
                    re_registrationModeKKT();
                //перерегистрация - замена ФН
                if (instruction.equals("Change_fn!"))
                    re_registrationChangeFN();
                //ФН поврежден
                if (instruction.equals("Damage_fn!" ))
                    damagedFN();
                //снятие с учета ккт
                if (instruction.equals("Un_register_kkt!" ))
                    deregistrationKKT();
                //перерегистрация - настройка ККТ
                if (instruction.equals("Re_Registration_settings_kkt!"))
                    re_registrationSettingsKKT();
                //тех. отчет
                if (instruction.equals("Print_tech_report!"))
                    techReport();
            }
            tcpSocket.setReadAllInstruction(true);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

     //   ToDoIntructionsThread toDoIntructionsThread = new ToDoIntructionsThread();
       // toDoIntructionsThread.t.start();
   // }


    private void testKeypadAllScreen() {
        tcpSocket.setFlagKeypadMode(true);
        tcpSocket.serverGetKepadMode();
        ///  System.out.println("---------in testKeypadAllScreen-----------");
        keypad_mode = tcpSocket.getKeypadMode();
        //  System.out.println("keypad_mode = " + keypad_mode);
    }

    private void re_registrationSettingsKKT() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        pressKeyBot(keyEnum.key7, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        //сделать проверку на уже выбранные сно и признаки
       // changeTaxSystems("tax_systems: ");
        //      changeSignsKkt("signs: ");
        // pressKeyBot(keyEnum.keyEnter, 0, 2);

        //      pressKeyBot(keyEnum.keyEnter, 0, 2);
    }

    private void deregistrationKKT() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        pressKeyBot(keyEnum.key6, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 2);
    }

    private void damagedFN() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 2);
    }

    private void re_registrationChangeFN() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        pressKeyBot(keyEnum.key4, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 2);
    }

    private void re_registrationModeKKT() {
        System.out.println("re_registrationModeKKT");
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        //bool compare = compareScreen(REGISTRATION);
        //if (compare)
        {
            pressKeyBot(keyEnum.key1, 0, 1);
            pressKeyBot(keyEnum.key8, 0, 1);
       //     changeKktMode ("mode_kkt: ");

            pressKeyBot(keyEnum.keyEnter, 0, 1);
            pressKeyBot(keyEnum.keyEnter, 0, 2);
        }
    }

    private void techReport() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        pressKeyBot(keyEnum.key6, 0, 1);
        tcpSocket.setFlagPause(true, 7);

    }
/*




/*
    private void re_registrationChangeOFD() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);

        //getScreen();
        //bool compare = compareScreen(REGISTRATION);
        //if (compare)
        {
            pressKeyBot(keyEnum.key1, 0, 1);
            pressKeyBot(keyEnum.key3, 0, 1);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
            changeOFDName (searchForKeyword( "ofd_name: ")); //re_reg_change_ofd
            pressKeyBot(keyEnum.keyEnter,0,  1);
            tcpSocket.setFlagPause(true, 10);
        }
    }









    private void printNumberRegistrationResult() throws IOException {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key2, 0, 1);
        pressKeyBot(keyEnum.key3, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        String numDocument = searchForKeyword("number_print_registration_result: ");
        if (numDocument.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ reserve in input file");
            return;
        }
        strToKeypadConvert(numDocument);
        pressKeyBot(keyEnum.keyEnter, 0,1);
    }

    private void printAllRegistrationResult() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key2, 0, 1);
        pressKeyBot(keyEnum.key3, 0, 1);
        pressKeyBot(keyEnum.key2, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
    }

    private void printDocumentNumberFN() throws IOException {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key2, 0, 2);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        String numDocument = searchForKeyword("number_print_FD: ");
        if (numDocument.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ reserve in input file");
            return;
        }
        strToKeypadConvert(numDocument);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
    }

    private void printAllDocumentsFN() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key2, 0, 3);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
    }

    private void recordAllDocumentsFN() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key2, 0, 2);
        pressKeyBot(keyEnum.key3, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
    }

    private void correctionReceipt() throws IOException {
        System.out.println("corrRec");
        //добавить проверку, что открыт экран
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key2, 0, 1);
        pressKeyBot(keyEnum.key4, 0, 1);
        String correction = searchForKeyword("correction_receipt_type: ");
        System.out.println("correction = " + correction);
        if (correction.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ reserve in input file");
            return;
        }
        if (correction.equals("Кор. прихода"))
            pressKeyBot(keyEnum.key1, 0, 1);
        if (correction.equals("Кор. расхода"))
            pressKeyBot(keyEnum.key2, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 1);

        //FIXME запросить из базы конфига, с какими СНО зарегана касса, сделать выбор в зависимости от доступных,
        correction = searchForKeyword("correction_tax: ");
        if (correction.equals("ОСН"))
            pressKeyBot(keyEnum.key1, 0, 1);
        if (correction.equals("УСН доход"))
            pressKeyBot(keyEnum.key2, 0, 1);
        if (correction.equals("УСН дох./расх."))
            pressKeyBot(keyEnum.key3, 0, 1);
        if (correction.equals("ЕНВД"))
            pressKeyBot(keyEnum.key4, 0, 1);
        if (correction.equals("ЕСХН"))
            pressKeyBot(keyEnum.key5, 0, 1);
        if (correction.equals("Патент"))
            pressKeyBot(keyEnum.key6, 0, 1);
        //FIXME:----------------------------------------------------------------------------------------------
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        //if ()
        ///////////////////////////////////////////
        correction = searchForKeyword("correction_cash: ");
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_card: ");
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_external_view: ");
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_type: ");
        if (correction.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ reserve in input file");
            return;
        }
        if (correction.equals("Самостоятельная"))
            pressKeyBot(keyEnum.key1, 0, 1);
        if (correction.equals("По предписанию"))
            pressKeyBot(keyEnum.key2, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_reason: ");
        if (correction.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ reserve in input file");
            return;
        }
        System.out.println("correction = " + correction);
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_doc_date: ");
        if (correction.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ reserve in input file");
            return;
        }
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_doc_number: ");
        if (correction.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ reserve in input file");
            return;
        }
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_tax_18: ");
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_tax_10: ");
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_tax_0: ");
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_tax_without: ");
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_tax_18/118: ");
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        correction = searchForKeyword("correction_tax_10/110: ");
        strToKeypadConvert(correction);
        pressKeyBot(keyEnum.keyEnter, 0, 2); /**/
  //  }

    /*private void reserve() throws IOException {
        pressKeyBot(keyEnum.keyMenu, 0,1);
        pressKeyBot(keyEnum.key1, 0,1);
        //добавить проверку, что смена открыта
        pressKeyBot(keyEnum.key4, 0,1);
        String sumReserve = searchForKeyword("sum_reserve: ");
        if (sumReserve.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ reserve in input file");
            return;
        }
        strToKeypadConvert(sumReserve);
        pressKeyBot(keyEnum.keyEnter, 0,2);
        tcpSocket.setFlagPause(true, 5);
    }

    private void insertion() throws IOException {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        //добавить проверку, что смена открыта
        pressKeyBot(keyEnum.key5, 0, 1);
        String sumInsertion = searchForKeyword("sum_insertion: ");
        if (sumInsertion.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Cannot find summ insertion in input file");
            return;
        }
        strToKeypadConvert(sumInsertion);
        pressKeyBot(keyEnum.keyEnter, 0, 2);
        tcpSocket.setFlagPause(true, 5);
    }


    private void closeShift() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        //добавить проверку, что на экране есть пункт закрытия смены
        pressKeyBot(keyEnum.key2, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 1);
        tcpSocket.setFlagPause(true, 7);
    }

    private void xCount() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        //добавить проверку, что смена открыта
        pressKeyBot(keyEnum.key3, 0, 1);
        tcpSocket.setFlagPause(true, 4);
    }

    private void currentStatusReportPrint() {
        pressKeyBot(keyEnum.keyMenu, 0,  1);
        pressKeyBot(keyEnum.key2, 0, 1);
        //проверить, что касса зарегистрирована
        pressKeyBot(keyEnum.key1, 0, 1);
        tcpSocket.setFlagPause(true, 5);
    }*/

    public void searchInstruction() {
        System.out.println("in searchInstruction");
        Vector<String> line;
        line = readFromFileBot();
        String tmpStr;

        for (short i = 0; i < line.size(); i++) {
            tmpStr = line.get(i);
            if (tmpStr.indexOf("!") != -1)
                instruction_table.add(tmpStr);
        }
    }












    private Vector<String> readFromFileBot() {
        Vector<String> test_suite = new Vector<>();
        try {
            FileInputStream fstream = new FileInputStream("botTestInput");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strFromFile;
            while ((strFromFile = br.readLine()) != null) {
                test_suite.add(strFromFile);
            }
            br.close();
            fstream.close();
        } catch (IOException e) {
            System.out.println("Ошибка readFromFileBot");
        }

        return test_suite;
    }

    public String searchForKeyword(String keyWord, List <String> keyWordArray) {
        // List<String> line = null;
        // line = readFromFileBot();

        String tmpStr;

        for (short i = 0; i < keyWordArray.size(); i++) {
            tmpStr = keyWordArray.get(i);
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

    public int strToKeypadConvert(String str) {
       // System.out.println("in strToKeypadConvert");
        tcpSocket.setKeypadSleep(1);

        tcpSocket.setFlagKeypadMode(true);
        tcpSocket.serverGetKepadMode();
    //    System.out.println("---------in testKeypadAllScreen-----------");
        keypad_mode = tcpSocket.getKeypadMode();
      // System.out.println("keypad_mode = " + keypad_mode);

        Short keyNumPrew = 40, keyNum = 0, pressCount;
        boolean exit;

        //System.out.println("strToKeypadConvert str = " + str);

        Keypad[] keys = new Keypad[keypad.keys_table_size];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new Keypad();
        }

        initKey(keys);

        Charset cset = Charset.forName("CP866");
        ByteBuffer buf = cset.encode(str);
        byte[] charsCp866 = buf.array();

        for (Short i = 0; i < charsCp866.length; i++) {
            int charsetNumber = (int) charsCp866[i];
            if (charsetNumber < 0) {
                charsetNumber += 256;
            }
            exit = false;

            // System.out.println("charsetNumber = " + charsetNumber);

            for (int j = 0; j < keypad.keys_table_size; j++) {
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
                            if (keyNumPrew.equals(keyNum)) tcpSocket.setFlagPause(true, 2);
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
                            if (keyNumPrew.equals(keyNum)) tcpSocket.setFlagPause(true, 2);
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
                            if (keyNumPrew.equals(keyNum)) tcpSocket.setFlagPause(true, 2);
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
                        //*
                        if (charsetNumber == 42) {
                            keyNum = 38;
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                }

                //Если русский + цифры, keypadMode == 5
                if (keypad_mode == keypadMode.CYRILLIC + keypadMode.NUMBERS) {
                    //    tcpSocket.setFlagPauseEnter(true);
                //    System.out.println("keypadMode == 5");
                    for (int k = 0; k < keys[j].rus_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].rus_code.get(k)) {
                            keyNum = keys[j].key_code;
                            if (keyNumPrew.equals(keyNum)) tcpSocket.setFlagPause(true, 2);
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;

                        }
                    }
                    //if (exit)
                    //  break;
                    //System.out.println("keyNum = " + keyNum);
                    //System.out.println("pressCount = " + pressCount);
                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount++;
                        if (keyNumPrew.equals(keyNum)) tcpSocket.setFlagPause(true, 2);
                        pressKeyBot(keyNum, 0, pressCount);
                        exit = true;
                    }
                    //   System.out.println("keyNum = " + keyNum);
                    // System.out.println("pressCount = " + pressCount);
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
                            if (keyNumPrew.equals(keyNum)) tcpSocket.setFlagPause(true, 2);
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
                            if (keyNumPrew.equals(keyNum)) tcpSocket.setFlagPause(true, 2);
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                    //if (!exit) pressCount = 8;
                    for (int k = 0; k < keys[j].eng_code.size(); k++) {
                        if (exit)
                            break;
                        pressCount++;
                        if (charsetNumber == keys[j].eng_code.get(k)) {
                            keyNum = keys[j].key_code;
                            if (keyNumPrew.equals(keyNum)) tcpSocket.setFlagPause(true, 2);
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                    //   if (!exit) pressCount = 14;
                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount++;
                        if (keyNumPrew.equals(keyNum)) tcpSocket.setFlagPause(true, 2);
                        pressKeyBot(keyNum, 0, pressCount);
                        exit = true;
                    }
                }
                keyNumPrew = keyNum;



  /*          // Русские символы + цифры
            if (keypadMode == CYRILLIC + NUMBERS)
            {

            }

            // Английские символы + цифры
            if (keypadMode == ENGLISH + NUMBERS)
            {

            }

            // Русские символы + Английские символы + цифр
            if (keypadMode == CYRILLIC +ENGLISH + NUMBERS)
            {

            }//*/
                //   System.out.println("keyNum = " + keyNum);
                // System.out.println("pressCount = " + pressCount);

            }
        }
        //  System.out.println("keyNum = " + keyNum);
        // System.out.println("pressCount = " + pressCount);
        return 0;
    }

    private void initKey(Keypad keys[]) {
        // Keypad[] keys = new Keypad[keypad.keys_table_size];
        //=======================================================================================================
        // KEY №0 - цифра 0 на клавиатуре
        keys[0].key_code = 35;
        // Доступ в режимах
        keys[0].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.SPEC_SYMBOLS);
        // Цифра
        keys[0].key_number = 0x30;
        // Специальные символы
        keys[0].spec_sym_code.add(0x40);
        keys[0].spec_sym_code.add(0x23);
        keys[0].spec_sym_code.add(0x24);
        keys[0].spec_sym_code.add(0x25);
        keys[0].spec_sym_code.add(0x26);
        keys[0].spec_sym_code.add(0x2A);
        //=======================================================================================================
        //=======================================================================================================
        // KEY №1 - цифра 1 на клавиатуре
        keys[1].key_code = 27;
        // Доступ в режимах
        keys[1].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[1].key_number = 0x31;
        // Русские заглавные
        keys[1].rus_code.add(0x94);
        keys[1].rus_code.add(0x95);
        keys[1].rus_code.add(0x96);
        keys[1].rus_code.add(0x97);
        // Руские прописные
        keys[1].rus_code.add(0xE4);
        keys[1].rus_code.add(0xE5);
        keys[1].rus_code.add(0xE6);
        keys[1].rus_code.add(0xE7);
        // Английские заглавные
        keys[1].eng_code.add(0x50);
        keys[1].eng_code.add(0x51);
        keys[1].eng_code.add(0x52);
        keys[1].eng_code.add(0x53);
        // Английские прописные
        keys[1].eng_code.add(0x70);
        keys[1].eng_code.add(0x71);
        keys[1].eng_code.add(0x72);
        keys[1].eng_code.add(0x73);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №2
        keys[2].key_code = 28;
        // Доступ в режимах
        keys[2].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[2].key_number = 0x32;

        // Русские заглавные
        keys[2].rus_code.add(0x98);
        keys[2].rus_code.add(0x99);
        keys[2].rus_code.add(0x9A);
        keys[2].rus_code.add(0x9B);
        // Руские прописные
        keys[2].rus_code.add(0xE8);
        keys[2].rus_code.add(0xE9);
        keys[2].rus_code.add(0xEA);
        keys[2].rus_code.add(0xEB);
        // Английские заглавные
        keys[2].eng_code.add(0x54);
        keys[2].eng_code.add(0x55);
        keys[2].eng_code.add(0x56);
        // Английские прописные
        keys[2].eng_code.add(0x74);
        keys[2].eng_code.add(0x75);
        keys[2].eng_code.add(0x76);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №3
        keys[3].key_code = 29;
        // Цифра
        keys[3].key_number = 0x33;

        // Доступ в режимах
        keys[3].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Русские заглавные
        keys[3].rus_code.add(0x9C);
        keys[3].rus_code.add(0x9D);
        keys[3].rus_code.add(0x9E);
        keys[3].rus_code.add(0x9F);
        // Руские прописные
        keys[3].rus_code.add(0xEC);
        keys[3].rus_code.add(0xED);
        keys[3].rus_code.add(0xEE);
        keys[3].rus_code.add(0xEF);
        // Английские заглавные
        keys[3].eng_code.add(0x57);
        keys[3].eng_code.add(0x58);
        keys[3].eng_code.add(0x59);
        keys[3].eng_code.add(0x5A);
        // Английские прописные
        keys[3].eng_code.add(0x77);
        keys[3].eng_code.add(0x78);
        keys[3].eng_code.add(0x79);
        keys[3].eng_code.add(0x7A);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №4
        keys[4].key_code = 19;
        // Доступ в режимах
        keys[4].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[4].key_number = 0x34;

        // Русские заглавные
        keys[4].rus_code.add(0x88);
        keys[4].rus_code.add(0x89);
        keys[4].rus_code.add(0x8A);
        keys[4].rus_code.add(0x8B);
        // Руские прописные
        keys[4].rus_code.add(0xA8);
        keys[4].rus_code.add(0xA9);
        keys[4].rus_code.add(0xAA);
        keys[4].rus_code.add(0xAB);
        // Английские заглавные
        keys[4].eng_code.add(0x47);
        keys[4].eng_code.add(0x48);
        keys[4].eng_code.add(0x49);
        // Английские прописные
        keys[4].eng_code.add(0x67);
        keys[4].eng_code.add(0x68);
        keys[4].eng_code.add(0x69);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №5
        keys[5].key_code = 20;
        // Доступ в режимах
        keys[5].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[5].key_number = 0x35;

        // Русские заглавные
        keys[5].rus_code.add(0x8C);
        keys[5].rus_code.add(0x8D);
        keys[5].rus_code.add(0x8E);
        keys[5].rus_code.add(0x8F);
        // Руские прописные
        keys[5].rus_code.add(0xAC);
        keys[5].rus_code.add(0xAD);
        keys[5].rus_code.add(0xAE);
        keys[5].rus_code.add(0xAF);
        // Английские заглавные
        keys[5].eng_code.add(0x4A);
        keys[5].eng_code.add(0x4B);
        keys[5].eng_code.add(0x4C);
        // Английские прописные
        keys[5].eng_code.add(0x6A);
        keys[5].eng_code.add(0x6B);
        keys[5].eng_code.add(0x6C);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №6
        keys[6].key_code = 21;
        // Доступ в режимах
        keys[6].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[6].key_number = 0x36;

        // Русские заглавные
        keys[6].rus_code.add(0x90);
        keys[6].rus_code.add(0x91);
        keys[6].rus_code.add(0x92);
        keys[6].rus_code.add(0x93);
        // Руские прописные
        keys[6].rus_code.add(0xE0);
        keys[6].rus_code.add(0xE1);
        keys[6].rus_code.add(0xE2);
        keys[6].rus_code.add(0xE3);
        // Английские заглавные
        keys[6].eng_code.add(0x4D);
        keys[6].eng_code.add(0x4E);
        keys[6].eng_code.add(0x4F);
        // Английские прописные
        keys[6].eng_code.add(0x6D);
        keys[6].eng_code.add(0x6E);
        keys[6].eng_code.add(0x6F);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №7
        keys[7].key_code = 11;
        // Цифра
        keys[7].key_number = 0x37;

        // Доступ в режимах
        keys[7].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.SPEC_SYMBOLS);
        // Специальные символы
        keys[7].spec_sym_code.add(0x2E);
        keys[7].spec_sym_code.add(0x2C);
        keys[7].spec_sym_code.add(0x21);
        keys[7].spec_sym_code.add(0x3F);
        keys[7].spec_sym_code.add(0x28);
        keys[7].spec_sym_code.add(0x29);
        keys[7].spec_sym_code.add(0x3A);
        keys[7].spec_sym_code.add(0x3B);
        keys[7].spec_sym_code.add(0x27);
        keys[7].spec_sym_code.add(0x22);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №8
        keys[8].key_code = 12;
        // Доступ в режимах
        keys[8].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[8].key_number = 0x38;

        // Русские заглавные
        keys[8].rus_code.add(0x80);
        keys[8].rus_code.add(0x81);
        keys[8].rus_code.add(0x82);
        keys[8].rus_code.add(0x83);
        // Руские прописные
        keys[8].rus_code.add(0xA0);
        keys[8].rus_code.add(0xA1);
        keys[8].rus_code.add(0xA2);
        keys[8].rus_code.add(0xA3);
        // Английские заглавные
        keys[8].eng_code.add(0x41);
        keys[8].eng_code.add(0x42);
        keys[8].eng_code.add(0x43);
        // Английские прописные
        keys[8].eng_code.add(0x61);
        keys[8].eng_code.add(0x62);
        keys[8].eng_code.add(0x63);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №9
        keys[9].key_code = 13;
        // Доступ в режимах
        keys[9].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[9].key_number = 0x39;

        // Русские заглавные
        keys[9].rus_code.add(0x84);
        keys[9].rus_code.add(0x85);
        keys[9].rus_code.add(0x86);
        keys[9].rus_code.add(0x87);
        // Руские прописные
        keys[9].rus_code.add(0xA4);
        keys[9].rus_code.add(0xA5);
        keys[9].rus_code.add(0xA6);
        keys[9].rus_code.add(0xA7);
        // Английские заглавные
        keys[9].eng_code.add(0x44);
        keys[9].eng_code.add(0x45);
        keys[9].eng_code.add(0x46);
        // Английские прописные
        keys[9].eng_code.add(0x64);
        keys[9].eng_code.add(0x65);
        keys[9].eng_code.add(0x66);

        //=======================================================================================================
        //=======================================================================================================
        // KEY ОТМЕНА
        keys[10].key_code = 0;
        // Доступ в режимах
        keys[10].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №1
        keys[11].key_code = 1;
        // Доступ в режимах
        keys[11].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №2
        keys[12].key_code = 2;
        // Доступ в режимах
        keys[12].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №3
        keys[13].key_code = 3;
        // Доступ в режимах
        keys[13].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №4
        keys[14].key_code = 4;
        // Доступ в режимах
        keys[14].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №5
        keys[15].key_code = 5;
        // Доступ в режимах
        keys[15].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №6
        keys[16].key_code = 6;
        // Доступ в режимах
        keys[16].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №7
        keys[17].key_code = 7;
        // Доступ в режимах
        keys[17].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №8
        keys[18].key_code = 8;
        // Доступ в режимах
        keys[18].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №9
        keys[19].key_code = 9;
        // Доступ в режимах
        keys[19].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №10
        keys[20].key_code = 10;
        // Доступ в режимах
        keys[20].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY BACKSPACE
        keys[21].key_code = 14;
        // Доступ в режимах
        keys[21].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №15
        keys[22].key_code = 15;
        // Доступ в режимах
        keys[22].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №16
        keys[23].key_code = 16;
        // Доступ в режимах
        keys[23].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №17
        keys[24].key_code = 17;
        // Доступ в режимах
        keys[24].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №18
        keys[25].key_code = 18;
        // Доступ в режимах
        keys[25].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №22
        keys[26].key_code = 22;
        // Доступ в режимах
        keys[26].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №23
        keys[27].key_code = 23;
        // Доступ в режимах
        keys[27].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY
        keys[28].key_code = 24;
        // Доступ в режимах
        keys[28].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №25
        keys[29].key_code = 25;
        // Доступ в режимах
        keys[29].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №26
        keys[30].key_code = 26;
        // Доступ в режимах
        keys[30].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №30
        keys[31].key_code = 30;
        // Доступ в режимах
        keys[31].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №31
        keys[32].key_code = 31;
        // Доступ в режимах
        keys[32].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №32
        keys[33].key_code = 32;
        // Доступ в режимах
        keys[33].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №33
        keys[34].key_code = 33;
        // Доступ в режимах
        keys[34].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №34
        keys[35].key_code = 34;
        // Доступ в режимах
        keys[35].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY 00
        keys[36].key_code = 36;
        // Доступ в режимах
        keys[36].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY Comma
        keys[37].key_code = 37;
        // Доступ в режимах
        keys[37].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.SPEC_SYMBOLS + keypadMode.NUMBERS + keypadMode.ACTION_MODE);
        keys[37].key_number = 0x2C;
        keys[37].spec_sym_code.add(0x2B);
        keys[37].spec_sym_code.add(0x2D);
        keys[37].spec_sym_code.add(0x2F);
        keys[37].spec_sym_code.add(0x3D);
        keys[37].spec_sym_code.add(0x5E);
        keys[37].spec_sym_code.add(0x5F);
        keys[37].spec_sym_code.add(0x7B);
        keys[37].spec_sym_code.add(0x7D);
        keys[37].spec_sym_code.add(0xB3);
        keys[37].spec_sym_code.add(0x7E);
        //=======================================================================================================
        // KEY №38
        keys[38].key_code = 38;
        // Доступ в режимах
        keys[38].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №39
        keys[39].key_code = 39;
        // Доступ в режимах
        keys[39].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================*/
    }

    private void writeLogFile(String text) {
        try {
            String strToFile = curdate.getTime() + "    " + text + "\n";
            Files.write(Paths.get("log.txt"), strToFile.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

  /*  private void enterPassBot() {
        System.out.println("in enterPassBot");
        boolean compare = compareScreen(screenPicture.PASSWORD);
        //если полученный экран с кассы совпадает с экраном ввода пароля, то выполняем if
        if (compare) {
            System.out.println("in enterPassBot compare == true");
            String pass = searchForKeyword("password: ");
            if (pass == "CANNOT FIND KEYWORD")
                writeLogFile("Пароль не найден в файле сценария.");
            strToKeypadConvert(pass);
            compare = compareScreen(screenPicture.INCORRECT_PASSWORD);
            if (compare) writeLogFile("Введен неверный пароль");
            else writeLogFile("Введен верный пароль");
        } else {
            writeLogFile("Экран ввода пароля не открыт.");
            return;
        }
    }

    private void checkPrintRefundConsumption() throws IOException {
        System.out.println("in checkPrintRefundConsuption");
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key3, 0, 1);
        pressKeyBot(keyEnum.key2, 0, 1);
        String countCheckStr = searchForKeyword("check_count: ");
        if (countCheckStr.equals("CANNOT FIND KEYWORD"))
            writeLogFile("Cannot find check count in input file");
        else {
            String tmpGoodsStr = searchForKeyword("Good ");
            if (tmpGoodsStr.equals("CANNOT FIND KEYWORD"))
                writeLogFile("Goods not found in input file");
            else {
                int countChecks = Integer.parseInt(countCheckStr);
                //if (compare) {
                for (int i = 0; i < countChecks; i++) {
                    pressKeyBot(keyEnum.key2, 0, 1);
                    checkPrint();
                    tcpSocket.setFlagPause(true, 15);
                }
            }
        }
    }

    private void checkPrintRefundComming() throws IOException {
        System.out.println("in checkPrintRefundComming");
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key3, 0, 1);
        pressKeyBot(keyEnum.key2, 0, 1);
        String countCheckStr = searchForKeyword("check_count: ");
        if (countCheckStr.equals("CANNOT FIND KEYWORD"))
            writeLogFile("Cannot find check count in input file");
        else {
            String tmpGoodsStr = searchForKeyword("Good ");
            if (tmpGoodsStr.equals("CANNOT FIND KEYWORD"))
                writeLogFile("Goods not found in input file");
            else {
                int countChecks = Integer.parseInt(countCheckStr);
                //if (compare) {
                for (int i = 0; i < countChecks; i++) {
                    pressKeyBot(keyEnum.key1, 0, 1);
                    checkPrint();
                    // pressKeyBot(keyEnum.stopDocument, 1);
                    //tcpSocket.setFlagPause(true, 15);
                }
            }
        }
    }

    private void checkPrintSaleConsumption() throws IOException {
        System.out.println("in checkPrintSaleConsuption");
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key3, 0, 1);
        String countCheckStr = searchForKeyword("check_count: ");
        if (countCheckStr.equals("CANNOT FIND KEYWORD"))
            writeLogFile("Cannot find check count in input file");
        else {
            String tmpGoodsStr = searchForKeyword("Good ");
            if (tmpGoodsStr.equals("CANNOT FIND KEYWORD"))
                writeLogFile("Goods not found in input file");
            else {
                int countChecks = Integer.parseInt(countCheckStr);
                //if (compare) {
                for (int i = 0; i < countChecks; i++) {
                    pressKeyBot(keyEnum.key1, 0, 1);
                    checkPrint();
                    //   pressKeyBot(keyEnum.stopDocument, 1);
                    tcpSocket.setFlagPause(true, 15);
                }
            }
        }
        //     tcpSocket.setFlagTcpSocket(true);
        //   tcpSocket.createSocket(cashboxIP.getText(), Integer.parseInt(cashboxPort.getText()));
    }













    private void checkPrint(List <String> keyWordArray) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!in checkPrint");
        String tmpGoodsStr;
        //System.out.println("countGoods = " + sale.getCountGoods());
        for (int j = 0; j < 1/*sale.getCountGoods()*/;/* j++) {
            //bool compare = compareScreen(SCREEN);
            //if (compare)
            //{
            tmpGoodsStr = searchForKeyword("good_from_" + (j + 1) + ": ", keyWordArray);
            System.out.println("good_from_ = " + tmpGoodsStr);
            if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                writeLogFile("Goods not found in input file");
                return;
            }
            if (tmpGoodsStr.equals("good_from_base")) {
                pressKeyBot(keyEnum.keyGoods, 0, 1);
                tmpGoodsStr = searchForKeyword("good_code_" + (j + 1) + ": ", keyWordArray);
                System.out.println();
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
                tmpGoodsStr = searchForKeyword("good_base_free_price_" + (j + 1) + ": ", keyWordArray);
                //       System.out.println("(free price base) tmpGoodsStr = " + tmpGoodsStr);
                if (!tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                    System.out.println("tmpGoodsStr = " + tmpGoodsStr);
                    strToKeypadConvert(tmpGoodsStr);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
                tmpGoodsStr = searchForKeyword("good_type_" + (j + 1) + ": ", keyWordArray);
                if (tmpGoodsStr.equals("weighted")) {
                    tmpGoodsStr = searchForKeyword("good_weight_" + (j + 1) + ": ", keyWordArray);
                    if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                        writeLogFile("Goods type not found in input file");
                        return;
                    }
                    strToKeypadConvert(tmpGoodsStr);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
            }
            if (tmpGoodsStr.equals("good_free_price")) {
                System.out.println("tmpGoodsStr.equals(\"good_free_price\")");
                tmpGoodsStr = searchForKeyword("good_price_" + (j + 1) + ": ", keyWordArray);
                System.out.println("tmpGoodsStr = " + tmpGoodsStr);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            tmpGoodsStr = searchForKeyword("good_type_" + (j + 1) + ": ", keyWordArray);
            if (tmpGoodsStr.equals("countable")) {
                tmpGoodsStr = searchForKeyword("good_count_" + (j + 1) + ": ", keyWordArray);
                if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
                    writeLogFile("Goods count not found in input file");
                    return;
                }
                if (Integer.parseInt(tmpGoodsStr) > 1) {
                    pressKeyBot(keyEnum.keyQuantity, 0, 1);
                    strToKeypadConvert(tmpGoodsStr);
                    pressKeyBot(keyEnum.keyEnter, 0, 1);
                }
            }

            tmpGoodsStr = searchForKeyword("spacial_form_" + (j + 1) + ": ", keyWordArray);
            if (!tmpGoodsStr.equals("CANNOT FIND KEYWORD"))
                holdKey(keyEnum.keyMenu, 0, 1);
            if (tmpGoodsStr.equals("ПР"))
                pressKeyBot(keyEnum.key1, 0, 1);
            if (tmpGoodsStr.equals("ЧП")) {
                pressKeyBot(keyEnum.key2, 0, 1);
                tmpGoodsStr = searchForKeyword("special_form_prepayment_sum_" + (j + 1) + ": ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            if (tmpGoodsStr.equals("А"))
                pressKeyBot(keyEnum.key3, 0, 1);
            if (tmpGoodsStr.equals("П")){
                pressKeyBot(keyEnum.key4, 0, 1);
                tmpGoodsStr = searchForKeyword("special_form_offset_of_prepayment_sum_" + (j + 1) + ": ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            if (tmpGoodsStr.equals("КР"))
                pressKeyBot(keyEnum.key5, 0, 1);
            if (tmpGoodsStr.equals("ЧК")) {
                pressKeyBot(keyEnum.key6, 0, 1);
                tmpGoodsStr = searchForKeyword("special_form_credit_sum_" + (j + 1) + ": ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            if (tmpGoodsStr.equals("К")) {
                pressKeyBot(keyEnum.key7, 0, 1);
                tmpGoodsStr = searchForKeyword("special_form_credit_pay_sum_" + (j + 1) + ": ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
            }
            pressKeyBot(keyEnum.keyEnter, 0, 1);
        }

        //Тип оплаты чека
        tmpGoodsStr = searchForKeyword("type_pay: ", keyWordArray);
        if (tmpGoodsStr.equals("CANNOT FIND KEYWORD")) {
            writeLogFile("Pay type not found in input file");
            return;
        } else {
            if (tmpGoodsStr.equals("cash_pay")) {
                pressKeyBot(keyEnum.keyPayByCash, 0, 1);
                tmpGoodsStr = searchForKeyword("sum_pay: ", keyWordArray);
                strToKeypadConvert(tmpGoodsStr);
            }
            if (tmpGoodsStr.equals("card_pay")) {
                pressKeyBot(keyEnum.keyPayByCard, 0, 1);
            }

            pressKeyBot(keyEnum.keyEnter, 0, 1);
        }
        tcpSocket.setFlagPause(true, 20);
    }*/

    private void pressKeyBot(int keyNum, int keyNum2,  int pressCount) {
        for (int i = 0; i < pressCount; i++)
            tcpSocket.sendPressKey(keyNum, keyNum2, 1);
    }

    private void holdKey(int keyNum, int keyNum2,  int pressCount) {
        tcpSocket.setFlagPauseEnter(true);
        tcpSocket.sendPressKey(keyNum, keyNum2, 2);
    }

    private boolean compareScreen(ScreenPicture action) {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
            FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strFromFile = br.readLine();

            switch (action) {
                case PASSWORD:
                    return strFromFile.equals(screens.passwodScreen);

                case INCORRECT_PASSWORD:
                    return strFromFile.equals(screens.incorrectPasswodScreen);

                case AFTER_PASSWORD:
                    return strFromFile.equals(screens.menuAfterPasswdScreen);

                case MENU_REGISTRATION:
                    return strFromFile.equals(screens.menuRegistrationScreen);

                case WRONG_REG_NUMBER:
                    return strFromFile.equals(screens.wrongRegNumberScreen);

                case REGISTRATION_FISCAL_MODE:
                    return strFromFile.equals(screens.registrationFiscalModeScreen);

                case GET_REGISTRATION_DATA_FROM_CABINET:
                    return strFromFile.equals(screens.getRegistrationDataFromCabinetScreen);

                case EMPTY_SCREEN:
                    return strFromFile.equals(screens.emptyScreen);

                case RE_REGISTRATION_MENU:
                    return strFromFile.equals(screens.reRegistrationMenuScreen);

                case RE_REGISTRATION_MENU_NOT_AUTONOMIC:
                    return strFromFile.equals(screens.reRegistrationMenuNotAutonomicScreen);

                case DOCUMENT_NOT_SENDED_SCREEN:
                    return strFromFile.equals(screens.documentNotSendedScreen);

                case TURN_OFF_CASHBOX:
                    return strFromFile.equals(screens.turnOffScreen);

                case OPEN_SHIFT_MENU:
                    return strFromFile.equals(screens.openShiftMenuScreen);

                case SHIFT_MENU_OPEN_SHIFT:
                    return strFromFile.equals(screens.shiftMenuOpenShiftScreen);

                case LESS_MONEY_IN_CASHBOX:
                    return strFromFile.equals(screens.lessMoneyInCashboxtScreen);

                case FREE_SALE_MODE:
                    return strFromFile.equals(screens.freeSaleModeScreen);

                default:
                    return false;
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Ошибка compareScreen");
        }
        return false;
    }
}