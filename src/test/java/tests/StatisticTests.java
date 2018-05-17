package tests;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.CashBoxType;
import io.qameta.allure.Feature;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import steps.StepsCabinet;
import steps.StepsStatistic;

import static json.request.data.enums.ConfigFieldsEnum.TERMINAL_MODE;
import static org.junit.Assert.assertEquals;

@Feature("Тесты статистики")
@DisplayName("Тестирование статистики")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StatisticTests {

    @Autowired
    Controllers controllers;

    @Autowired
    Environment environment;

    private static CashBox cashBox;
    private static Bot bot;
    private static StepsStatistic step;
    private static StepsCabinet stepsCabinet;

    @BeforeClass
    public static void beforeClass() {
        cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.242.164");
        bot = new Bot(cashBox);
        step = new StepsStatistic(bot, cashBox);
        stepsCabinet = new StepsCabinet(bot, cashBox);

//        bot.start();
//        stepsCabinet.connectCashboxToKabinet();
//        assertThat(stepsCabinet.checkCabinetIsEnable()).as("Касса не подключилась к Кабинету").isTrue();
//        bot.stop();
    }

    @Before
    public void before() throws InterruptedException {
        step.rebootCashBox();
    }

    @After
    public void after() {
        step.clearTxtFiles();
    }

    @Test
    @DisplayName("Проверка валидности JSON (СТАТИСТИКИ)")
    public void testValidJson_Statistic_TerminalMode() {
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName(("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE - пустое поле)"))
    public void testValidJson_Statistic_TerminalMode_Empty() {
        step.changeConfigField(TERMINAL_MODE,"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName(("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE  - пробел)"))
    public void testValidJson_Statistic_TerminalMode_SymbolSpace() {
        step.changeConfigField(TERMINAL_MODE," ");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName(("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE - точка с запятой)"))
    public void testValidJson_Statistic_TerminalMode_SymbolSemicolon() {
        step.changeConfigField(TERMINAL_MODE,";");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName(("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE - двоеточие)"))
    public void testValidJson_Statistic_TerminalMode_SymbolColon() {
        step.changeConfigField(TERMINAL_MODE,":");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName(("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE - кавычки)"))
    public void testValidJson_Statistic_TerminalMode_SymbolQuotes() {
        step.changeConfigField(TERMINAL_MODE,"\"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName(("Проверка валидности JSON - CASH INFO REPORT (если в конфиге TERMINAL_MODE - кавычки)"))
    public void testValidJson_CashInfoReport_TerminalMode_SymbolQuotes() {
        assertEquals(true, step.isStatisticJSONValid());
    }

}
