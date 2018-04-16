package cashbox;

import keypad.KeyEnum;
import keypad.Keypad;
import keypad.KeypadMode;
import remoteAccess.DataFromCashbox;
import remoteAccess.SQLCommands;
import remoteAccess.TCPSocket;
import screens.Screens;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

//Содержит тесты, которые будут выполняться на кассе

public class CashTest {
    public CashTest() throws FileNotFoundException {
    }

    private FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
    public BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

    //Текущая дата, используется для записи в лог
    private Calendar curdate = Calendar.getInstance();
    private List<String> dateStr = new ArrayList<>();

    //объекты для выполнения тестов
    private Config config = new Config();
    private Screens screens = new Screens();
    private Keypad keypad = new Keypad();
    private KeypadMode keypadMode = new KeypadMode();
    private static int keypad_mode = 0; //= keypadMode.FREE_MODE;//SPEC_SYMBOLS;//ENGLISH;//CYRILLIC;
    private KeyEnum keyEnum = new KeyEnum();
    private TCPSocket tcpSocket = new TCPSocket();
    private DataFromCashbox dataFromCashbox = new DataFromCashbox();
    private SQLCommands sqlCommands = new SQLCommands();
    private ParseCount parseCount = new ParseCount();

    // возвращает данные с кассы
    public List<String> cashBoxConnect(String command) {
        return dataFromCashbox.executeListCommand(command);
    }

    //Инициализация клавиатуры в зависимости от типа кассы
    public void initializationKeyboard() {
        keyEnum.initKeyEnum();
    }

