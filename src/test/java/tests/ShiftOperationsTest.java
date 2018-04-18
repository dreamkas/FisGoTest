package tests;

import cashbox.*;
import remoteAccess.DataFromCashbox;
import remoteAccess.SQLCommands;
import remoteAccess.TCPSocket;
import screens.Screens;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


import keypad.KeyEnum;

/**
 * Тестирование сменных операций
 * - Открытие смены
 * - Х-отчет
 * - Внесение
 * - Изъяьте
 * - Закрытие смены
 */

public class ShiftOperationsTest {

    private TCPSocket tcpSocket = new TCPSocket();
    private Screens screens = new Screens();
    private CashBox cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.242.116");
    private Bot bot = new Bot(cashBox);
    private SQLCommands sqlCommands = new SQLCommands();
    private DataFromCashbox dataFromCashbox = new DataFromCashbox();

    @BeforeClass
    public static void befoclass() {
        //создаем сокет
        //    tcpSocket.createSocket(cashbox.CashBox.CASHBOX_IP, cashbox.CashBox.CASHBOX_PORT);
        //инициализируем керпкки

    }

    @Before
    public void beforeTests() {
        tcpSocket.createSocket(cashBox.CASHBOX_IP, CashBox.CASHBOX_PORT);
        //проверяем, что stage кассы = 2
        if (!getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED))) {
            registration();
        }
        bot.trySleep(5000);
    }

    @Test
    public void openShiftTest() {
        // делаем выборку их конфига на кассе, проверем, открыта смена или нет
        // в зависимости от режима статус открытия смены проверяется по разным флагам
        // в учебном режиме и енвд используется флаг SHIFT_TIMER
        // для зареганной кассы выгружаем флаг из биоса IS_SHIFT_OPEN
        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);
        List<String> valueConfigList;

        if (getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED))) {
            //проверяем, что открыт экран ввода пароля
            bot.enterPasswordIfScreenOpen();

            int testResult = bot.openShift(CashboxStagesEnum.REGISTRED);
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
                    // line.add(cashbox.ConfigFieldsEnum.IS_SHIFT_OPEN);
                    valueConfigList = bot.cfgGetJson(line);
                    //  if (Integer.parseInt(valueConfigList.get(0)) == 1) {
                    //делаем выборку даты с кассы
                    //assertEquals(dateStr.get(0), line.get(0));
                    assertEquals(valueConfigList.get(0), String.valueOf(1));
                    //    } else {
                    //      fail("Поле SHIFT_TIMER в конфиге OFF");
                    //   }
                    break;
                default:
                    break;
            }
        }else {
            fail("Касса не зарегистрирована или закрыта смена");
        }

    }

    @After
    public void closeConn() {
        bot.pressKeyBot(cashBox.keyEnum.keyCancel,0, 2);
        bot.sendData();
        bot.closeSessionJson();
        tcpSocket.socketClose(bot.resultJson());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void insertionTest() {
        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);
        List<String> valueConfigList = bot.cfgGetJson(line);
        if (valueConfigList.get(0).equals("0")) {
            openShift(CashboxStagesEnum.REGISTRED);
        }

        valueConfigList.clear();
        valueConfigList = bot.cfgGetJson(line);

        if (getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED)) && (!valueConfigList.get(0).equals("0"))) {
            bot.enterPasswordIfScreenOpen();
            List<String> listScript = bot.readDataScript("src\\test\\resourses\\insertion_100.txt");
            int testResult = bot.insertion(listScript);
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
                default:
                    break;
            }
        }
    }
    /*
        @Test
        public void saleFromBaseTest() {

        }

        @Test
        public void refundSummaryTest() {

        }

        @Test
        public void refundFromBaseTest() {

        }
    */
    @Test
    public void reserveTest() throws IOException {
        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);
        List<String> valueConfigList = bot.cfgGetJson(line);
        if (valueConfigList.get(0).equals("0")) {
            openShift(CashboxStagesEnum.REGISTRED);
        }

        valueConfigList.clear();
        valueConfigList = bot.cfgGetJson(line);

        if (getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED)) && (!valueConfigList.get(0).equals("0"))) {
            bot.enterPasswordIfScreenOpen();
            List<String> listScript = bot.readDataScript("src\\test\\resourses\\reserve_100.txt");
            int testResult = bot.reserve(listScript);
            assertEquals(testResult, 0);

        }
    }

    /*@Test
    public void xCountTest() {

    }*/

    @AfterClass
    public static void afterTests() {
        // bot.sendData();
        // bot.closeSessionJson();
        //tcpSocket.socketClose(bot.resultJson());
    }


    // Получаем Stage кассы
    private List<String> getStage() {
        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.STAGE);
        return bot.cfgGetJson(line);
    }

    //открытие смены, для тестов ввода пароля при открытой смене
    private void openShift(int stage) {
        List<ConfigFieldsEnum> line = new ArrayList<>();
        if ((stage == CashboxStagesEnum.STUDY) || (stage == CashboxStagesEnum.ENVD))
            line.add(ConfigFieldsEnum.SHIFT_TIMER);
        if (stage == CashboxStagesEnum.REGISTRED)
            line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);

        List<String> valueConfigList = bot.cfgGetJson(line);
        if (valueConfigList.get(0).equals("0")) {
            bot.enterPasswordIfScreenOpen();
            int openShiftResult = bot.openShift(stage);
            System.out.println("openShiftResult = " + openShiftResult);
            if (openShiftResult != 0)
                fail("Ошибка при открытии смены");
                //если смена открыта, то перезапускаем кассу, чтобы попасть на экран авторизации
            else {
                //перезапускаем фискат
                // fiscatReboot();
                //открываем сокет заново
                //  tcpSocket.createSocket(cashbox.CashBox.CASHBOX_IP, cashbox.CashBox.CASHBOX_PORT);
            }
        }
    }

    //регистрация ккт, автономный режим
    private void registration() {
        bot.enterPasswordIfScreenOpen();
        List<String> listScript = bot.readDataScript("src\\test\\resourses\\registration_correct_autonomic.txt");
        int registrationResult = bot.registration(listScript);
        if (registrationResult != 0)
            fail("Ошибка при регистрации кассы");
            //если регистрация выполнена успешно, то перезапускаем кассу, чтобы попасть на экран авторизации
        else {
            //перезапускаем фискат
            fiscatReboot();
            //открываем сокет заново
            tcpSocket.createSocket(cashBox.CASHBOX_IP, CashBox.CASHBOX_PORT);
        }
    }

    private void fiscatReboot() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DataFromCashbox dataFromCashbox = new DataFromCashbox();
        dataFromCashbox.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        dataFromCashbox.executeListCommand("/sbin/reboot");
        dataFromCashbox.disconnectSession();

        try {
            Thread.sleep(90000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
