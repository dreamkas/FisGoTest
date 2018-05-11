package tests;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.CashBoxType;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.assertj.core.api.SoftAssertions;
import org.junit.*;
import steps.Steps2G;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("2G тесты")
@DisplayName("Тестирование 2G")
public class Tests2G {

    private static CashBox cashBox;
    private static Bot bot;

    private static Steps2G step;

    private SoftAssertions softly;

    @BeforeClass
    public static void init() {
        cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.243.4");
        bot = new Bot(cashBox);
        step = new Steps2G(bot, cashBox);
//        bot.start();
//        step.enable2G();
//        bot.stop();
    }

    @Step("Начальные установки")
    @Before
    public void beforeTest() {
        bot.start();
        //step.enable2G();


        softly = new SoftAssertions();
    }

    @After
    public void afterTest() {
        bot.stop();
    }


    @DisplayName("Проверка подключения 2G в системе. (команда route)")
    @Test
    public void testEnable2g() {
        assertThat(step.checkEnable2G()).as("2G не запустился").isTrue();
    }


    @DisplayName("Подключение кассы к кабинету через 2G")
    @Test
    public void testIncludeToCabinetWith2G() {
        assertThat(step.checkEnable2G()).as("2G не запустился").isTrue();
        step.disableConnectToCabinet();
        step.connectCashboxToKabinet();
        assertThat(step.checkCabinetIsEnable()).as("Упал так как не смог подключиться к кабинету").isTrue();
        bot.pressKey(cashBox.keyEnum.keyCancel, 0, 4);
        bot.sendData();
        step.disableConnectToCabinet();
        softly.assertAll();
    }

    @DisplayName("Проверка загрузки товаров на кассу из Кабинета")
    @Test
    public void testLoadGoodsFromCabinetWith2G() throws InterruptedException {
       // assertThat(step.checkEnable2G()).as("2G не запустился").isTrue();
        step.disableConnectToCabinet();
        step.cleanGoodsDb();
        step.connectCashboxToKabinet();
        assertThat(step.checkCabinetIsEnable()).as("Упал так как не смог подключиться к кабинету").isTrue();
        int actual = step.isGoodsUpload();
        softly.assertThat(actual).as("Упал так как товары не загрузились").isEqualTo(step.getCountGoodsInKabinet());
        softly.assertAll();
    }






}