    //установка соединения
    public void connectionSetup() {
        //  tcpSocket.setFlagReceiveScreen(true);
        tcpSocket.createSocket(Config.CASHBOX_IP, Config.CASHBOX_PORT);
        dataFromCashbox.initSession(Config.CASHBOX_IP, Config.USERNAME, Config.PORT, Config.PASSWORD);
    }

}

    //---------------------------Предусловия--------------------------/
    // Инициализация клавиатуры, установка ssh-соединения, создания сокета
    // Сброс кассы (выполнение тех. обнуления), добавление товаров
  /*  @Before
    public void before_test_clear_cashbox() {
        initializationKeyboard();
        connectionSetup();

        //проверяем, что открыт экран ввода пароля
        enterPasswordIfScreenOpen();
        //делаем тех. обнуление на кассе
        techNull();
        tcpSocket.socketClose();
        //перезапускаем фискат
        cashBoxConnect("/sbin/reboot");
        sleepMiliSecond(25000);
    //    tcpSocket.setFlagPause(true, 25);
        connectionSetup();
     //   tcpSocket.setFlagPause(false, 0);
        //добавляем в БД товаров все виды товаров
        //addGoodsOnCash(); *//*

    }

    //---------------------------Постусловия--------------------------/
    // Закрытие сокета и ssh-соединения
    @After
    public void after_test_clear_cashbox() {
        dataFromCashbox.disconnectSession();
        tcpSocket.socketClose();
    }

    //-------------------------Тесты на регистрацию---------------------------------/
    //------------Тест на регистрацию через ККТ, в автономном режиме----------------/
    @Test
    public void correct_registration_autonomic() throws IOException {
        //проверяем, что stage кассы = 0
        String getStageCommand = sqlCommands.getStageCommand();
        List<String> line = cashBoxConnect(getStageCommand);
        if (!line.get(0).equals("2")) {
            enterPasswordIfScreenOpen();
            List<String> listScript = readDataScript("src\\test\\resourses\\registration_correct_autonomic.txt");

            int testResult = registration(listScript);
            switch (testResult) {
                case -1:
                    fail("Пункт регистрации недоступен в меню.");
                    break;
                case -2:
                    fail("Пустой ИНН в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -3:
                    fail("Пустое наименование организации в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -4:
                    fail("Пустой адрес рассчетов в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -5:
                    fail("Пустое место рассчетов в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -6:
                    fail("Пустой РН ККТ в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -7:
                    fail("Неверный РН ККТ в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                default:
                    break;
            }

            line = cashBoxConnect(getStageCommand);
            if (line.get(0).equals("2")) {
                pressKeyBot(keyEnum.key1, 0, 1);
                String strFromFile = br.readLine();
                assertEquals(strFromFile, screens.reRegistrationMenuScreen);
            } else
                fail("После регистрации stage != 2 ");
        } else
            fail("Stage = 2, Касса зарегистрирована!");
    }

    //--------Тесты на регистрацию через ККТ, в режиме передачи данных--------------/
    @Test
    public void correct_registration_not_autonomic() throws IOException {
        //проверяем, что stage кассы = 0
        String getStageCommand = sqlCommands.getStageCommand();
        List<String> line = cashBoxConnect(getStageCommand);
        if (!line.get(0).equals("2")) {
            enterPasswordIfScreenOpen();
            List<String> listScript = readDataScript("src\\test\\resourses\\correct_registration_not_autonomic.txt");

            int testResult = registration(listScript);
            switch (testResult) {
                case -1:
                    fail("Пункт регистрации недоступен в меню.");
                    break;
                case -2:
                    fail("Пустой ИНН в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -3:
                    fail("Пустое наименование организации в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -4:
                    fail("Пустой адрес рассчетов в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -5:
                    fail("Пустое место рассчетов в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -6:
                    fail("Пустой РН ККТ в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                case -7:
                    fail("Неверный РН ККТ в пункте регистрации. Завершить регистрацию невнозможно.");
                    break;
                default:
                    break;
            }

            line = cashBoxConnect(getStageCommand);
            if (line.get(0).equals("2")) {
                pressKeyBot(keyEnum.key1, 0, 1);
                String strFromFile = br.readLine();
                assertEquals(strFromFile, screens.reRegistrationMenuScreen);
            } else
                fail("После регистрации stage != 2 ");
        } else
            fail("Stage = 2, Касса зарегистрирована!");
    }
    //------------------------------------------------------------------------------/

    //----------------------------Тесты на перерегистрацию---------------------------/
    //------------ККТ зарегана в автономном режиме, изменяем ланные юр.лица----------/
    @Test
    public void correct_re_registration_legal_entity() throws IOException {
        //проверяем, что stage кассы = 2
        String getStage = sqlCommands.getStageCommand();
        List<String> line = cashBoxConnect(getStage);
        enterPasswordIfScreenOpen();

        //регистрируем кассу, если она не зарегистирована
        if (!line.get(0).equals("2")) {
            List<String> listScript = readDataScript("src\\test\\resourses\\registration_correct_autonomic.txt");
            int testResult = registration(listScript);
            if (testResult != 0)
                fail("Касса не зарегисрирована, перерегистрация невозможна.");
        }

        line = cashBoxConnect(getStage);
        if (line.get(0).equals("2")) {
            List<String> listScript = readDataScript("src\\test\\resourses\\correct_reregistration_legal_entity.txt");
            int testResul = re_registrationLegalEntity(listScript);

            line.clear();
            String getData = "echo \"attach '/FisGo/configDb.db' as config; " +
                    "select ORGANIZATION_NAME from config.CONFIG;" +
                    "select CALCULATION_ADDRESS from config.CONFIG;" +
                    "select CALCULATION_PLACE from config.CONFIG;\"" +
                    " | sqlite3 /FisGo/configDb.db\n";
            line = cashBoxConnect(getData);

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
    //---------------------------------------------------------------------------------/

    //-----------------------------------Тесты на продажу------------------------------/
    //ККТ в учебном режиме, продажа - приход, наличные, товар по свободной цене, итог 100 руб.
    @Test
    public void sale_advent_learning_mode_cash() {
        //проверяем, что stage кассы = 0
        List<String> line = cashBoxConnect(sqlCommands.getStageCommand());
        if (line.get(0).equals("0")) {
            enterPasswordIfScreenOpen();
            line.clear();
            //делаем выборку из конфига на кассе, проверем, открыта смена или нет
            line = cashBoxConnect(sqlCommands.getOpenShiftCommand());
            if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
                if (openShift() != 0)
                    fail("Ошибка при открытии смены");
            }

            //делаем выборку из базы чеков
            line.clear();
            line = cashBoxConnect(sqlCommands.getRecieptCountCommand());
            int countCheck = Integer.parseInt(line.get(0));
            line.clear();

            //делаем выборку из базы счетчиков
            int [] countBegin = parseCount.parseCountValueFromStr(cashBoxConnect(sqlCommands.getCountersAdventValueCommand()).get(0));

            //читаем из файла сценарий пробития чека
            List<String> listScript = readDataScript("src\\test\\resourses\\free_price_total_100_cash.txt");
            int testResult = checkPrintSaleComming(listScript, ScreenPicture.FREE_SALE_MODE_CHANGE_400);
            switch (testResult) {
                case -1:
                    fail("Не открыт экран продажи (режим свободной цены)");
                    break;
                case -2:
                    fail("Не найдены товары, которые необходимо добавить в чек");
                    break;
                case -3:
                    fail("Не найдены товары, которые необходимо добавить в чек в методе печати чека");
                    break;
                case -4:
                    fail("Не указан способ добавления товара в чек");
                    break;
                case -5:
                    fail("Не указано количество товара во входном файле сценария");
                    break;
                case -6:
                    fail("Не указан способ оплаты во входном файле сценария");
                    break;
                case -7:
                    fail("Не указан тип товара во входном файле сценария");
                    break;
                case -8:
                    fail("Не совпадает дисплей кассы с ожидаемым экраном сдачи");
                    break;
                case 0: {
                    line = cashBoxConnect(sqlCommands.getRecieptCountCommand());
                    int countCheckTest = Integer.parseInt(line.get(0));
                    if ((countCheckTest - countCheck) == 1) {
                        //сделать выборку из базы чеков, по дате
                        String getCheckDateCommand = "echo \"attach '/FisGo/receiptsDb.db' as receipts; " +
                                "select RECEIPT_CREATE_DATE from receipts.RECEIPTS ORDER BY ID DESC limit 1;\" | sqlite3 /FisGo/receiptsDb.db\n";
                        line = cashBoxConnect(getCheckDateCommand);
                        String receiptDate = line.get(0);
                        if (receiptDate.length() == 12) {
                            StringBuilder dateStrBuild = new StringBuilder();
                            dateStrBuild.append(receiptDate.substring(0, 4));
                            dateStrBuild.append(receiptDate.substring(6));
                            receiptDate = dateStrBuild.toString();
                        }
                        writeLogFile("Тест печати чека в учебном режиме. Дата на кассе: " + dateStr.get(0) + "Дата чека: " + receiptDate);

                        //делаем выборку из базы счетчиков
                        int[] countAfter = parseCount.parseCountValueFromStr(cashBoxConnect(sqlCommands.getCountersAdventValueCommand()).get(0));

                        //Подсчет общей суммы чека
                        //  int totalSum = totalReceiptSum(listScript);
                        String typePayStr = searchForKeyword("type_pay: ", listScript);
                        int typePay = -1;
                        if (typePayStr.equals("cash_pay"))
                            typePay = 0;
                        if (typePayStr.equals("card_pay"))
                            typePay = 1;

                        int assertCountRes = assertCount(countBegin, countAfter, 0, 10000, typePay);//totalSum);
                        if (assertCountRes == 0) {
                            assertEquals(dateStr.get(0), receiptDate);
                        } else {
                            fail("Неверное изменение в базе счетчиков, assertCount = " + assertCountRes);
                        }
                    } else {
                        fail("Чек не добавлен в базу receiptsDb.db");
                    }
                    break;
                }
                default:
                    break;
            }
        } else {
            fail("Касса не в учебном режиме");
        }
    }
    //ККТ в учебном режиме, продажа - приход, электронные, товар по свободной цене, итог 100 руб.
    @Test
    public void sale_advent_learning_mode_card() {
        //проверяем, что stage кассы = 0
        List<String> line = cashBoxConnect(sqlCommands.getStageCommand());
        if (line.get(0).equals("0")) {
            //проверяем, что открыт экран ввода пароля; при необходимости вводим пароль
            enterPasswordIfScreenOpen();

            line.clear();
            //делаем выборку их конфига на кассе, проверем, открыта смена или нет
            line = cashBoxConnect(sqlCommands.getOpenShiftCommand());
            if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
                openShift();
            }

            //делаем выборку из базы чеков
            line.clear();
            line = cashBoxConnect(sqlCommands.getRecieptCountCommand());
            int countCheckBeforeTest = Integer.parseInt(line.get(0));
            line.clear();

            //проверяем, включен терминал или нет. При необходимости включаем терминал
            line = cashBoxConnect(sqlCommands.getTerminalModeCommand());
            //если терминал выключен, то включаем
            if (line.get(0).equals("0")) {
                int result = externalTerninalTurnOn();
                if (result == -1)
                    fail("Банковский терминал выключен");
            }
            line.clear();

            //делаем выборку из базы счетчиков
            int [] countBegin = parseCount.parseCountValueFromStr(cashBoxConnect(sqlCommands.getCountersAdventValueCommand()).get(0));

            //читаем из файла сценарий пробития чека
            List<String> listScript = readDataScript("src\\test\\resourses\\sale_advent_treaning_mode_free_price_card.txt");
            int testResult = checkPrintSaleComming(listScript, ScreenPicture.GIVE_CARD_AND_RECEIPT);
            switch (testResult) {
                case -1:
                    fail("Не открыт экран продажи (режим свободной цены)");
                    break;
                case -2:
                    fail("Не найдены товары, которые необходимо добавить в чек");
                    break;
                case -3:
                    fail("Не найдены товары, которые необходимо добавить в чек в методе печати чека");
                    break;
                case -4:
                    fail("Не указан способ добавления товара в чек");
                    break;
                case -5:
                    fail("Не указано количество товара во входном файле сценария");
                    break;
                case -6:
                    fail("Не указан способ оплаты во входном файле сценария");
                    break;
                case -7:
                    fail("Не указан тип товара во входном файле сценария");
                    break;
                case -8:
                    fail("Не совпадает дисплей кассы с ожидаемым экраном сдачи");
                    break;
                case 0: {
                    line = cashBoxConnect(sqlCommands.getRecieptCountCommand());
                    int countCheckAfterTest = Integer.parseInt(line.get(0));
                    if ((countCheckAfterTest - countCheckBeforeTest) == 1) {
                        //сделать выборку из базы чеков, по дате
                        String getCheckDateCommand = "echo \"attach '/FisGo/receiptsDb.db' as receipts; " +
                                "select RECEIPT_CREATE_DATE from receipts.RECEIPTS ORDER BY ID DESC limit 1;\" | sqlite3 /FisGo/receiptsDb.db\n";
                        line = cashBoxConnect(getCheckDateCommand);
                        String receiptDate = line.get(0);
                        if (receiptDate.length() == 12) {
                            StringBuilder dateStrBuild = new StringBuilder();
                            dateStrBuild.append(receiptDate.substring(0, 4));
                            dateStrBuild.append(receiptDate.substring(6));
                            receiptDate = dateStrBuild.toString();
                        }
                        writeLogFile("Тест печати чека в учебном режиме. Дата на кассе: " + dateStr.get(0) + "Дата чека: " + receiptDate);

                        //делаем выборку из базы счетчиков
                        int[] countAfter = parseCount.parseCountValueFromStr(cashBoxConnect(sqlCommands.getCountersAdventValueCommand()).get(0));

                        //Подсчет общей суммы чека
                        //  int totalSum = totalReceiptSum(listScript);
                        String typePayStr = searchForKeyword("type_pay: ", listScript);
                        int typePay = -1;
                        if (typePayStr.equals("cash_pay"))
                            typePay = 0;
                        if (typePayStr.equals("card_pay"))
                            typePay = 1;

                        int assertCountRes = assertCount(countBegin, countAfter, 0, 10000, typePay);//totalSum);
                        if (assertCountRes == 0) {
                            assertEquals(dateStr.get(0), receiptDate);
                        } else {
                            fail("Неверное изменение в базе счетчиков, assertCount = " + assertCountRes);
                        }

                    } else {
                        fail("Чек не добавлен в базу receiptsDb.db");
                    }
                    break;
                }
                default:
                    break;
            }

        } else {
            fail("Касса не в учебном режиме");
        }
    }
    //ККТ в учебном режиме, продажа - расход, наличные, товар по свободной цене, итог 100 руб.
    @Test
    public void sale_consumption_learning_mode_cash() {
        //проверяем, что stage кассы = 0
        List<String> line = cashBoxConnect(sqlCommands.getStageCommand());
        if (line.get(0).equals("0")) {
            //проверяем, что открыт экран ввода пароля
            enterPasswordIfScreenOpen();

            line.clear();
            //делаем выборку их конфига на кассе, проверем, открыта смена или нет
            line = cashBoxConnect(sqlCommands.getOpenShiftCommand());
            if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
                openShift();
            }

            line.clear();
            //делаем выборку из базы счетчиков, проверяем что сумма в кассе > 10000 (коп)
            line = cashBoxConnect(sqlCommands.getCashInFinalCunterCommand());
            //если сумма меньше, то делаем внесение на 100 р
            if (Integer.parseInt(line.get(0)) < 10000) {
                int resInsertion = insertion(readDataScript("src\\test\\resourses\\insertion_100.txt"));
                if (resInsertion == 0) {
                    //делаем выборку из базы счетчиков, проверяем что сумма в кассе => 10000 (коп)
                    line.clear();
                    sleepMiliSecond(2000);
                    line = cashBoxConnect(sqlCommands.getCashInFinalCunterCommand());
                    System.out.println("getCashInFinalCunterCommand = " + line.get(0));
                    if (Integer.parseInt(line.get(0)) < 10000) {
                        fail("Сумма в кассе меньше 100 р.");
                    }
                } else {
                    fail("Ошибка выполнения внесения. Функция вернула " + resInsertion);
                }
            }

            //делаем выборку из базы чеков
            line.clear();
            line = cashBoxConnect(sqlCommands.getRecieptCountCommand());
            int countCheck = Integer.parseInt(line.get(0));
            line.clear();

            //делаем выборку из базы счетчиков
            int [] countBegin = parseCount.parseCountValueFromStr(cashBoxConnect(sqlCommands.getCountersAdventValueCommand()).get(0));

            //читаем из файла сценарий пробития чека
            List<String> listScript = readDataScript("src\\test\\resourses\\free_price_total_100_cash.txt");
            int testResult = checkPrintSaleConsumption(listScript, ScreenPicture.CONSUMTION_RESULT_SCREEN_100);
            switch (testResult) {
                case -1:
                    fail("Не открыт экран продажи (режим свободной цены)");
                    break;
                case -2:
                    fail("Не найдены товары, которые необходимо добавить в чек");
                    break;
                case -3:
                    fail("Не найдены товары, которые необходимо добавить в чек в методе печати чека");
                    break;
                case -4:
                    fail("Не указан способ добавления товара в чек");
                    break;
                case -5:
                    fail("Не указано количество товара во входном файле сценария");
                    break;
                case -6:
                    fail("Не указан способ оплаты во входном файле сценария");
                    break;
                case -7:
                    fail("Не указан тип товара во входном файле сценария");
                    break;
                case -8:
                    fail("Не совпадает дисплей кассы с ожидаемым экраном сдачи");
                    break;
                case 0: {
                    line = cashBoxConnect(sqlCommands.getRecieptCountCommand());
                    int countCheckTest = Integer.parseInt(line.get(0));
                    if ((countCheckTest - countCheck) == 1) {
                        //сделать выборку из базы чеков, по дате
                        String getCheckDateCommand = "echo \"attach '/FisGo/receiptsDb.db' as receipts; " +
                                "select RECEIPT_CREATE_DATE from receipts.RECEIPTS ORDER BY ID DESC limit 1;\" | sqlite3 /FisGo/receiptsDb.db\n";
                        line = cashBoxConnect(getCheckDateCommand);
                        String receiptDate = line.get(0);
                        if (receiptDate.length() == 12) {
                            StringBuilder dateStrBuild = new StringBuilder();
                            dateStrBuild.append(receiptDate.substring(0, 4));
                            dateStrBuild.append(receiptDate.substring(6));
                            receiptDate = dateStrBuild.toString();
                        }
                        writeLogFile("Тест печати чека в учебном режиме. Дата на кассе: " + dateStr.get(0) + "Дата чека: " + receiptDate);

                        //делаем выборку из базы счетчиков
                        int[] countAfter = parseCount.parseCountValueFromStr(cashBoxConnect(sqlCommands.getCountersAdventValueCommand()).get(0));

                        //Подсчет общей суммы чека
                        //  int totalSum = totalReceiptSum(listScript);
                        String typePayStr = searchForKeyword("type_pay: ", listScript);
                        int typePay = -1;
                        if (typePayStr.equals("cash_pay"))
                            typePay = 0;
                        if (typePayStr.equals("card_pay"))
                            typePay = 1;

                        int assertCountRes = assertCount(countBegin, countAfter, 1, 10000, typePay);//totalSum);
                        if (assertCountRes == 0) {
                            assertEquals(dateStr.get(0), receiptDate);
                        } else {
                            fail("Неверное изменение в базе счетчиков, assertCount = " + assertCountRes);
                        }

                    } else {
                        fail("Чек не добавлен в базу receiptsDb.db");
                    }
                    break;
                }
                default:
                    break;
            }
        } else {
            fail("Касса не в учебном режиме");
        }
    }
    //------------------------------------------------------------------------------------/

    //--------------------------------Тесты на открытие смены-----------------------------/
    //---------------------------------ККТ в учебном режиме-------------------------------/
    @Test
    public void open_shift_training_mode() {
        List<String> line = cashBoxConnect(sqlCommands.getStageCommand());
        if (line.get(0).equals("0")) {
            //проверяем, что открыт экран ввода пароля
            enterPasswordIfScreenOpen();

            line.clear();
            //делаем выборку их конфига на кассе, проверем, открыта смена или нет
            line = cashBoxConnect(sqlCommands.getOpenShiftCommand());
            if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
                int testResult = openShift();
                switch (testResult) {
                    case -1:
                        fail("Смена уже открыта");
                        break;
                    case -2:
                        fail("Пункт открытия смены не доступен");
                        break;
                    case -3:
                        fail("После открытия смены не открыт экран продажи по свободной цене");
                        break;
                    case 0:
                        line.clear();
                        line = cashBoxConnect(sqlCommands.getOpenShiftCommand());
                        if (Integer.parseInt(line.get(1)) == 1) {
                            //делаем выборку даты с кассы
                            assertEquals(dateStr.get(0), line.get(0));
                        } else {
                            fail("Поле SHIFT_TIMER в конфиге OFF");
                        }
                        break;
                    default:
                        break;
                }
            }
            else {
                fail("Смена уже открыта");
            }
        } else {
            fail("Касса не в учебном режиме");
        }
    }
    //------------------------------------------------------------------------------------/


    //--------------------------------Тесты на закрытие смены-----------------------------/
    //-------------------------ККТ в учебном режиме, смена открыта------------------------/
    @Test
    public void close_shift_training_mode() throws IOException {
        List<String> line = cashBoxConnect(sqlCommands.getStageCommand());
        if (line.get(0).equals("0")) {
            //проверяем, что открыт экран ввода пароля
            enterPasswordIfScreenOpen();
            line.clear();
            //делаем выборку их конфига на кассе, проверем, открыта смена или нет
            line = cashBoxConnect(sqlCommands.getOpenShiftCommand());
            //открываем смену, если она закрыта
            if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
                int openShiftInt = openShift();
                if (openShiftInt != 0)
                    fail("Ошибка при откытии смены, результат выполнения " + openShiftInt);
            }
            int testResult = closeShift();
            switch (testResult) {
                case -1:
                    fail("Смена закрыта");
                    break;
                case -2:
                    fail("Пункт закрытия смены не доступен");
                    break;
                case -3:
                    fail("Экран после закрытия смены не совпадает с ожидаемым (нет пункта открытия смены)");
                    break;
                case  -4:
                    fail("Поля OPEN_SHIFT_DATE и SHIFT_TIMER не пустые в конфиге");
                    break;
                case 0: {
                    line.clear();
                    line = cashBoxConnect(sqlCommands.getOpenShiftCommand());
                    if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
                        String strFromFile = br.readLine();
                        assertEquals(strFromFile, screens.openShiftMenuScreen);
                    } else {
                        fail("Поля OPEN_SHIFT_DATE и SHIFT_TIMER не пустые в конфиге");
                    }
                    break;
                }
                default:
                    fail("Неизвестное значение");
                    break;
            }
        } else {
            fail("Касса не в учебном режиме");
        }
    }
    //------------------------------------------------------------------------------------/




    //----------------------------------Печать Х-отчета-----------------------------------/
    */
