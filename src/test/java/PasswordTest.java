import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Тесты на ввод пароля
 */
public class PasswordTest {

    public PasswordTest() throws FileNotFoundException {
    }

    private CashTest cashTest = new CashTest();
    private TCPSocket tcpSocket = new TCPSocket();
    private DataFromCashbox dataFromCashbox = new DataFromCashbox();
    private Screens screens = new Screens();
    private SQLCommands sqlCommands = new SQLCommands();
    private KeyEnum keyEnum = new KeyEnum();

    @Before
    public void before_test_clear_cashbox() {
        cashTest.initializationKeyboard();
        cashTest.connectionSetup();
        //проверяем, что открыт экран ввода пароля
        cashTest.enterPasswordIfScreenOpen();
        //делаем тех. обнуление на кассе
        cashTest.techNull();
        tcpSocket.socketClose();
        //перезапускаем фискат
        cashTest.cashBoxConnect("/sbin/reboot");
        cashTest.sleepMiliSecond(25000);
        tcpSocket.setFlagPause(true, 25);
        cashTest.connectionSetup();
        tcpSocket.setFlagPause(false, 0);
    }

    @After
    public void after_test_clear_cashbox() {
        cashTest.pressKeyBot(keyEnum.keyCancel, 0, 1);
        dataFromCashbox.disconnectSession();
        tcpSocket.socketClose();
    }

    //----------------------------ККТ в учебном режиме------------------------------/
    //Ввод некорректного пароля
    @Test
    public void incorrect_password() throws IOException {
        //проверяем, что stage = 0
        List<String> line = cashTest.cashBoxConnect(sqlCommands.getStageCommand());
        if (line.get(0).equals("0")) {
            List<String> listScript = cashTest.readDataScript("src\\test\\resourses\\passwd_1235.txt");
            int testResult = cashTest.enterPassword(listScript);
            switch (testResult) {
                case -1: {
                    String strFromFile = cashTest.br.readLine();
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
            fail("Касса после тех. обнуления не в учебном режиме");
        }
    }

    //Ввод корректного пароля, касса после тех. обнуления
    @Test
    public void correct_password() throws IOException {
        //проверяем, что stage = 0
        List<String> line = cashTest.cashBoxConnect(sqlCommands.getStageCommand());
        if (line.get(0).equals("0")) {
            line.clear();
            //делаем выборку из конфига на кассе, проверем, открыта смена или нет
            line = cashTest.cashBoxConnect(sqlCommands.getOpenShiftCommand());
            if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
                List<String> listScript = cashTest.readDataScript("src\\test\\resourses\\passwd_1234.txt");
                int testResult = cashTest.enterPassword(listScript);
                switch (testResult) {
                    case -1:
                        fail("Введен неверный пароль.");
                        break;
                    case -2:
                        fail("Не открыт экран ввода пароля.");
                        break;
                    case 0: {
                        String strFromFile = cashTest.br.readLine();
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
    public void correct_password_open_shift() throws IOException {
        //проверяем, что stage = 0
        List<String> line = cashTest.cashBoxConnect(sqlCommands.getStageCommand());
        if (line.get(0).equals("0")) {
            openShift();
            line.clear();
            //делаем выборку из конфига на кассе, проверем, открыта смена или нет
            line = cashTest.cashBoxConnect(sqlCommands.getOpenShiftCommand());
            if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
                fail("Смена закрыта");
            } else {
                List<String> listScript = cashTest.readDataScript("src\\test\\resourses\\passwd_1234.txt");
                int testResult = cashTest.enterPassword(listScript);
                switch (testResult) {
                    case -1:
                        fail("Введен неверный пароль.");
                        break;
                    case -2:
                        fail("Не открыт экран ввода пароля.");
                        break;
                    case 0: {
                        String strFromFile = cashTest.br.readLine();
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
        List<String> line = cashTest.cashBoxConnect(sqlCommands.getOpenShiftCommand());
        if (line.get(0).isEmpty() && (Integer.parseInt(line.get(1)) == 0)) {
            cashTest.enterPasswordIfScreenOpen();
            int openShiftResult = cashTest.openShift();
            System.out.println("openShiftResult = " + openShiftResult);
            if (openShiftResult != 0)
                fail("Ошибка при открытии смены");
                //если смена открыта, то перезапускаем кассу, чтобы попасть на экран авторизации
            else {
                //перезапускаем фискат
                tcpSocket.setFlagPause(false, 0);
                tcpSocket.socketClose();
                cashTest.cashBoxConnect("/sbin/reboot");
                cashTest.sleepMiliSecond(25000);
                tcpSocket.setFlagPause(true, 25);
                dataFromCashbox.initSession(CashTest.CashboxIP, CashTest.USERNAME, CashTest.PORT, CashTest.PASSWORD);
                tcpSocket.setFlagReceiveScreen(true);
                tcpSocket.createSocket(CashTest.CashboxIP, CashTest.CashboxPort);
                tcpSocket.setFlagPause(false, 0);
            }
        }
    }
}