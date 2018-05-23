package steps;

import cashbox.Bot;
import cashbox.CashBox;
import io.qameta.allure.Step;
import screens.ScreenPicture;
import screens.Screens;

import java.util.List;

public class StepsInternet {

    private Bot bot;
    private CashBox cashBox;
    private static Screens screen;

    public StepsInternet(Bot bot, CashBox cashBox) {
        this.bot = bot;
        this.cashBox = cashBox;
        screen = new Screens();
    }

    @Step("Вклюить WiFi в меню СОСТОЯНИE")
    public void wifiEnableInMenu() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
    }
    @Step("Вклюить Ethernet в меню СОСТОЯНИE")
    public void ethernetEnableInMenu() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
    }

    @Step("Проверка - успешно ли подключился Ethernet в меню СОСТОЯНИЕ")
    public boolean checkEnableInternetInterfaceInMenu() {
        bot.getScreenJson();
        return screen.compareScreen(ScreenPicture.MENU_INTERNET);
    }

    @Step("Открыть меню НАСТРОЙКИ в ethernet")
    public void openEthernetSetting() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.sendData();
    }

    @Step("Ожидание лоудера")
    public boolean checkOverLoader() {
        int count = 0;
        while (bot.isLoaderScreen()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            if (count == 1000000) {
                return false;
            }
        }
        return true;
    }

    @Step("Открыть меню НАСТРОЙКИ в WiFi")
    public void openWiFiSetting() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.sendData();
    }

    //TODO НЕВОЗМОЖНО ВЫБРАТЬ НУЖНУЮ СЕТЬ ПОКА НЕ БУДЕТ ВЫПОЛНЕНА ЗАДАЧА НА ПОЛУЧЕНИЕ СПИСКА ПУНКТОВ ИЗ МЕНЮ
    @Step("Выбрать сеть для подключения и ввести пароль")
    public void chooseWiFiNetworkAndInputPassword() {
        bot.pressKey(cashBox.keyEnum.keyDown, 0, 2);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
        bot.enterStringData("Dream50Guest");
    }

    @Step("Выбрать автоматическое подключение к ethernet")
    public void chooseAutoConnectEthernet() {
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step("Выбрать ручное подключение к ethernet")
    public void chooseManualConnectEthernet() {
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
        //TODO СУПЕР КОСТЫЛЬ )))00) НЕОБХОДИМО НАПИСАТЬ МЕТОД ДЛЯ ПАРСИНГА СТРОК В НАБОР КНОПОК
        enterDataForManualEthernet(192, 168, 242, 111);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        enterDataForManualEthernet(255, 255, 254, 0);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        enterDataForManualEthernet(192, 168, 242, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        enterDataForManualEthernet(8, 8, 8, 8);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        enterDataForManualEthernet(8, 8, 4, 4);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step("Проверка, что на кассе экран об успешном подключении к Ethernet")
    public boolean checkScreenSuccesfulConnect() {
        bot.getScreenJson();
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
        return screen.compareScreen(ScreenPicture.SCREEN_SUCCES_CONNECT_ETHERNET);
    }

    @Step("Пинг 8.8.8.8 для проверки работы интернета")
    public boolean checkSuccessPing() {
        List<String> pingData = bot.sendCommandSsh("ping 8.8.8.8 -c4");
        return pingData.size() == 9 && pingData.get(7).equals("4 packets transmitted, 4 packets received, 0% packet loss");
    }

    private void enterDataForManualEthernet(int a, int b, int c, int d){
        bot.enterData(String.valueOf(a));
        bot.pressKey(cashBox.keyEnum.keyComma, 0, 1);
        bot.enterData(String.valueOf(b));
        bot.pressKey(cashBox.keyEnum.keyComma, 0, 1);
        bot.enterData(String.valueOf(c));
        bot.pressKey(cashBox.keyEnum.keyComma, 0, 1);
        bot.enterData(String.valueOf(d));
    }
}
