package cashbox;

import json.request.data.enums.ConfigFieldsEnum;

import java.util.Map;

public interface IBot {

    void start();

    void stop();

    void getScreenJson();

    int getKeypadMode();

    Map<ConfigFieldsEnum, String> cfgGetJson(ConfigFieldsEnum ... configFieldsEnums);

    void pressKeyBot(int keyNum, int keyNum2, int pressCount);
}
