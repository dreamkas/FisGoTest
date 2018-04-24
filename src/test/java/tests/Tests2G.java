package tests;

import static org.junit.Assert.assertEquals;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.CashBoxType;
import cashbox.ConfigFieldsEnum;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.assertj.core.api.SoftAssertions;
import org.junit.*;
import remoteAccess.DataFromCashbox;
import remoteAccess.TCPSocket;
import io.qameta.allure.junit4.DisplayName;

import screens.ScreenPicture;
import screens.Screens;

import static java.lang.Thread.sleep;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@Feature("2G тесты")
public class Tests2G  {

    private static TCPSocket tcpSocket;
    private static CashBox cashBox;
    private static Bot bot;
    private static Screens screens;

    private SoftAssertions softly;

    private static final String TOKEN = "29a76931-f19a-4c74-a8d1-0df938f1cf1b";

    @BeforeClass
    public static void init() {
        cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.243.4");
        bot = new Bot(cashBox);
        screens = new Screens();
    }


    @Before
    public void beforeTest() {
        bot.start();
        //disableEthernet();
        //disableWiFi();
        enable2G();

        softly = new SoftAssertions();
    }

    @After
    public void closeConn() {
        bot.stop();
    }

    @Ignore
    @Test
    public void test2gWithBankTerminal() {
        enableTerminal();
        paymentByCreditCard();
        assertEquals(true, isSuccessfulPayment());
    }

    @DisplayName("Подключение кассы к кабинету через 2G")
    @Test
    public void testIncludeToCabinetWith2G(){
        disableConnectToCabinet();
        connectCashboxToKabinet();
        softly.assertThat(checkCabinetIsEnable()).as("Упал так как не смог подключиться к кабинету").isTrue();
       // assertEquals(true, checkCabinetIsEnable());
        bot.pressKeyBot(cashBox.keyEnum.keyCancel, 0, 4);
        bot.sendData();
        disableConnectToCabinet();
        softly.assertAll();
    }


    @Ignore
    @DisplayName("Проверка загрузки товаров на кассу из Кабинета")
    @Test
    public void testLoadGoodsFromCabinetWith2G(){
        cleanGoodsDb();
        connectCashboxToKabinet();

    }


    private boolean isGoodsUpload() throws InterruptedException {
        int expectedCountGoods = getCountGoodsInKabinet();
        int currentCountGoodsInCashbox = 0;
        int pastCountGoodsInCashbox;
        int flag = 0;
        while (true) {
            pastCountGoodsInCashbox = currentCountGoodsInCashbox;
            currentCountGoodsInCashbox = getCountGoodsInCashBox();
            if (currentCountGoodsInCashbox < expectedCountGoods) {
                sleep(100000);
                if (currentCountGoodsInCashbox == pastCountGoodsInCashbox) {
                    flag++;
                }
                if (flag == 3) {
                    return false;
                }
            } else return true;
        }
    }

    @Step("Подключить кассу к кабинету")
    private void connectCashboxToKabinet() {
        bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key8, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.sendData();
        bot.enterData(getCodeFromKabinet());
        bot.sendData();
    }

   @Step("Отключить кассу от кабинета")
    private void disableConnectToCabinet() {
        if (isCabinetEnable()){
            bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
            bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
            bot.pressKeyBot(cashBox.keyEnum.key8, 0, 1);
            bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
            bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);
            bot.sendData();

            softly.assertThat(checkCabinetIsDisable()).as("Упал так как не смог отключиться от кабинета").isTrue();
        }
    }

    private RequestSpecification authByUserToken(String token) {
        return given().header("Authorization", "Bearer " + token);
    }


    private String getCodeFromKabinet() {
        Response response = authByUserToken(TOKEN).when().get("https://kabinet-beta.dreamkas.ru/api/users/0/pin").then().
                contentType(ContentType.JSON).
                extract().response();
        return response.path("code").toString();
    }

    private Response loadCoodsInKabinet(String fileName, String service) {
        return authByUserToken(TOKEN).with().contentType("multipart/form-data")
                .multiPart("type", "xlsx")
                .multiPart("devices", "ALL")
                .multiPart("file", new File("src/test/resources/" + fileName))
                .when().post(service);
    }

    private void paymentByCreditCard() {
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyPayByCard, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
    }

    private void enableTerminal() {
        bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyCancel, 0, 4);
    }

    private void enable2G() {
        bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);                        //МЕНЮ
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);                           //НАСТРОЙКИ
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);                           //СЕТЬ
        bot.pressKeyBot(cashBox.keyEnum.key3, 0, 1);                           //2G
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);                           //СОСТОЯНИЕ
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);                           //ВКЛ
        bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);                       //ENTER
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);                           //НАСТРОЙКА
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);                           //МТС
        bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);                       //ЕНТЕР
        bot.sendData();
        try {
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bot.pressKeyBot(cashBox.keyEnum.keyCancel, 0, 4);                      //назад
        bot.sendData();
    }

    private void disableWiFi() {
        bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyCancel, 0, 4);
        bot.sendData();
    }

    private void disableEthernet() {
        bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyCancel, 0, 4);
        bot.sendData();
    }

    private int getCountGoodsInKabinet() {
        Response response = authByUserToken(TOKEN).when().get("https://kabinet-beta.dreamkas.ru/api/products/count").then().
                contentType(ContentType.JSON).
                extract().response();
        return response.path("count");
    }

    private int getCountGoodsInCashBox() {
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        String count = ssh.executeListCommand("echo \"attach '/FisGo/goodsDb.db' as goods; select count (*) from goods.GOODS;\" | sqlite3 /FisGo/goodsDb.db").get(0);
        return Integer.parseInt(count);
    }

    private void cleanGoodsDb() {
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        ssh.executeListCommand("echo \"attach '/FisGo/goodsDb.db' as goods; delete from goods.GOODS;\" | sqlite3 /FisGo/goodsDb.db");
    }


    private boolean isSuccessfulPayment() {
        //TODO
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        ssh.executeListCommand("echo \"attach '/FisGo/goodsDb.db' as goods; delete (*) from goods.GOODS;\" | sqlite3 /FisGo/goodsDb.db");
        return true;
    }

    //******************************************************************************************************************

    private boolean checkCabinetIsEnable() {
        while (!isCabinetEnable()) {
            if (isIncorrectCabinetCode() || isCabinetError()) {
                return false;
            }
        }
        return true;
    }

    private boolean isCabinetEnable() {
        List<ConfigFieldsEnum> configFields = new ArrayList<>();
        configFields.add(ConfigFieldsEnum.IS_CABINET_ENABLE);
        List<String> response = bot.cfgGetJson(configFields);
        return Integer.parseInt(response.get(0)) != 0;
    }

    private boolean isIncorrectCabinetCode() {
        bot.getScreenJson();
        return screens.compareScreen(ScreenPicture.INCORRECT_CABINET_CODE);
    }

    private boolean isCabinetError() {
        bot.getScreenJson();
        return screens.compareScreen(ScreenPicture.CABINET_ERROR);
    }

    private boolean checkCabinetIsDisable() {
        while (isCabinetEnable()) {
            if (isCabinetError()){
                System.out.println("экран error");
                return false;}
        }
        if (isCabinetDisableScreen()){
            System.out.println("экран кабинет отключен ");
            bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);
            return true;
        }
        return false;
    }

    private boolean isCabinetDisableScreen(){
        bot.getScreenJson();
        return screens.compareScreen(ScreenPicture.CABINET_SUCCES_DISABLE);
    }



    //******************************************************************************************************************

}
