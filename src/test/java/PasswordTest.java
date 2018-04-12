import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Тесты на ввод пароля
 */

public class PasswordTest {

    private TCPSocket tcpSocket = new TCPSocket();
    private Bot bot = new Bot();
    private KeyEnum keyEnum = new KeyEnum();
    private Screens screens = new Screens();

    @Before
    public void setupConn () {
        //создаем сокет
        tcpSocket.createSocket(Config.CASHBOX_IP, Config.CASHBOX_PORT);
        //инициализируем керпкки
        keyEnum.initKeyEnum();
    }

    @After
    public void closeConn () {
        bot.pressKeyBot(keyEnum.keyCancel, 0, 1);//pressButton(keyEnum.keyCancel, 0, KeypadActionEnum.KEY_DOWN);
        tcpSocket.sendDataToSocket(bot.getTaskId(), bot.resultJson());
        bot.closeSessionJson();
        tcpSocket.socketClose(bot.resultJson());
    }

    //----------------------------ККТ в учебном режиме------------------------------/
    //Ввод некорректного пароля
    @Test
    public void incorrect_password() throws IOException {
        //проверяем, что stage = 0
        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.STAGE);
        List<String> valueConfigList = bot.cfgGetJson(line);

        if (valueConfigList.get(0).equals("0")) {
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
        } else  {
            fail("Касса после тех. обнуления касса не в учебном режиме");
        }
    }

    //Ввод корректного пароля, касса после тех. обнуления
    @Test
    public void correct_password() throws IOException {
        //проверяем, что stage = 0
        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.STAGE);
        List<String> valueConfigList = bot.cfgGetJson(line);

        if (valueConfigList.get(0).equals("0")) {
            //проверем, открыта смена или нет
            line.clear();
            valueConfigList.clear();
            line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);
            line.add(ConfigFieldsEnum.SHIFT_TIMER);
            valueConfigList = bot.cfgGetJson(line);

            if (valueConfigList.get(0).equals("0") && (Integer.parseInt(valueConfigList.get(1)) == 0)) {
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
        } else  {
            fail("Касса после тех. обнуления не в учебном режиме");
        }
    }

    //Ввод корректного пароля, на кассе открыта смена
    @Test
    public void correct_password_open_shift()  throws IOException {
        //проверяем, что stage = 0
        List<ConfigFieldsEnum> line = new ArrayList<>();
        line.add(ConfigFieldsEnum.STAGE);
        List<String> valueConfigList = bot.cfgGetJson(line);

        if (valueConfigList.get(0).equals("0")) {
            openShift();
            line.clear();
            //проверем, открыта смена или нет
            line.clear();
            valueConfigList.clear();
            line.add(ConfigFieldsEnum.IS_SHIFT_OPEN);
            line.add(ConfigFieldsEnum.SHIFT_TIMER);
            valueConfigList = bot.cfgGetJson(line);

            if (valueConfigList.get(0).equals("0") && (Integer.parseInt(valueConfigList.get(1)) == 0)) {
                fail("Смена закрыта");
            } else {
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
        } else  {
            fail("Касса после тех. обнуления не в учебном режиме");
        }
    }
    //------------------------------------------------------------------------------/

    //открытие смены, для тестов ввода пароля при открытой смене
    private void openShift() {
        bot.enterPasswordIfScreenOpen();
        int openShiftResult = bot.openShift();
        if (openShiftResult != 0)
            fail("Ошибка при открытии смены");
        //если смена открыта, то перезапускаем кассу, чтобы попасть на экран авторизации
        else {
            //перезапускаем фискат
            DataFromCashbox dataFromCashbox = new DataFromCashbox();
            dataFromCashbox.executeListCommand("/sbin/reboot");

            try {
                Thread.sleep(25000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tcpSocket.createSocket(Config.CASHBOX_IP, Config.CASHBOX_PORT);
        }

    }

}
