package tests;

import org.junit.*;
import remoteAccess.DataFromCashbox;
import remoteAccess.SQLCommands;
import remoteAccess.TCPSocket;
import screens.ScreenPicture;
import screens.Screens;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import cashbox.Bot;
import keypad.KeyEnum;
import cashbox.Config;
import cashbox.CashboxStagesEnum;
import cashbox.ConfigFieldsEnum;

/**
 * Тесты на продажу
 */
public class SalesTest {
    private static TCPSocket tcpSocket = new TCPSocket();
    private Bot bot = new Bot();
    private static KeyEnum keyEnum = new KeyEnum();
    private Screens screens = new Screens();
    private SQLCommands sqlCommands = new SQLCommands();
    private DataFromCashbox dataFromCashbox = new DataFromCashbox();
    @BeforeClass
    public static void befoclass() {
        //создаем сокет
        //    tcpSocket.createSocket(cashbox.Config.CASHBOX_IP, cashbox.Config.CASHBOX_PORT);
        //инициализируем керпкки
        keyEnum.initKeyEnum();
    }

    @Before
    public void beforeTests() {
        tcpSocket.createSocket(Config.CASHBOX_IP, Config.CASHBOX_PORT);
        //проверяем, что stage кассы = 2
        if (!getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED))) {
            registration();
        }
        bot.trySleep(5000);
    }

    //Тест на продажу (приход), касса зарегистрирована, продажа по суммовому режиму
    @Test
    public void sale_summary_advent_registred_mode_cash_test() {

        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);
        List<String> valueConfigList = bot.cfgGetJson(line);
        if (valueConfigList.get(0).equals("0")) {
            openShift(CashboxStagesEnum.REGISTRED);
        }

        valueConfigList.clear();
        valueConfigList = bot.cfgGetJson(line);

        if (getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED)) && (!valueConfigList.get(0).equals("0"))) {
            System.out.println("tut");
            bot.enterPasswordIfScreenOpen();
            //делаем выборку из базы чеков
            dataFromCashbox.initSession(Config.CASHBOX_IP, Config.USERNAME, Config.PORT, Config.PASSWORD);
            List<String> getCheckCount = dataFromCashbox.executeListCommand(sqlCommands.getRecieptCountCommand());

            System.out.println("sqlCommands.getRecieptCountCommand() = " + sqlCommands.getRecieptCountCommand());

            int countCheckBeforeTest = Integer.parseInt(getCheckCount.get(0));
            getCheckCount.clear();

            //делаем выборку из базы счетчиков
            //int [] countBegin = parseCount.parseCountValueFromStr(cashBoxConnect(sqlCommands.getCountersAdventValueCommand()).get(0));

            //читаем из файла сценарий пробития чека
            List<String> listScript = bot.readDataScript("src\\test\\resourses\\free_price_total_100_cash.txt");
            int testResult = bot.checkPrintSaleComming(listScript, ScreenPicture.FREE_SALE_MODE_CHANGE_400);
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
                    getCheckCount = dataFromCashbox.executeListCommand(sqlCommands.getRecieptCountCommand());
                    int countCheckTest = Integer.parseInt(getCheckCount.get(0));

                    if ((countCheckTest - countCheckBeforeTest) == 1) {
                        //сделать выборку из базы чеков, по дате
                        String getCheckDateCommand = "echo \"attach '/FisGo/receiptsDb.db' as receipts; " +
                                "select RECEIPT_CREATE_DATE from receipts.RECEIPTS ORDER BY ID DESC limit 1;\" | sqlite3 /FisGo/receiptsDb.db\n";
                        List <String> getCheckDate = dataFromCashbox.executeListCommand(getCheckDateCommand);
                        dataFromCashbox.disconnectSession();

                        String receiptDate = getCheckDate.get(0);
                        if (receiptDate.length() == 12) {
                            StringBuilder dateStrBuild = new StringBuilder();
                            dateStrBuild.append(receiptDate.substring(0, 4));
                            dateStrBuild.append(receiptDate.substring(6));
                            receiptDate = dateStrBuild.toString();
                        }
                        //     bot.writeLogFile("Тест печати чека в учебном режиме. Дата на кассе: " + dateStr.get(0) + "Дата чека: " + receiptDate);

                        //делаем выборку из базы счетчиков
                        //int[] countAfter = parseCount.parseCountValueFromStr(cashBoxConnect(sqlCommands.getCountersAdventValueCommand()).get(0));

                        //Подсчет общей суммы чека
                        //  int totalSum = totalReceiptSum(listScript);
                        String typePayStr = bot.searchForKeyword("type_pay: ", listScript);
                        int typePay = -1;
                        if (typePayStr.equals("cash_pay"))
                            typePay = 0;
                        if (typePayStr.equals("card_pay"))
                            typePay = 1;

                        // int assertCountRes = assertCount(countBegin, countAfter, 0, 10000, typePay);//totalSum);
                        // if (assertCountRes == 0) {
                        Assert.assertEquals(bot.dateStr.get(0), receiptDate);
                        /*} else {
                            fail("Неверное изменение в базе счетчиков, assertCount = " + assertCountRes);
                        }*/
                    } else {
                        fail("Чек не добавлен в базу receiptsDb.db");
                    }
                    break;
                }
                default:
                    break;
            }
        } else {
            fail("Касса не зарегистрирована или закрыта смена");
        }
    }

    @After
    public void closeConn() {
        bot.pressKeyBot(keyEnum.keyCancel,0, 2);
        bot.sendData();
        bot.closeSessionJson();
        tcpSocket.socketClose(bot.resultJson());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            if (openShiftResult != 0) {
                fail("Ошибка при открытии смены");
            }
        }
    }

    //регистрация ккт, автономный режим
    private void registration() {
        bot.enterPasswordIfScreenOpen();
        List<String> listScript = bot.readDataScript("src\\test\\resourses\\registration_correct_autonomic.txt");
        int registrationResult = bot.registration(listScript);
        if (registrationResult != 0) {
            fail("Ошибка при регистрации кассы");
        }
    }
}
