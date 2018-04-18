package tests;

import static org.junit.Assert.assertEquals;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.CashBoxType;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import remoteAccess.DataFromCashbox;
import remoteAccess.TCPSocket;

import static java.lang.Thread.sleep;

import java.io.File;

import static io.restassured.RestAssured.given;

public class Tests2G {

    private static TCPSocket tcpSocket;
    private static CashBox cashBox;
    private static Bot bot;
    private static final String TOKEN = "29a76931-f19a-4c74-a8d1-0df938f1cf1b";

    @BeforeClass
    public static void init() {
        tcpSocket = new TCPSocket();
        cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.242.116");
        bot = new Bot(cashBox);
        tcpSocket.createSocket(cashBox.CASHBOX_IP, CashBox.CASHBOX_PORT);
    }

    @Before
    public void beforeTest() {
        disableEthernet();
        disableWiFi();
        enable2G();
    }

    @After
    public void closeConn() {
        bot.pressKeyBot(cashBox.keyEnum.keyCancel, 0, 1);
        tcpSocket.sendDataToSocket(bot.getTaskId(), bot.resultJson());

        bot.closeSessionJson();
        tcpSocket.socketClose(bot.resultJson());
    }

    @Test
    public void test2gWithBankTerminal() {
        enableTerminal();
        paymentByCreditCard();
        //TODO
    }

    @Test
    public void test2gWithKabinet() throws Exception {
        connectCashboxToKabinet();
        boolean result = isGoodsUpload();
        assertEquals(true, result);
    }

    private boolean isGoodsUpload() throws InterruptedException {
        int expectedCountGoods = 0;
        int currentCountGoodsInCashbox = 0;
        int pastCountGoodsInCashbox = 0;
        int flag = 0;
        while (true) {
            expectedCountGoods = getCountGoodsInKabinet();
            pastCountGoodsInCashbox = currentCountGoodsInCashbox;
            currentCountGoodsInCashbox = getCountGoodsInCashBox();
            if (currentCountGoodsInCashbox < expectedCountGoods) {
                sleep(10000);
                if (currentCountGoodsInCashbox == pastCountGoodsInCashbox) {
                    flag++;
                }
                if (flag == 3) {
                    return false;
                }
            } else return true;
        }
    }

    private void connectCashboxToKabinet() {
        bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key8, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.sendData();
        bot.enterData(getCodeFromKabinet());
        bot.sendData();
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
        bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key3, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyCancel, 0, 4);
    }

    private void disableWiFi() {
        bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bot.pressKeyBot(cashBox.keyEnum.keyCancel, 0, 4);
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
}
