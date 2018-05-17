package steps;

import cashbox.Bot;
import cashbox.CashBox;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import json.request.data.enums.ConfigFieldsEnum;
import screens.ScreenPicture;
import screens.Screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class StepsCabinet {

    private Bot bot;
    private CashBox cashBox;
    private static Screens screens;

    private static final String TOKEN = "350dd7e3-56f0-445c-a72c-a275a10e3955";

    public StepsCabinet(Bot bot, CashBox cashBox) {
        this.bot = bot;
        this.cashBox = cashBox;
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

    private String getCodeFromKabinet() {
        Response response = authByUserToken(TOKEN).when().get("https://kabinet.dreamkas.ru/api/users/0/pin").then().
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
        bot.getScreenJson();
        return screens.compareScreen(ScreenPicture.CABINET_SUCCES_DISABLE);
    }

    /**
     * Авторизация по токену для работы с кабинетом
     * @param token - токен полученный в кабинете.
     */
    private RequestSpecification authByUserToken(String token) {
        return given().header("Authorization", "Bearer " + token);
    }

}
