package steps;

import cashbox.Bot;
import cashbox.CashBox;
import cashbox.ConfigFieldsEnum;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import remoteAccess.DataFromCashbox;
import screens.ScreenPicture;
import screens.Screens;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static java.lang.Thread.sleep;

public class Steps2G {

    private Bot bot;
    private CashBox cashBox;
    private static Screens screens;

    private static final String TOKEN = "29a76931-f19a-4c74-a8d1-0df938f1cf1b";

    public Steps2G(Bot bot, CashBox cashBox) {
        this.bot = bot;
        this.cashBox = cashBox;
        screens = new Screens();
    }

    @Step("Включить 2G")
    public void enable2G() {
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

    @Step("Выключить Ethernet")
    public void disableEthernet() {
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

    @Step("Выключить WiFi")
    public void disableWiFi() {
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

    @Step("Подключить кассу к кабинету")
    public void connectCashboxToKabinet() {
        bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key8, 0, 1);
        bot.pressKeyBot(cashBox.keyEnum.key1, 0, 1);
        bot.sendData();
        bot.enterData(getCodeFromKabinet());
        bot.sendData();
    }

    @Step("Отключить кассу от кабинета")
    public void disableConnectToCabinet() {
        if (isCabinetEnable()){
            bot.pressKeyBot(cashBox.keyEnum.keyMenu, 0, 1);
            bot.pressKeyBot(cashBox.keyEnum.key5, 0, 1);
            bot.pressKeyBot(cashBox.keyEnum.key8, 0, 1);
            bot.pressKeyBot(cashBox.keyEnum.key2, 0, 1);
            bot.pressKeyBot(cashBox.keyEnum.keyEnter, 0, 1);
            bot.sendData();
            checkCabinetIsDisable();
        }
    }

    @Step("Очистить БД товаров на кассе")
    public void cleanGoodsDb() {
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        ssh.executeListCommand("echo \"attach '/FisGo/goodsDb.db' as goods; delete from goods.GOODS;\" | sqlite3 /FisGo/goodsDb.db");
    }

    @Step("Проверить подключился ли 2G")
    public boolean checkEnable2G(){
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        List list = ssh.executeListCommand("/sbin/route");
        String[] route = ssh.executeListCommand("/sbin/route")
                .get(2)
                .replaceAll("[\\s]{2,}", " ")
                .split(" ");

        System.out.println('w');
        return route[route.length - 1].equals("ppp0");
    }

    /**
     * Авторизация по токену для работы с кабинетом
     * @param token - токен полученный в кабинете.
     */
    private RequestSpecification authByUserToken(String token) {
        return given().header("Authorization", "Bearer " + token);
    }

    /**
     * Получение кода для подключения к Кабинету
     */
    private String getCodeFromKabinet() {
        Response response = authByUserToken(TOKEN).when().get("https://kabinet-beta.dreamkas.ru/api/users/0/pin").then().
                contentType(ContentType.JSON).
                extract().response();
        return response.path("code").toString();
    }

    /**
     * Получение колличества товаров в кабинете
     * @return - колличество товаров.
     */
    private int getCountGoodsInKabinet() {
        Response response = authByUserToken(TOKEN).when().get("https://kabinet-beta.dreamkas.ru/api/products/count").then().
                contentType(ContentType.JSON).
                extract().response();
        return response.path("count");
    }

    /**
     * Проверка после подключения кассы к кабинету
     * @return true, если касса подключенна к кабинету
     */
    public boolean checkCabinetIsEnable() {
        while (!isCabinetEnable()) {
            if (isIncorrectCabinetCode() || isCabinetError()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверка подключена ли касса к Кабинету. Проверка из конфига кассы
     * @return boolean
     */
    private boolean isCabinetEnable() {
        List<ConfigFieldsEnum> configFields = new ArrayList<>();
        configFields.add(ConfigFieldsEnum.IS_CABINET_ENABLE);
        List<String> response = bot.cfgGetJson(configFields);
        return Integer.parseInt(response.get(0)) != 0;
    }

    /**
     * Проверка - экран - "Неверный код"
     * @return true, если на кассе экран "Неверный код"
     */
    private boolean isIncorrectCabinetCode() {
        bot.getScreenJson();
        return screens.compareScreen(ScreenPicture.INCORRECT_CABINET_CODE);
    }

    /**
     * Проверка - экран - "Ошибка кабинета"
     * @return true, если на кассе экран "Ошибка кабинета"
     */
    private boolean isCabinetError() {
        bot.getScreenJson();
        return screens.compareScreen(ScreenPicture.CABINET_ERROR);
    }

    /**
     * Проверка, что кабинет отключился.
     * @return true - если кабинет отключен.
     */
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

    /**
     * Проверка - экран - "Кабинет успешно отключен"
     * @return true, если на кассе экран "Ошибка кабинета"
     */
    private boolean isCabinetDisableScreen(){
        bot.getScreenJson();
        return screens.compareScreen(ScreenPicture.CABINET_SUCCES_DISABLE);
    }

    /**
     * Получить количесво товаров к БД товаров на кассе
     * @return - количество товаров
     */
    private int getCountGoodsInCashBox() {
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        String count = ssh.executeListCommand("echo \"attach '/FisGo/goodsDb.db' as goods; select count (*) from goods.GOODS;\" | sqlite3 /FisGo/goodsDb.db").get(0);
        return Integer.parseInt(count);
    }


    public boolean isGoodsUpload() throws InterruptedException {
        int expectedCountGoods = getCountGoodsInKabinet();
        int currentCountGoodsInCashbox = 0;
        int pastCountGoodsInCashbox;
        int flag = 0;
        while (true) {
            pastCountGoodsInCashbox = currentCountGoodsInCashbox;
            currentCountGoodsInCashbox = getCountGoodsInCashBox();
            System.out.println("ТЕКУЩЕЕ КОЛЛИЧЕСТВО ТОВАРОВ НА КАССЕ = " + currentCountGoodsInCashbox);
            if (currentCountGoodsInCashbox < expectedCountGoods) {
                System.out.println("*текущее* меньше *ожидаемого*");
                sleep(60000);
                if (currentCountGoodsInCashbox == pastCountGoodsInCashbox) {
                    flag++;
                    System.out.println("*текущее* = *предыдущему* ФЛАГ = " + flag);
                }
                if (flag == 3) {
                    return false;
                }
            } else return true;
        }
    }

    private boolean isSuccessfulPayment() {
        //TODO
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        ssh.executeListCommand("echo \"attach '/FisGo/goodsDb.db' as goods; delete (*) from goods.GOODS;\" | sqlite3 /FisGo/goodsDb.db");
        return true;
    }


}
