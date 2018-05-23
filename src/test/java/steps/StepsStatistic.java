package steps;

import cashbox.Bot;
import cashbox.CashBox;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import json.request.data.enums.ConfigFieldsEnum;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static tests.Controllers.PATH_CASH_INFO_REPORT;
import static tests.Controllers.PATH_STATS;

public class StepsStatistic {

    private Bot bot;
    private CashBox cashBox;

    public StepsStatistic(Bot bot, CashBox cashBox) {
        this.bot = bot;
        this.cashBox = cashBox;
    }

    @Step("Проверка валидности полученного JSON (СТАТИСТИКА)")
    public boolean isStatisticJSONValid(){
        return isJSONValid(PATH_STATS);
    }

    @Step("Проверка валидности полученного JSON (CASH INFO REPORT)")
    public boolean isCashInfoReportJSONValid(){
        return isJSONValid(PATH_CASH_INFO_REPORT);
    }

    @Step("Изменить поле конфига на необходимое значение")
    public void changeConfigField(ConfigFieldsEnum field, String value) {
        bot.sendCommandSsh("echo \"attach '/FisGo/configDb.db' as config; update config.CONFIG set " + field + "='" + value + "';\" | sqlite3 /FisGo/configDb.db");
    }

    @Step("Перезагрузить кассу")
    public void rebootCashBox() {
        bot.sendCommandSsh("/sbin/reboot");
        try {
            Thread.sleep(80_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step("Сделать backup конфига")
    public void configBackup() {
        bot.sendCommandSsh("cp /FisGo/configDb.db /FisGo/configDb_backup_for_test.db");
    }

    private boolean isJSONValid(String path) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(readTxtFile(path).get(0));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void clearTxtFiles(){
        clearTxtFile(PATH_STATS);
        clearTxtFile(PATH_CASH_INFO_REPORT);
    }
    private void clearTxtFile(String path) {
        try {
            FileWriter fstream = new FileWriter(path);// конструктор с одним параметром - для перезаписи
            BufferedWriter out = new BufferedWriter(fstream); //  создаём буферезированный поток
            out.write(""); // очищаем, перезаписав поверх пустую строку
            out.close(); // закрываем
        } catch (Exception e) {
            System.err.println("Error in file cleaning: " + e.getMessage());
        }
    }

    private List<String> readTxtFile(String path) {
        List<String> list = new ArrayList<>();
        try {
            FileInputStream fstream = new FileInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                list.add(strLine);
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла");
        }
        return list;
    }
}