/*FIXME условия прохождения теста - ??? *//*

 */
/*   @Test
    public void print_x_count() {
        //проверяем, что stage кассы = 0
        String getStageCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                "select STAGE from config.CONFIG;\"" +
                " | sqlite3 /FisGo/configDb.db\n";
        List<String> line = cashBoxConnect(getStageCommand);
        if (line.get(0).equals("0")) {
            //проверяем, что открыт экран ввода пароля
            boolean compare = compareScreen(ScreenPicture.PASSWORD);
            //если полученный экран с кассы совпадает с экраном ввода пароля, то выполняем if
            if (compare) {
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                String getPassCommand = "echo \"attach '/FisGo/usersDb.db' as users; " +
                        "select PASS from users.USERS limit 1;\" | sqlite3 /FisGo/usersDb.db\n";
                line = cashBoxConnect(getPassCommand);
                //вводим пароль на кассе
                strToKeypadConvert(line.get(0));
            }

            line.clear();
            //делаем выборку их конфига на кассе, проверем, открыта смена или нет
            String getOpenShiftCommand = "echo \"attach '/FisGo/configDb.db' as config; " +
                    "select OPEN_SHIFT_DATE from config.CONFIG;\" | sqlite3 /FisGo/configDb.db\n";
            line = cashBoxConnect(getOpenShiftCommand);
            if (line.isEmpty()) {
                openShift();
            }

            line.clear();

            String insertCounts = "echo \"attach '/FisGo/countersDb.db' as counters;\n" +
                    "update counters.COUNTERS set CASH_ON_TOP = 1, ADVENT = 2, ADVENT_CNT = 3, CONSUMPTION = 4,\n" +
                    "CONSUMPTION_CNT = 5, ADVENT_RETURN = 6, ADVENT_RETURN_CNT = 7, CONSUMPTION_RETURN = 8, CONSUMPTION_RETURN_CNT = 9,\n" +
                    "INSERTION = 10, INSERTION_CNT = 11, RESERVE = 12, RESERVE_CNT = 13, CASH_IN_FINAL = 14, ADVENT_CARD = 15,\n" +
                    "ADVENT_CARD_CNT = 16, ADVENT_RETURN_CARD = 17, ADVENT_RETURN_CARD_CNT = 18, ADVENT_TOTAL = 19, CONSUMPTION_TOTAL = 20,\n" +
                    "ADVENT_RETURN_TOTAL = 21, CONSUMPTION_RETURN_TOTAL = 22, REALIZATION_TOTAL = 23, REPORT_CNT = 24, CASH = 25,\n" +
                    "CARD = 26, CASH_CNT = 27, CARD_CNT = 28, CONSUMPTION_CARD = 29, CONSUMPTION_CARD_CNT = 30, RET_CONSUMPTION_CARD = 31,\n" +
                    "RET_CONSUMPTION_CARD_CNT = 32, ADVENT_TOTAL_ABS = 33, CONSUMPTION_TOTAL_ABS = 34, ADVENT_RETURN_TOTAL_ABS = 35,\n" +
                    "CONSUMPTION_RETURN_TOTAL_ABS = 36, REALIZATION_TOTAL_ABS = 37, CURR_RECEIPT_NUM = 38, CURR_SHIFT_NUM = 39 WHERE ID = 1;\n" +
                    "\" | sqlite3 /FisGo/configDb.db";
            cashBoxConnect(insertCounts);
            //перезапускаем фискат
            tcpSocket.socketClose();

            //перезапускаем фискат
            cashBoxConnect("/sbin/reboot");
            sleepMiliSecond(25000);

            dataFromCashbox.initSession(CashboxIP, USERNAME, PORT, PASSWORD);
            //   tcpSocket.setReadAllInstruction(false);
            tcpSocket.setFlagReceiveScreen(true);//.setFlagReceiveScreen(true);
            tcpSocket.createSocket(CashboxIP, CashboxPort);

            //проверяем, что открыт экран ввода пароля
            compare = compareScreen(ScreenPicture.PASSWORD);
            //если полученный экран с кассы совпадает с экраном ввода пароля, то выполняем if
            if (compare) {
                //делаем выборку их БД users на кассе, получаем пароль одного из них
                String getPassCommand = "echo \"attach '/FisGo/usersDb.db' as users; " +
                        "select PASS from users.USERS limit 1;\" | sqlite3 /FisGo/usersDb.db\n";
                line = cashBoxConnect(getPassCommand);
                //вводим пароль на кассе
                strToKeypadConvert(line.get(0));
            }
            //setLogLevel(readDataScript("src\\test\\resourses\\set_log_level_debug.txt"));
            xCount();
            sleepMiliSecond(6000);
            //setLogLevel(readDataScript("src\\test\\resourses\\set_log_level_error.txt"));

        } else {
            fail("Касса не в учебном режиме");
        }
    }
    //-----------------------------------------------------------------------------------------------/
*//*









    //Смена данных юрлица
    private int re_registrationLegalEntity(List <String> keyWordArray) {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key5, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);

        boolean compare = compareScreen(ScreenPicture.RE_REGISTRATION_MENU);
        if (compare) {
            //делаем выборку их БД users на кассе, получаем пароль одного из них
            String regDataFromCashbox = "echo \"attach '/FisGo/configDb.db' as config; " +
                    "select ORGANIZATION_NAME from config.CONFIG;" +
                    "select CALCULATION_ADDRESS from config.CONFIG;" +
                    "select CALCULATION_PLACE from config.CONFIG;\"" +
                    " | sqlite3 /FisGo/configDb.db\n";
            List <String> line = cashBoxConnect(regDataFromCashbox);

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


    //Продажа, расход
    private int checkPrintSaleConsumption(List <String> keyWordArray, ScreenPicture screen) {
        System.out.println("in checkPrintSaleConsuption");
        sleepMiliSecond(1000);
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key3, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        sleepMiliSecond(1000);
        boolean compare = compareScreen(ScreenPicture.FREE_SALE_MODE);
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
                    //tcpSocket.setFlagPause(true, 15);
                    //sleepMiliSecond(15000);
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
        }
        else {
            writeLogFile("Не открыт экран продажи (режим свободной цены)");
            return -1;
        }
        return 1;
    }

    private int assertCount(int [] countBegin, int [] countAfter, int typeCheck, int totalSumReceipt, int typePay){
        //0 - Чек продажи, приход
        //1 - Чек продажи, расход
        //2 - Чек возврата, приход
        //3 - Чек возврата, расход
        //4 - Внесение
        //5 - Изъятие
        switch (typeCheck) {
            //Чек продажи, приход
            case 0: {
                boolean corectCount = false;
                //Наличными
                if (typePay == 0) {
                    for (int i = 0; i < countBegin.length; i++) {
                        if (countBegin[i] != countAfter[i]) {
                            if ((i == cashbox.ParseCount.ADVENT) || (i == cashbox.ParseCount.CASH_IN_FINAL) ||
                                    (i == cashbox.ParseCount.ADVENT_TOTAL) || (i == cashbox.ParseCount.REALIZATION_TOTAL) ||
                                    (i == cashbox.ParseCount.CASH) || (i == cashbox.ParseCount.ADVENT_TOTAL_ABS) ||
                                    (i == cashbox.ParseCount.REALIZATION_TOTAL_ABS)) {
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    corectCount = true;
                                } else {
                                    return -3;
                                }
                            } else {
                                if ((i == cashbox.ParseCount.ADVENT_CNT) || (i == cashbox.ParseCount.CASH_CNT) ||
                                        (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        corectCount = true;
                                    } else {
                                        return -4;
                                    }
                                } else {
                                    return -2;
                                }
                            }
                        }
                    }
                }
                //Электронными
                if (typePay == 1) {
                    for (int i = 0; i < countBegin.length; i++) {
                        if (countBegin[i] != countAfter[i]) {
                            if ((i == cashbox.ParseCount.ADVENT_CARD) || (i == cashbox.ParseCount.ADVENT_TOTAL) ||
                                    (i == cashbox.ParseCount.REALIZATION_TOTAL) || (i == cashbox.ParseCount.CARD) ||
                                    (i == cashbox.ParseCount.ADVENT_TOTAL_ABS) || (i == cashbox.ParseCount.REALIZATION_TOTAL_ABS)) {
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    corectCount = true;
                                } else {
                                    return -5;
                                }
                            } else {
                                if ((i == cashbox.ParseCount.ADVENT_CARD_CNT) || (i == cashbox.ParseCount.CARD_CNT) ||
                                        (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        corectCount = true;
                                    } else {
                                        return -7;
                                    }
                                } else {
                                    return -6;
                                }
                            }
                        }
                    }
                }
                if (corectCount)
                    return 0;
                break;
            }

            //Чек продажи, расход
            case 1: {
                System.out.println("CONSUMPTION");
                boolean corectCount = false;
                //Наличными
                if (typePay == 0) {
                    System.out.println("(typePay == 0)");
                    for (int i = 0; i < countBegin.length; i++) {
                        System.out.println("i = " + i);
                        if (countBegin[i] != countAfter[i]) {
                            System.out.println("countBegin[i] != countAfter[i]");
                            if ((i == cashbox.ParseCount.CONSUMPTION) || (i == cashbox.ParseCount.CASH_IN_FINAL) ||
                                    (i == cashbox.ParseCount.CONSUMPTION_TOTAL) || (i == cashbox.ParseCount.REALIZATION_TOTAL) ||
                                    (i == cashbox.ParseCount.CASH) || (i == cashbox.ParseCount.CONSUMPTION_TOTAL_ABS) ||
                                    (i == cashbox.ParseCount.REALIZATION_TOTAL_ABS)) {
                                System.out.println("CONSUMPTION, CASH_IN_FINAL, CONSUMPTION_TOTAL, REALIZATION_TOTAL, CASH, CONSUMPTION_TOTAL_ABS, REALIZATION_TOTAL_ABS");
                                System.out.println("countAfter[i] - countBegin[i] = " + (countAfter[i] - countBegin[i]));
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    System.out.println("totalSum");
                                    corectCount = true;
                                } else {
                                    return -9;
                                }
                            } else {
                                if ((i == cashbox.ParseCount.CONSUMPTION_CNT) || (i == cashbox.ParseCount.CASH_CNT) ||
                                        (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    System.out.println("CONSUMPTION_CNT, CASH_CNT, CURR_RECEIPT_NUM");
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        System.out.println("1");
                                        corectCount = true;
                                    } else {
                                        return -10;
                                    }
                                } else {
                                    return -8;
                                }
                            }
                        }
                    }

                }
                //Электронными
                if (typePay == 1) {
                    for (int i = 0; i < countBegin.length; i++) {
                        System.out.println("i = " + i);
                        if (countBegin[i] != countAfter[i]) {
                            System.out.println("countBegin[i] != countAfter[i]");
                            if ((i == cashbox.ParseCount.CONSUMPTION_CARD) || (i == cashbox.ParseCount.CONSUMPTION_TOTAL) ||
                                    (i == cashbox.ParseCount.REALIZATION_TOTAL) || (i == cashbox.ParseCount.CARD) ||
                                    (i == cashbox.ParseCount.CONSUMPTION_TOTAL_ABS) || (i == cashbox.ParseCount.REALIZATION_TOTAL_ABS)) {
                                System.out.println("CONSUMPTION_CARD, CONSUMPTION_TOTAL, REALIZATION_TOTAL, CARD, CONSUMPTION_TOTAL_ABS, REALIZATION_TOTAL_ABS");
                                System.out.println("countAfter[i] - countBegin[i] = " + (countAfter[i] - countBegin[i]));
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    System.out.println("totalSum");
                                    corectCount = true;
                                }
                                else {
                                    return -12;
                                }
                            }
                            else {
                                if ((i == cashbox.ParseCount.CONSUMPTION_CARD_CNT) || (i == cashbox.ParseCount.CARD_CNT) ||
                                        (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    System.out.println("CONSUMPTION_CARD_CNT, CARD_CNT, CURR_RECEIPT_NUM");
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        System.out.println("1");
                                        corectCount = true;
                                    } else {
                                        return -13;
                                    }
                                } else {
                                    return -11;
                                }
                            }
                        }
                    }
                }
                if (corectCount)
                    return 0;
                break;
            }
            //Чек возврата, приход
            case 2: {
                System.out.println("ADVENT_RETURN");
                boolean corectCount = false;
                //Наличными
                if (typePay == 0) {
                    System.out.println("(typePay == 0)");
                    for (int i = 0; i < countBegin.length; i++) {
                        System.out.println("i = " + i);
                        if (countBegin[i] != countAfter[i]) {
                            System.out.println("countBegin[i] != countAfter[i]");
                            if ((i == cashbox.ParseCount.ADVENT_RETURN) || (i == cashbox.ParseCount.CASH_IN_FINAL) ||
                                    (i == cashbox.ParseCount.ADVENT_RETURN_TOTAL) || (i == cashbox.ParseCount.REALIZATION_TOTAL) ||
                                    (i == cashbox.ParseCount.CASH) || (i == cashbox.ParseCount.ADVENT_RETURN_TOTAL_ABS) ||
                                    (i == cashbox.ParseCount.REALIZATION_TOTAL_ABS)) {
                                System.out.println("ADVENT_RETURN, CASH_IN_FINAL, ADVENT_RETURN_TOTAL, REALIZATION_TOTAL, CASH, CONSUMPTION_TOTAL_ABS, REALIZATION_TOTAL_ABS");
                                System.out.println("countAfter[i] - countBegin[i] = " + (countAfter[i] - countBegin[i]));
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    System.out.println("totalSum");
                                    corectCount = true;
                                }
                                else {
                                    return -15;
                                }
                            }
                            else {
                                if ((i == cashbox.ParseCount.ADVENT_RETURN_CNT) || (i == cashbox.ParseCount.CASH_CNT) ||
                                        (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    System.out.println("CONSUMPTION_CNT, CASH_CNT, CURR_RECEIPT_NUM");
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        System.out.println("1");
                                        corectCount = true;
                                    } else {
                                        return -16;
                                    }
                                } else {
                                    return -14;
                                }
                            }
                        }
                    }
                    if (corectCount)
                        return 0;
                }
                //Электронными
                if (typePay == 1) {
                    for (int i = 0; i < countBegin.length; i++) {
                        System.out.println("i = " + i);
                        if (countBegin[i] != countAfter[i]) {
                            System.out.println("countBegin[i] != countAfter[i]");
                            if ((i == cashbox.ParseCount.ADVENT_RETURN_CARD) || (i == cashbox.ParseCount.ADVENT_RETURN_TOTAL) ||
                                    (i == cashbox.ParseCount.REALIZATION_TOTAL) || (i == cashbox.ParseCount.CARD) ||
                                    (i == cashbox.ParseCount.ADVENT_RETURN_TOTAL_ABS) || (i == cashbox.ParseCount.REALIZATION_TOTAL_ABS)) {
                                System.out.println("CONSUMPTION_CARD, CONSUMPTION_TOTAL, REALIZATION_TOTAL, CARD, CONSUMPTION_TOTAL_ABS, REALIZATION_TOTAL_ABS");
                                System.out.println("countAfter[i] - countBegin[i] = " + (countAfter[i] - countBegin[i]));
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    System.out.println("totalSum");
                                    corectCount = true;
                                }
                                else {
                                    return -18;
                                }
                            }
                            else {
                                if ((i == cashbox.ParseCount.ADVENT_RETURN_CARD_CNT) || (i == cashbox.ParseCount.CARD_CNT) ||
                                        (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    System.out.println("CONSUMPTION_CARD_CNT, CARD_CNT, CURR_RECEIPT_NUM");
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        System.out.println("1");
                                        corectCount = true;
                                    } else {
                                        return -19;
                                    }
                                } else {
                                    return -17;
                                }
                            }
                        }
                    }
                    if (corectCount)
                        return 0;
                }
                break;
            }
            //Чек возврата, расход
            case 3: {
                System.out.println("CONSUMPTION_RETURN");
                boolean corectCount = false;
                //Наличными
                if (typePay == 0) {
                    System.out.println("(typePay == 0)");
                    for (int i = 0; i < countBegin.length; i++) {
                        System.out.println("i = " + i);
                        if (countBegin[i] != countAfter[i]) {
                            System.out.println("countBegin[i] != countAfter[i]");
                            if ((i == cashbox.ParseCount.CONSUMPTION_RETURN) || (i == cashbox.ParseCount.CASH_IN_FINAL) ||
                                    (i == cashbox.ParseCount.CONSUMPTION_RETURN_TOTAL) || (i == cashbox.ParseCount.REALIZATION_TOTAL) ||
                                    (i == cashbox.ParseCount.CASH) || (i == cashbox.ParseCount.CONSUMPTION_RETURN_TOTAL_ABS) ||
                                    (i == cashbox.ParseCount.REALIZATION_TOTAL_ABS)) {
                                System.out.println("ADVENT_RETURN, CASH_IN_FINAL, ADVENT_RETURN_TOTAL, REALIZATION_TOTAL, CASH, CONSUMPTION_TOTAL_ABS, REALIZATION_TOTAL_ABS");
                                System.out.println("countAfter[i] - countBegin[i] = " + (countAfter[i] - countBegin[i]));
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    System.out.println("totalSum");
                                    corectCount = true;
                                }
                                else {
                                    return -21;
                                }
                            }
                            else {
                                if ((i == cashbox.ParseCount.CONSUMPTION_RETURN_CNT) || (i == cashbox.ParseCount.CASH_CNT) ||
                                        (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    System.out.println("CONSUMPTION_CNT, CASH_CNT, CURR_RECEIPT_NUM");
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        System.out.println("1");
                                        corectCount = true;
                                    } else {
                                        return -22;
                                    }
                                } else {
                                    return -20;
                                }
                            }
                        }
                    }
                    if (corectCount)
                        return 0;
                }
                //Электронными
                if (typePay == 1) {
                    for (int i = 0; i < countBegin.length; i++) {
                        System.out.println("i = " + i);
                        if (countBegin[i] != countAfter[i]) {
                            System.out.println("countBegin[i] != countAfter[i]");
                            if ((i == cashbox.ParseCount.CONSUMPTION_RETURN_CARD) || (i == cashbox.ParseCount.CONSUMPTION_RETURN_TOTAL) ||
                                    (i == cashbox.ParseCount.REALIZATION_TOTAL) || (i == cashbox.ParseCount.CARD) ||
                                    (i == cashbox.ParseCount.CONSUMPTION_RETURN_TOTAL_ABS) || (i == cashbox.ParseCount.REALIZATION_TOTAL_ABS)) {
                                System.out.println("CONSUMPTION_CARD, CONSUMPTION_TOTAL, REALIZATION_TOTAL, CARD, CONSUMPTION_TOTAL_ABS, REALIZATION_TOTAL_ABS");
                                System.out.println("countAfter[i] - countBegin[i] = " + (countAfter[i] - countBegin[i]));
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    System.out.println("totalSum");
                                    corectCount = true;
                                }
                                else {
                                    return -23;
                                }
                            }
                            else {
                                if ((i == cashbox.ParseCount.CONSUMPTION_RETURN_CARD_CNT) || (i == cashbox.ParseCount.CARD_CNT) ||
                                        (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    System.out.println("CONSUMPTION_CARD_CNT, CARD_CNT, CURR_RECEIPT_NUM");
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        System.out.println("1");
                                        corectCount = true;
                                    } else {
                                        return -25;
                                    }
                                } else {
                                    return -24;
                                }
                            }
                        }
                    }
                    if (corectCount)
                        return 0;
                }
                break;
            }
            //Внесение
            case 4: {
                System.out.println("INSERTION");
                boolean corectCount = false;
                if (typePay == 0) {
                    for (int i = 0; i < countBegin.length; i++) {
                        System.out.println("i = " + i);
                        if (countBegin[i] != countAfter[i]) {
                            System.out.println("countBegin[i] != countAfter[i]");
                            if ((i == cashbox.ParseCount.INSERTION) || (i == cashbox.ParseCount.CASH_IN_FINAL)) {
                                System.out.println("INSERTION, CASH_IN_FINAL");
                                System.out.println("countAfter[i] - countBegin[i] = " + (countAfter[i] - countBegin[i]));
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    System.out.println("totalSum");
                                    corectCount = true;
                                }
                                else {
                                    return -27;
                                }
                            }
                            else {
                                if ((i == cashbox.ParseCount.INSERTION_CNT) || (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    System.out.println("INSERTION_CNT, CURR_RECEIPT_NUM");
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        System.out.println("1");
                                        corectCount = true;
                                    } else {
                                        return -29;
                                    }
                                } else {
                                    return -28;
                                }
                            }
                        }
                    }
                    if (corectCount)
                        return 0;
                } else {
                    return -26;
                }
                break;
            }
            //Изъятие
            case 5: {
                System.out.println("RESERVE");
                boolean corectCount = false;
                if (typePay == 0) {
                    for (int i = 0; i < countBegin.length; i++) {
                        System.out.println("i = " + i);
                        if (countBegin[i] != countAfter[i]) {
                            System.out.println("countBegin[i] != countAfter[i]");
                            if ((i == cashbox.ParseCount.RESERVE) || (i == cashbox.ParseCount.CASH_IN_FINAL)) {
                                System.out.println("RESERVE, CASH_IN_FINAL");
                                System.out.println("countAfter[i] - countBegin[i] = " + (countAfter[i] - countBegin[i]));
                                if (countAfter[i] - countBegin[i] == totalSumReceipt) {
                                    System.out.println("totalSum");
                                    corectCount = true;
                                }
                                else {
                                    return -30;
                                }
                            }
                            else {
                                if ((i == cashbox.ParseCount.RESERVE_CNT) || (i == cashbox.ParseCount.CURR_RECEIPT_NUM)) {
                                    System.out.println("RESERVE, CARD_CNT, CURR_RECEIPT_NUM");
                                    if (countAfter[i] - countBegin[i] == 1) {
                                        System.out.println("1");
                                        corectCount = true;
                                    } else {
                                        return -32;
                                    }
                                } else {
                                    return -31;
                                }
                            }
                        }
                    }
                    if (corectCount)
                        return 0;

                } else {
                    return -27;
                }
                break;
            }
            default:
                return -1;
        }
        return 1;
    }
    //Тех. обнуление
    public void techNull() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key4, 0, 1);
        pressKeyBot(keyEnum.key7, 0, 1);
        pressKeyBot(keyEnum.keyEnter, 0, 2);
        tcpSocket.sendPressKey();
        tcpSocket.setFlagPause(true, 17);
        sleepMiliSecond(20000);
    }
    //включить внешний банковский терминал
    private int externalTerninalTurnOn() {
        List<String> line = cashBoxConnect(sqlCommands.getTerminalModeCommand());
        if (line.get(0).equals("0")) {
            pressKeyBot(keyEnum.keyMenu, 0, 1);
            pressKeyBot(keyEnum.key5, 0, 2);
            pressKeyBot(keyEnum.key2, 0, 1);
            pressKeyBot(keyEnum.keyEnter, 0, 1);
        }
        line.clear();
        line = cashBoxConnect(sqlCommands.getTerminalModeCommand());
        if (line.get(0).equals("1")) {
            return 0;
        } else {
            return -1;
        }
    }
    //Печать Х-отчета
    private void xCount() {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key1, 0, 1);
        boolean compare = compareScreen(ScreenPicture.SHIFT_MENU_OPEN_SHIFT);
        if (compare) {
            pressKeyBot(keyEnum.key3, 0, 1);
            tcpSocket.setFlagPause(true, 4);
        }
        else {
            writeLogFile("X-отчет не доступен");
            return;
        }
    }

    //Добавление товаров на кассу
    //FIXME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!разобраться с русскими символами!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void addGoodsOnCash(){
        //вставляем товары из файла
        StringBuilder addGoodCmd = new StringBuilder();
        addGoodCmd.append("echo \"attach '/FisGo/goodsDb.db' as goods; ");
        List <String> insertsGoods = readDataScript("src\\test\\resourses\\insert_to_goodsDb.txt");
        for (String tmpStr: insertsGoods)
            addGoodCmd.append(tmpStr);
        addGoodCmd.append("\" | sqlite3 /FisGo/goodsDb.db\n");
        cashBoxConnect(addGoodCmd.toString());

        //получаем список ID и артикулов из базы goods для всавки hash в goods_code
        StringBuilder selectGoodIDCmd = new StringBuilder();
        selectGoodIDCmd.append("echo \"attach '/FisGo/goodsDb.db' as goods; ");
        for (int i = 0; i<100; i++) {
            selectGoodIDCmd.append("select ID, ARTICUL from goods.GOODS where ARTICUL = '" + (i + 1) + "';");
        }
        selectGoodIDCmd.append("\" | sqlite3 /FisGo/goodsDb.db\n");
        List<String> line = cashBoxConnect(selectGoodIDCmd.toString());

        //заполнение таблицы goods_code
        List<String> hashArticle = readDataScript("src\\test\\resourses\\hashTable.txt");
        for (String strLine: line) {
            String idLine, articleLine;
            String[] parts = (strLine).split("\\|");
            idLine = parts[0];
            articleLine = parts[1];

            String insertGoodCodeCmd = "echo \"attach '/FisGo/goodsDb.db' as goods; " +
                    "insert into goods.GOODS_CODE (GOODS_ID, HASH_VAL, TYPE) values (" + idLine + ", " +
                    hashArticle.get(Integer.parseInt(articleLine) - 1) + ", 2);" + "\" | sqlite3 /FisGo/goodsDb.db\n";

            cashBoxConnect(insertGoodCodeCmd);//.toString());
        }
    }
    //Устанавливаем уровень лога
    private void setLogLevel(List <String> dataList) {
        pressKeyBot(keyEnum.keyMenu, 0, 1);
        pressKeyBot(keyEnum.key4, 0, 2);
        pressKeyBot(keyEnum.key1, 0, 1);
        String logLevel = searchForKeyword("level_log: ", dataList);
        System.out.println("logLevel = " + logLevel);

        if (logLevel.equals("CANNOT FIND KEYWORD"))
            writeLogFile("Не установлен уровень лога");
        else {
            if (logLevel.equals("Ошибки"))
                pressKeyBot(keyEnum.key1, 0, 1);
            if (logLevel.equals("Предупреждения"))
                pressKeyBot(keyEnum.key2, 0, 1);
            if (logLevel.equals("Информация"))
                pressKeyBot(keyEnum.key3, 0, 1);
            if (logLevel.equals("Отладка"))
                pressKeyBot(keyEnum.key4, 0, 1);

            pressKeyBot(keyEnum.keyEnter, 0, 1);
        }
        tcpSocket.setFlagPause(true, 2);
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

    private int closeShift() {
        //делаем выборку их конфига на кассе, проверем, открыта смена или нет
        List <String> line = cashBoxConnect(sqlCommands.getOpenShiftCommand());
        if (!line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) != 0)) {
            pressKeyBot(keyEnum.keyMenu, 0, 1);
            pressKeyBot(keyEnum.key1, 0, 1);
            //проверка, что на экране есть пункт закрытия смены
            boolean compare = compareScreen(ScreenPicture.SHIFT_MENU_OPEN_SHIFT);
            if (compare) {
                pressKeyBot(keyEnum.key2, 0, 1);
                pressKeyBot(keyEnum.keyEnter, 0, 1);
                sleepMiliSecond(8000);
                tcpSocket.setFlagPause(true, 8);
                pressKeyBot(keyEnum.key1, 0, 1);
                compare = compareScreen(ScreenPicture.OPEN_SHIFT_MENU);
                if (compare) {
                    line.clear();
                    line = cashBoxConnect(sqlCommands.getOpenShiftCommand());
                    if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
                        return 0;
                    }
                    else {
                        writeLogFile("Смена не закрыта");
                        return -4;
                    }
                } else {
                    writeLogFile("Экран после закрытия смены не совпадает с ожидаемым");
                    return -3;
                }

            } else {
                writeLogFile("Пункт закрытия смены не доступен");
                return -2;
            }
        } else {
            writeLogFile("Смена закрыта");
            return -1;
        }
    }







    */
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
        pressKeyBot(keyEnum.keyEnter, 0, 2); //
    }

    */
