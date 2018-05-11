package steps;

import cashbox.Bot;
import cashbox.CashBox;
import io.qameta.allure.Step;
import screens.ScreenPicture;
import screens.Screens;

public class StepsPassword {

    private Bot bot;
    private CashBox cashBox;
    private static Screens screens;

    public StepsPassword(Bot bot, CashBox cashBox) {
        this.bot = bot;
        this.cashBox = cashBox;
    }

    @Step("Перезапустить кассу")
    public void rebootCashbox() {
        bot.rebootCashBox();
    }

    @Step("Ввод пароля")
    public void inputPassword() {
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key3, 0, 1);
        bot.pressKey(cashBox.keyEnum.key4, 0, 1);
    }

    @Step("Ввод неправильного пароля")
    public void inputincorrectPassword() {
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
    }

    @Step("Проверка - успешный ввод пароля")
    public boolean isSuccessfulEntryPassword() {
        bot.getScreenJson();
        return screens.compareScreen(ScreenPicture.FREE_SALE_MODE) | screens.compareScreen(ScreenPicture.MENU_SYSTEM);
    }

    @Step("Сменить пользователя")
    public void changeUser() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key4, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
    }
}
