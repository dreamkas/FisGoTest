package tests;

import cashbox.*;
import remoteAccess.DataFromCashbox;
import remoteAccess.TCPSocket;
import screens.Screens;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import keypad.KeyEnum;

/**
 * Тесты на ввод пароля
 */

public class PasswordTest {

    private TCPSocket tcpSocket = new TCPSocket();
    private Screens screens = new Screens();
    private CashBox cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.242.116");
    private Bot bot = new Bot(cashBox);

    @Before
    public void setupConn() {
        //создаем сокет
        tcpSocket.createSocket(cashBox.CASHBOX_IP, CashBox.CASHBOX_PORT);
    }

    @After
    public void closeConn() {
        bot.sendData();
        bot.closeSessionJson();
        tcpSocket.socketClose(bot.resultJson());
    }

    //----------------------------ККТ в учебном режиме------------------------------/
    //Ввод некорректного пароля
    @Test
    public void incorrect_password_study_mode() throws IOException {
        //проверяем, что stage = 0
        if (getStage().get(0).equals(String.valueOf(CashboxStagesEnum.STUDY))) {
            List<String> listScript = bot.readDataScript("src\\test\\resourses\\passwd_1235.txt");
            int testResult = bot.enterPassword(listScript);
            switch (testResult) {
                case -1: {
                    FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                    String strFromFile = br.readLine();
                    assertEquals(strFromFile, screens.incorrectPasswodScreen);
                    break;
                }
                case -2:
                    fail("Не открыт экран ввода пароля.");
                    break;
                case 0:
                    fail("Введен верный пароль.");
                    break;
                default:
                    fail("Неизвестное значение");
                    break;
            }
        } else {
            fail("Касса после тех. обнуления касса не в учебном режиме");
        }
    }

    //Ввод корректного пароля, касса после тех. обнуления
    @Test
    public void correct_password_study_mode() throws IOException {
        //проверяем, что stage = 0
        if (getStage().get(0).equals(String.valueOf(CashboxStagesEnum.STUDY))) {
            //проверем, открыта смена или нет
            List<ConfigFieldsEnum> line = new ArrayList<>();
            line.add(ConfigFieldsEnum.SHIFT_TIMER);
            List<String> valueConfigList = bot.cfgGetJson(line);

            if (valueConfigList.get(0).equals("0")) {
                List<String> listScript = bot.readDataScript("src\\test\\resourses\\passwd_1234.txt");
                int testResult = bot.enterPassword(listScript);
                switch (testResult) {
                    case -1:
                        fail("Введен неверный пароль.");
                        break;
                    case -2:
                        fail("Не открыт экран ввода пароля.");
                        break;
                    case 0: {
                        FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
                        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                        String strFromFile = br.readLine();
                        assertEquals(screens.menuAfterPasswdScreen, strFromFile);
                        break;
                    }
                    default:
                        fail("Неизвестное значение");
                        break;
                }
            } else {
                fail("Касса после тех обнуления, но смена открыта");
            }
        } else {
            fail("Касса после тех. обнуления не в учебном режиме");
        }
    }

    //Ввод корректного пароля, на кассе открыта смена
    @Test
    public void correct_password_open_shift_study_mode() throws IOException {
        //проверяем, что stage = 0
        if (getStage().get(0).equals(String.valueOf(CashboxStagesEnum.STUDY))) {
            //пытаемся открыть смену, если будет ошибка тест завалиться в самой функции
            openShift(CashboxStagesEnum.STUDY);

            List<String> listScript = bot.readDataScript("src\\test\\resourses\\passwd_1234.txt");
            int testResult = bot.enterPassword(listScript);
            switch (testResult) {
                case -1:
                    fail("Введен неверный пароль.");
                    break;
                case -2:
                    fail("Не открыт экран ввода пароля.");
                    break;
                case 0: {
                    FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                    String strFromFile = br.readLine();
                    assertEquals(screens.freeSaleModeScreen, strFromFile);
                    break;
                }
                default:
                    fail("Неизвестное значение");
                    break;
            }
        }
    }

    //------------------------------------------------------------------------------/
    //----------------------------ККТ зарегистрирована------------------------------/
    //Ввод некорректного пароля
    @Test
    public void incorrect_password_regiistred_mode() throws IOException {
        //проверяем, что stage = 2
        if (!getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED))) {
            registration();
        }
        if (getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED))) {
            List<String> listScript = bot.readDataScript("src\\test\\resourses\\passwd_1235.txt");
            int testResult = bot.enterPassword(listScript);
            switch (testResult) {
                case -1: {
                    FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                    String strFromFile = br.readLine();
                    assertEquals(strFromFile, screens.incorrectPasswodScreen);
                    break;
                }
                case -2:
                    fail("Не открыт экран ввода пароля");
                    break;
                case 0:
                    fail("Введен верный пароль");
                    break;
                default:
                    fail("Неизвестное значение");
                    break;
            }
        } else {
            fail("Касса не зарегистрирована");
        }
    }

    @Test
    public void correct_password_regiistred_mode() throws IOException {
        //проверяем, что stage = 2
        if (!getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED))) {
            registration();
        }
        //проверем, открыта смена или нет
        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);
        List<String> valueConfigList = bot.cfgGetJson(line);

        if (valueConfigList.get(0).equals("0")) {
            List<String> listScript = bot.readDataScript("src\\test\\resourses\\passwd_1234.txt");
            int testResult = bot.enterPassword(listScript);
            switch (testResult) {
                case -1:
                    fail("Введен неверный пароль.");
                    break;
                case -2:
                    fail("Не открыт экран ввода пароля.");
                    break;
                case 0: {
                    FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                    String strFromFile = br.readLine();
                    assertEquals(screens.mainMenuRegistredModeScreen, strFromFile);
                    break;
                }
                default:
                    fail("Неизвестное значение");
                    break;
            }
        } else {
            fail("Смена открыта");
        }
    }

    //Ввод корректного пароля, на зарегистрированной кассе, смена открыта
    @Test
    public void correct_password_open_shift_regiistred_mode() throws IOException {
        //проверяем, что stage = 2
        if (!getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED))) {
            registration();
        }
        if (getStage().get(0).equals(String.valueOf(CashboxStagesEnum.REGISTRED))) {
            //пытаемся открыть смену, если будет ошибка тест завалиться в самой функции
            openShift(CashboxStagesEnum.REGISTRED);

            List<String> listScript = bot.readDataScript("src\\test\\resourses\\passwd_1234.txt");
            int testResult = bot.enterPassword(listScript);
            switch (testResult) {
                case -1:
                    fail("Введен неверный пароль.");
                    break;
                case -2:
                    fail("Не открыт экран ввода пароля.");
                    break;
                case 0: {
                    FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                    String strFromFile = br.readLine();
                    assertEquals(screens.freeSaleModeScreen, strFromFile);
                    break;
                }
                default:
                    fail("Неизвестное значение");
                    break;
            }
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
            if (openShiftResult != 0)
                fail("Ошибка при открытии смены");
                //если смена открыта, то перезапускаем кассу, чтобы попасть на экран авторизации
            else {
                //перезапускаем фискат
                fiscatReboot();
                //открываем сокет заново
                tcpSocket.createSocket(cashBox.CASHBOX_IP, CashBox.CASHBOX_PORT);
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
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DataFromCashbox dataFromCashbox = new DataFromCashbox();
        dataFromCashbox.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        dataFromCashbox.executeListCommand("/sbin/reboot");
        dataFromCashbox.disconnectSession();

        try {
            Thread.sleep(70000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
