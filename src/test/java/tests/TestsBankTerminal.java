package tests;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.CashBoxType;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import steps.StepsBankTerminal;

import static org.assertj.core.api.Assertions.assertThat;

public class TestsBankTerminal {

    private static CashBox cashBox;
    private static Bot bot;

    private static StepsBankTerminal step;

    private SoftAssertions softly;

    @BeforeClass
    public static void init() {
        cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.243.4");
        bot = new Bot(cashBox);
        step = new StepsBankTerminal(bot, cashBox);

    }

    @Step("Начальные установки")
    @Before
    public void beforeTest() {
        softly = new SoftAssertions();
        bot.start();
        step.enableBankTerminal();
        softly.assertThat(step.isEnableBankTerminal()).as("Упал так как не смог подключить терминал").isTrue();
    }

    @After
    public void afterTest() {
        bot.stop();
    }

    @DisplayName("Проверка подключения терминала в конфиге")
    @Test
    public void testTerminalInConfig(){
        assertThat(step.isEnableBankTerminal()).as("Терминал не подключен").isTrue();
    }

    @DisplayName("Оплата по терминалу. Проверка в логе терминала")
    @Test
    public void testPaymentOperationInTerminalLog() {
        step.deleteTerminalLogs();
        step.paymentByCreditCard();
        assertThat(step.checkOperationInLog()).as("Терминал не подключен").isTrue();
    }

    @DisplayName("Проверка соединения с хостом. Проверка в логе терминала")
    @Test
    public void testCheckConnection(){
        step.deleteTerminalLogs();
        step.checkConnection();
        assertThat(step.checkOperationInLog()).as("Проверка не прошла успешно").isTrue();
    }

    @DisplayName("Проверка загрузки ключей. Проверка в логе терминала")
    @Test
    public void testLoadKeys(){
        step.deleteTerminalLogs();
        step.loadkeys();
        assertThat(step.checkOperationInLog()).as("Загрузка ключей не прошла успешно").isTrue();
    }

    @DisplayName("Проверка загрузки параметров. Проверка в логе терминала")
    @Test
    public void testLoadParams(){
        step.deleteTerminalLogs();
        step.loadParams();
        assertThat(step.checkOperationInLog()).as("Загрузка параметров не прошла успешно").isTrue();
    }

}
