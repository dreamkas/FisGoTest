package steps;

import cashbox.Bot;
import cashbox.CashBox;
import io.qameta.allure.Step;
import json.request.data.enums.ConfigFieldsEnum;
import json.request.data.enums.CountersFieldsEnum;
import json.response.data.CountersResponse;
import remoteAccess.DataFromCashbox;
import screens.Screens;

import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class StepsBankTerminal {

    private Bot bot;
    private CashBox cashBox;
    private static Screens screens;

    public StepsBankTerminal(Bot bot, CashBox cashBox){
        this.bot = bot;
        this.cashBox = cashBox;
        screens = new Screens();
    }


    @Step("Включить терминал")
    public void enableBankTerminal(){
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
        //FIXME!!ИСПРАВИТЬ КОГДА СДЕЛАЮТ СОСТОЯНИЕ ЛОУДЕРА
        try {
            sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bot.pressKey(cashBox.keyEnum.keyCancel, 0, 4);
        bot.sendData();
    }

    @Step("Проверка конфига - подключен ли терминал?")
    public boolean isEnableBankTerminal(){
        Map<ConfigFieldsEnum, String> response = bot.getConfig(ConfigFieldsEnum.TERMINAL_MODE);
        String result = response.get(ConfigFieldsEnum.TERMINAL_MODE);
        return Integer.parseInt(result) == 2;
    }

    @Step("Совершить покупку банковской картой")
    public void paymentByCreditCard(){
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyPayByCard, 0, 1);
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
        //FIXME!!ИСПРАВИТЬ КОГДА СДЕЛАЮТ СОСТОЯНИЕ ЛОУДЕРА
        try {
            sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bot.pressKey(cashBox.keyEnum.keyEnter, 0, 1);
        bot.sendData();
    }

    @Step("Удаление логов терминала с кассы")
    public void deleteTerminalLogs() {
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        ssh.executeListCommand("rm /FisGo/*.log");
    }

    @Step("Проверка успешной операции в логе терминала")
    public boolean checkOperationInLog(){
        DataFromCashbox ssh = new DataFromCashbox();
        ssh.initSession(cashBox.CASHBOX_IP, CashBox.USERNAME, CashBox.PORT, CashBox.PASSWORD);
        List<String> result = ssh.executeListCommand("cat /FisGo/*.log");
        return result.get(result.size()-1).contains("╨Ю╤В╨▓╨╡╤В ╨╜╨░ ╨╖╨░╨┐╤А╨╛╤Б ╨┐╨╛╨╗╤Г╤З╨╡╨╜");
    }

    public void isPaymentSucces(){
        CountersResponse countersResponse = bot.getCounters(CountersFieldsEnum.SALE_SUMS);
        System.out.println(countersResponse.getSaleSums().getCard());
        System.out.println(countersResponse.getSaleSums().getCash());
    }

    @Step("Проверка связи")
    public void checkConnection() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key3, 0, 1);
        bot.pressKey(cashBox.keyEnum.key3, 0, 1);
        bot.sendData();
        //FIXME!!ИСПРАВИТЬ КОГДА СДЕЛАЮТ СОСТОЯНИЕ ЛОУДЕРА
        try {
            sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void loadkeys() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key3, 0, 1);
        bot.pressKey(cashBox.keyEnum.key1, 0, 1);
        bot.sendData();
        //FIXME!!ИСПРАВИТЬ КОГДА СДЕЛАЮТ СОСТОЯНИЕ ЛОУДЕРА
        try {
            sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void loadParams() {
        bot.pressKey(cashBox.keyEnum.keyMenu, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key5, 0, 1);
        bot.pressKey(cashBox.keyEnum.key3, 0, 1);
        bot.pressKey(cashBox.keyEnum.key2, 0, 1);
        bot.sendData();
        //FIXME!!ИСПРАВИТЬ КОГДА СДЕЛАЮТ СОСТОЯНИЕ ЛОУДЕРА
        try {
            sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
