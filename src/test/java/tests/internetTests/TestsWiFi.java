package tests.internetTests;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.CashBoxType;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import steps.StepsInternet;

import static org.assertj.core.api.Java6Assertions.assertThat;

@Feature("Тесты WiFi")
@DisplayName("Тестирование WiFi")
public class TestsWiFi {

    private static CashBox cashBox;
    private static Bot bot;
    private static StepsInternet step;
    private SoftAssertions softly;

    @BeforeClass
    public static void init() {
        cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.242.111");
        bot = new Bot(cashBox);
        step = new StepsInternet(bot, cashBox);
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
    public void testEnableWiFi(){
        step.wifiEnableInMenu();

        boolean loaderIsOver = step.checkOverLoader();
        softly.assertThat(loaderIsOver).as("Бесконечный лоудер").isTrue();
        if(!loaderIsOver) {return;}

        assertThat(step.checkEnableInternetInterfaceInMenu()).as("WiFi не подключился в меню").isTrue();

        step.openWiFiSetting();

        loaderIsOver = step.checkOverLoader();
        softly.assertThat(loaderIsOver).as("Бесконечный лоудер").isTrue();
        if(!loaderIsOver) {return;}

        step.chooseWiFiNetworkAndInputPassword();

    }
}
