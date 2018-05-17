package tests;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.CashBoxType;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.junit.*;
import steps.StepsPassword;

import static org.junit.Assert.assertEquals;

@Ignore
public class PasswordTests {

    private static CashBox cashBox;
    private static Bot bot;
    private static StepsPassword step;

    @BeforeClass
    public static void init() {
        cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.242.132");
        bot = new Bot(cashBox);
        step = new StepsPassword(bot, cashBox);
    }

    @Step("Начальные установки")
    @Before
    public void beforeTest() {
        bot.start();
    }

    @After
    public void afterTest() {
        bot.stop();
    }

    @Test
    @DisplayName("Ввод правильного пароля")
    public void testInputPassword() {
        step.rebootCashbox();
        assertEquals(true, step.isSuccessfulEntryPassword());
    }

    @Test
    @DisplayName("Ввод неправильного пароля")
    public void testIncorrectPassword() {
        step.rebootCashbox();
        step.inputincorrectPassword();
        assertEquals(false, step.isSuccessfulEntryPassword());
    }

    @Test
    @DisplayName("Ввод пароля при смене пользователя")
    public void testPasswordWithChangeUser(){
        step.changeUser();
        step.inputPassword();
        assertEquals(true, step.isSuccessfulEntryPassword());
    }

    @Test
    @DisplayName("Ввод пароля при смене пользователя")
    public void testIncorrectPasswordWithChangeUser(){
        step.changeUser();
        step.inputincorrectPassword();
        assertEquals(false, step.isSuccessfulEntryPassword());
    }

}