/*

    private void currentStatusReportPrint() {
        pressKeyBot(keyEnum.keyMenu, 0,  1);
        pressKeyBot(keyEnum.key2, 0, 1);
        //проверить, что касса зарегистрирована
        pressKeyBot(keyEnum.key1, 0, 1);
        tcpSocket.setFlagPause(true, 5);
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

        private int totalReceiptSum(List <String> listScript) {
        int countGoods = 0;
        for (String tmpStr: listScript) {
            if (tmpStr.contains("Good "))
                countGoods++;
        }
        if (countGoods == 0)
            return -1;

        for (int i = 0; i < countGoods; i++) {

        }
        return 1;
    }






    *//*





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
*
    //Нажатие на кнопку (ввод данных) в зависимости от символа
    private void strToKeypadConvert(String str) {
       // tcpSocket.serverGetKepadMode();
        keypad_mode = keypadMode.FREE_MODE;//tcpSocket.getKeypadMode();

        Short keyNumPrew = 40, keyNum = 0, pressCount;
        boolean exit;

        keypad.Keypad[] keys = new keypad.Keypad[keypad.Keypad.keys_table_size];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new keypad.Keypad();
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

            for (int j = 0; j < keypad.Keypad.keys_table_size; j++) {
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
                            if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);
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
                            if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);
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
                            if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);
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
                 */
