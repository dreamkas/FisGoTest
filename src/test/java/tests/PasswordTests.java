package tests;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.CashBoxType;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.assertj.core.api.SoftAssertions;
import org.junit.*;
import steps.StepsPassword;

import static org.junit.Assert.assertEquals;

@Ignore
@Feature("Тесты на ввод пароля")
@DisplayName("Тестирование ввода пароля")
public class PasswordTests {

    private static CashBox cashBox;
    private static Bot bot;
    private static StepsPassword step;
    private SoftAssertions softly;

    @BeforeClass
    public static void init() {
        cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.242.111");
        bot = new Bot(cashBox);
        step = new StepsPassword(bot, cashBox);
    }

    @Step("Начальные установки")
    @Before
    public void beforeTest() {
        bot.start();
        softly = new SoftAssertions();
    }

    @After
    public void afterTest() {
        bot.stop();
    }

    @Test
    @DisplayName("Ввод правильного пароля")
    public void testInputPassword() {
        step.rebootCashbox();
        step.inputPassword();
        assertEquals(true, step.isSuccessfulEntryPassword());
    }

    @Test
    @DisplayName("Ввод неправильного пароля")
    public void testIncorrectPassword() {
        step.rebootCashbox();
        step.inputincorrectPassword();
        softly.assertThat(step.isSuccessfulEntryPassword()).as("Не корректно обработал неправильный пароль").isFalse();

        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        step.inputPassword();

        softly.assertAll();
    }

    @Test
    @DisplayName("Ввод пароля при смене пользователя")
    public void testPasswordWithChangeUser() {
        step.changeUser();
        step.inputPassword();
        assertEquals(true, step.isSuccessfulEntryPassword());
    }

    @Test
    @DisplayName("Ввод неправильного пароля при смене пользователя")
    public void testIncorrectPasswordWithChangeUser() {
        step.changeUser();
        step.inputincorrectPassword();
        softly.assertThat(step.isSuccessfulEntryPassword()).as("Не корректно обработал неправильный пароль").isFalse();

        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        step.inputPassword();

        softly.assertAll();
    }
}
