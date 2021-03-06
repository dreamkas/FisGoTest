package steps;

import cashbox.Bot;
import cashbox.CashBox;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import json.request.data.enums.ConfigFieldsEnum;
import remoteAccess.DataFromCashbox;
import screens.ScreenPicture;
import screens.Screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);                        //МЕНЮ
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);                           //НАСТРОЙКИ
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);                           //СЕТЬ
        bot.pressKey(cashBox.keyEnum.key3, 0, 1);                           //2G
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);                           //СОСТОЯНИЕ
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);                           //ВКЛ
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);                       //ENTER
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);                           //НАСТРОЙКА
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);                           //МТС
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);                       //ЕНТЕР
        bot.sendData();
        try {
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bot.pressKey(cashBox.keyEnum.keyCancel, 0, 4);                      //назад
        bot.sendData();
    }

    @Step("Выключить Ethernet")
    public void disableEthernet() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyCancel, 0, 4);
        bot.sendData();
    }

    @Step("Выключить WiFi")
    public void disableWiFi() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyCancel, 0, 4);
        bot.sendData();
    }

    @Step("Подключить кассу к кабинету")
    public void connectCashboxToKabinet() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key8, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.sendData();
        bot.enterData(getCodeFromKabinet());
        bot.sendData();
    }

    @Step("Отключить кассу от кабинета если она подключена")
    public void disableConnectToCabinet() {
        if (isCabinetEnable()){
            bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
            bot.pressKey(cashBox.keyEnum.key5, 0, 1);
            bot.pressKey(cashBox.keyEnum.key8, 0, 1);
            bot.pressKey(cashBox.keyEnum.key2, 0, 1);
            bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
            bot.sendData();
            checkCabinetIsDisable();
        }
    }

    @Step("Очистить БД товаров на кассе")
    public void cleanGoodsDb() {
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        ssh.executeListCommand("echo \"attach '/FisGo/goodsDb.db' as goods; delete from goods.GOODS;\" | sqlite3 /FisGo/goodsDb.db");
        System.out.println("Очистка БД товаров");
    }

    @Step("Проверка - подключился ли 2G")
    public boolean checkEnable2G(){
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
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
    public int getCountGoodsInKabinet() {
        Response response = authByUserToken(TOKEN).when().get("https://kabinet-beta.dreamkas.ru/api/products/count").then().
                contentType(ContentType.JSON).
                extract().response();
        return response.path("count");
    }

    /**
     * Проверка после подключения кассы к кабинету
     * @return true, если касса подключенна к кабинету
     */
    @Step("Проверка - подключен ли кабинет")
    public boolean checkCabinetIsEnable() {
        while (!isCabinetEnable()) {
            if (isIncorrectCabinetCode() || isCabinetError()) {
                bot.pressKey(cashBox.keyEnum.keyCancel, 0, 1);
                bot.sendData();
                return false;
            }
        }
        bot.pressKey(cashBox.keyEnum.keyCancel, 0, 1);
        bot.sendData();
        return true;
    }

    /**
     * Проверка подключена ли касса к Кабинету. Проверка из конфига кассы
     * @return boolean
     */
    public boolean isCabinetEnable() {
        List<ConfigFieldsEnum> configFields = new ArrayList<>();
        configFields.add(ConfigFieldsEnum.IS_CABINET_ENABLE);
        Map<ConfigFieldsEnum, String> response = bot.getConfig(ConfigFieldsEnum.IS_CABINET_ENABLE);
        String result = response.get(ConfigFieldsEnum.IS_CABINET_ENABLE);
        return Integer.parseInt(result) != 0;
    }

    /**
     * Проверка - экран - "Неверный код"
     * @return true, если на кассе экран "Неверный код"
     */
    private boolean isIncorrectCabinetCode() {
        return screens.compareScreen(ScreenPicture.INCORRECT_CABINET_CODE, bot.getScreenJson());
    }

    /**
     * Проверка - экран - "Ошибка кабинета"
     * @return true, если на кассе экран "Ошибка кабинета"
     */
    private boolean isCabinetError() {
        return screens.compareScreen(ScreenPicture.CABINET_ERROR, bot.getScreenJson());
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
            bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
            bot.sendData();
            return true;
        }
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
        return false;
    }

    /**
     * Проверка - экран - "Кабинет успешно отключен"
     * @return true, если на кассе экран "Ошибка кабинета"
     */
    private boolean isCabinetDisableScreen(){
        return screens.compareScreen(ScreenPicture.CABINET_SUCCES_DISABLE, bot.getScreenJson());
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

    /**
     * Проверка, что товары из кабинета загруженны на кассу. Каждую минуту метод сравнивнивает количество товаров
     * на кассе с ожидаемым количеством товаров в кабинете.
     * @return true - если на касссе заргуженны все товары из кабинета.
     */
    public int isGoodsUpload() throws InterruptedException {
        int expectedCountGoods = getCountGoodsInKabinet();
        int currentCountGoodsInCashbox = 0;
        int pastCountGoodsInCashbox;
        int flag = 0;
        while (true) {
            pastCountGoodsInCashbox = currentCountGoodsInCashbox;
            currentCountGoodsInCashbox = getCountGoodsInCashBox();
            System.out.println("ТЕКУЩЕЕ КОЛЛИЧЕСТВО ТОВАРОВ НА КАССЕ = " + currentCountGoodsInCashbox);
            if (currentCountGoodsInCashbox < expectedCountGoods) {
                //System.out.println("*текущее* меньше *ожидаемого*");
                sleep(60000);
                if (currentCountGoodsInCashbox == pastCountGoodsInCashbox) {
                    flag++;
                    System.out.println("*текущее* = *предыдущему* ФЛАГ = " + flag);
                } else {flag = 0;}
                if (flag == 4) {
                    return currentCountGoodsInCashbox;
                }
            } else return currentCountGoodsInCashbox;
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