/*   for (int k = 0; k < keys[j].spec_sym_code.size(); k++) {
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
                        /*/
/*
                        if (charsetNumber == 42) {
                            keyNum = 38;
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    } *//*

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
                            if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }

                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount++;
                        //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                        if (keyNumPrew.equals(keyNum))
                            sleepMiliSecond(1500);
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
                            if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);
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
                            if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);
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
                            if (keyNumPrew.equals(keyNum))
                                sleepMiliSecond(1500);
                            pressKeyBot(keyNum, 0, pressCount);
                            exit = true;
                        }
                    }
                    if (charsetNumber == keys[j].key_number) {
                        keyNum = keys[j].key_code;
                        pressCount++;
                        //если 2 буквы в строке находятся на одной кнопке  делаем паузу
                        if (keyNumPrew.equals(keyNum))
                            sleepMiliSecond(1500);
                        pressKeyBot(keyNum, 0, pressCount);
                        exit = true;
                    }
                }
                keyNumPrew = keyNum;

                */
/* // Английские символы + цифры
                if (keypadMode == ENGLISH + NUMBERS)
                {
                } *//*

            }
        }
    }

    //нажатие на кнопку
    public void pressKeyBot(int keyNum, int keyNum2,  int pressCount) {


        for (int i = 0; i < pressCount; i++)
            tcpSocket.pressButton(keyNum, keyNum2, keypad.KeypadActionEnum.KEY_DOWN);
        //tcpSocket.sendPressKey(keyNum, keyNum2, 1);
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

    //Сравниваем экран на кассе с экраном из "базы"
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

                case NOT_ENOUGH_MONEY:
                    return strFromFile.equals(screens.notEnoughMoneyScreen);

                case FREE_SALE_MODE_CHANGE_400:
                    return strFromFile.equals(screens.freeSaleModeChange400Screen);

                case GIVE_CARD_AND_RECEIPT:
                    return strFromFile.equals(screens.giveCardAndReceiptScreen);

                case CONSUMTION_RESULT_SCREEN_100:
                    return strFromFile.equals(screens.consuptionReaultScreen_100Screen);
                default:
                    return false;
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Ошибка сравнения экранов");
            e.printStackTrace();
        }
        return false;
    }*

    //инициализация клавиатуры и режимов, в которых используются кнопки
    private void initKey(keypad.Keypad keys[]) {
        // keypad.Keypad[] keys = new keypad.Keypad[keypad.keys_table_size];
        //=======================================================================================================
        // KEY №0 - цифра 0 на клавиатуре
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[0].key_code = 35;
        else                     // Дримкас РФ
            keys[0].key_code = 0x18;
        // Доступ в режимах
        keys[0].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.SPEC_SYMBOLS);
        // Цифра
        keys[0].key_number = 0x30;
        // Специальные символы
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))         // НЕ(Антон, прочти это!) Дримкас Ф!
            keys[0].spec_sym_code.add( 0x20 );
        keys[0].spec_sym_code.add(0x40);
        keys[0].spec_sym_code.add(0x23);
        keys[0].spec_sym_code.add(0x24);
        keys[0].spec_sym_code.add(0x25);
        keys[0].spec_sym_code.add(0x26);
        keys[0].spec_sym_code.add(0x2A);
        //=======================================================================================================
        //=======================================================================================================
        // KEY №1 - цифра 1 на клавиатуре
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[1].key_code = 27;
        else                     // Дримкас РФ
            keys[1].key_code = 0x12;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[2].key_code = 28;
        else                     // Дримкас РФ
            keys[2].key_code = 0x13;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[3].key_code = 29;
        else                     // Дримкас РФ
            keys[3].key_code = 0x14;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[4].key_code = 19;
        else                     // Дримкас РФ
            keys[4].key_code = 0x0C;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[5].key_code = 20;
        else                     // Дримкас РФ
            keys[5].key_code = 0x0D;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[6].key_code = 21;
        else                     // Дримкас РФ
            keys[6].key_code = 0x0E;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[7].key_code = 11;
        else                     // Дримкас РФ
            keys[7].key_code = 0x06;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[8].key_code = 12;
        else                     // Дримкас РФ
            keys[8].key_code = 0x07;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[9].key_code = 13;
        else                     // Дримкас РФ
            keys[9].key_code = 0x08;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[16].key_code = 6;
        else
            keys[16].key_code = 0x0a;
        // Доступ в режимах
        keys[16].key_mode_available = (char) keypadMode.ACTION_MODE;
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF")) {
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
        }
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[21].key_code = 14;
        else                     // Дримкас РФ
            keys[21].key_code = 0x09;

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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF")) {
            //=======================================================================================================
            // KEY №18
            keys[25].key_code = 18;
            // Доступ в режимах
            keys[25].key_mode_available = (char) keypadMode.ACTION_MODE;
            //=======================================================================================================
            // KEY №22
            keys[26].key_code = 22;
        }
        else                     // Дримкас РФ
            //=======================================================================================================
            // KEY №21
            keys[26].key_code = 0x15;
        // Доступ в режимах
        keys[26].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №23
        keys[27].key_code = 23;
        // Доступ в режимах
        keys[27].key_mode_available = (char) keypadMode.ACTION_MODE;
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF")) {
            //=======================================================================================================
            // KEY
            keys[28].key_code = 24;
            // Доступ в режимах
            keys[28].key_mode_available = (char) keypadMode.ACTION_MODE;
        }
        //=======================================================================================================
        // KEY №25
        keys[29].key_code = 25;
        // Доступ в режимах
        keys[29].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №26
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[30].key_code = 26;
        else                     // Дримкас РФ
            keys[30].key_code = 27;
        // Доступ в режимах
        keys[30].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №30
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[31].key_code = 30;
        else                     // Дримкас РФ
            keys[31].key_code = 0x16;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[37].key_code = 37;
        else                     // Дримкас РФ
            keys[37].key_code = 0x1A;
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
        if (cashbox.Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[39].key_code = 39;
        else
            keys[39].key_code = 0x1c;
        // Доступ в режимах
        keys[39].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================*//*

    }

    //Запись в лог
    private void writeLogFile(String text) {
        try {
            String strToFile = curdate.getTime() + "    " + text + "\n";
            Files.write(Paths.get("log.txt"), strToFile.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //задержка, милисекунды
    public void sleepMiliSecond(int delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}*/
