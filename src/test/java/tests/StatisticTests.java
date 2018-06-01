package tests;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.CashBoxType;
import io.qameta.allure.Feature;
import io.qameta.allure.junit4.DisplayName;
import json.request.data.enums.ConfigFieldsEnum;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import steps.StepsCabinet;
import steps.StepsStatistic;

import static json.request.data.enums.ConfigFieldsEnum.*;
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
        cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.242.113");
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
//        step.rebootCashBox();
        //step.configBackup();
    }

    @After
    public void after() {
        step.clearTxtFiles();
    }

    @Ignore
    @Test
    @DisplayName("Проверка валидности JSON (СТАТИСТИКИ)")
    public void testValidJson_Statistic_TerminalMode() {
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE - пустое поле)")
    public void testValidJson_Statistic_TerminalMode_Empty() {
        step.changeConfigField(TERMINAL_MODE,"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE - число)")
    public void testValidJson_Statistic_TerminalMode_two() {
        step.changeConfigField(TERMINAL_MODE,"1043");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE  - пробел)")
    public void testValidJson_Statistic_TerminalMode_SymbolSpace() {
        step.changeConfigField(TERMINAL_MODE," ");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE - точка с запятой)")
    public void testValidJson_Statistic_TerminalMode_SymbolSemicolon() {
        step.changeConfigField(TERMINAL_MODE,";");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE - двоеточие)")
    public void testValidJson_Statistic_TerminalMode_SymbolColon() {
        step.changeConfigField(TERMINAL_MODE,":");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге TERMINAL_MODE - кавычки)")
    public void testValidJson_Statistic_TerminalMode_SymbolQuotes() {
        step.changeConfigField(TERMINAL_MODE,"\"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    //******************************************************************************************************************

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FISGO_VERSION - кавычки)")
    public void testValidJson_Statistic_VersionFisGo_SymbolQuotes() {
        step.changeConfigField(FISGO_VERSION,"\"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FISGO_VERSION - пустое поле)")
    public void testValidJson_Statistic_VersionFisGo_Empty() {
        step.changeConfigField(FISGO_VERSION,"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FISGO_VERSION  - пробел)")
    public void testValidJson_Statistic_VersionFisGo_SymbolSpace() {
        step.changeConfigField(FISGO_VERSION," ");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FISGO_VERSION - точка с запятой)")
    public void testValidJson_Statistic_VersionFisGo_SymbolSemicolon() {
        step.changeConfigField(FISGO_VERSION,";");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FISGO_VERSION - двоеточие)")
    public void testValidJson_Statistic_VersionFisGo_SymbolColon() {
        step.changeConfigField(FISGO_VERSION,":");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    //******************************************************************************************************************

    @Test
    @DisplayName("Проверка валидности JSON - CASH INFO REPORT")
    public void testValidJson_CashInfoReport() {
        assertEquals(true, step.isCashInfoReportJSONValid());
    }

    //******************************************************************************************************************

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге CONFIG_VER - двоеточие)")
    public void testValidJson_Statistic_ConfigVersion_SymbolColon() {
        step.changeConfigField(CONFIG_VER,":");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге CONFIG_VER - кавычки)")
    public void testValidJson_Statistic_ConfigVersion_SymbolQuotes() {
        step.changeConfigField(CONFIG_VER,"\"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге CONFIG_VER - пустое поле)")
    public void testValidJson_Statistic_ConfigVersion_Empty() {
        step.changeConfigField(CONFIG_VER,"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге CONFIG_VER  - пробел)")
    public void testValidJson_Statistic_ConfigVersion_SymbolSpace() {
        step.changeConfigField(CONFIG_VER," ");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге CONFIG_VER - точка с запятой)")
    public void testValidJson_Statistic_ConfigVersion_SymbolSemicolon() {
        step.changeConfigField(CONFIG_VER,";");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    //******************************************************************************************************************

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FS_REPLACE_MODE - двоеточие)")
    public void testValidJson_Statistic_kk_SymbolColon() {
        step.changeConfigField(KKT_MODE,":");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FS_REPLACE_MODE - кавычки)")
    public void testValidJson_Statistic_FsReplaseMode_SymbolQuotes() {
        step.changeConfigField(FS_REPLACE_MODE,"\"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FS_REPLACE_MODE - пустое поле)")
    public void testValidJson_Statistic_FsReplaseMode_Empty() {
        step.changeConfigField(FS_REPLACE_MODE,"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FS_REPLACE_MODE  - пробел)")
    public void testValidJson_Statistic_FsReplaseMode_SymbolSpace() {
        step.changeConfigField(FS_REPLACE_MODE," ");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге FS_REPLACE_MODE - точка с запятой)")
    public void testValidJson_Statistic_FsReplaseMode_SymbolSemicolon() {
        step.changeConfigField(FS_REPLACE_MODE,";");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }
    //******************************************************************************************************************

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге KKT_MODE - двоеточие)")
    public void testValidJson_Statistic_kktMode_SymbolColon() {
        step.changeConfigField(KKT_MODE,":");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге KKT_MODE - кавычки)")
    public void testValidJson_Statistic_kktMode_SymbolQuotes() {
        step.changeConfigField(KKT_MODE,"\"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге KKT_MODE - пустое поле)")
    public void testValidJson_Statistic_kktMode_Empty() {
        step.changeConfigField(KKT_MODE,"");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге KKT_MODE  - пробел)")
    public void testValidJson_Statistic_kktMode_SymbolSpace() {
        step.changeConfigField(KKT_MODE," ");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

    @Test
    @DisplayName("Проверка валидности JSON - СТАТИСТИКА (если в конфиге KKT_MODE - точка с запятой)")
    public void testValidJson_Statistic_kktMode_SymbolSemicolon() {
        step.changeConfigField(KKT_MODE,";");
        step.clearTxtFiles();
        step.rebootCashBox();
        assertEquals(true, step.isStatisticJSONValid());
    }

}
