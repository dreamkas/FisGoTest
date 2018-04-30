package cashbox;

import json.request.data.enums.ConfigFieldsEnum;
import json.request.data.enums.CountersFieldsEnum;
import json.response.data.CountersResponse;

import java.util.Map;

public interface IBot {

    /**
     * Метод для старта бота (установка tcp-соединения с сервером кассы)
     */
    void start();

    /**
     * Метод для закрытия соединения с кассой.
     */
    void stop();

    /**
     * Команда для получения скриншота с экрана кассы. Скриншот сохраняется в папку reciveData в каталоге проекта.
     */
    void getScreenJson();

    /**
     * Команда на получение текущего режима клавиатуры. (кириллица, английский и т.д.)
     * @return int - номер обозначающий режиим клавиатуры
     */
    int getKeypadMode();

    /**
     * Нажатие клавиши на кассе.
     * @param keyNum - первая кнопка
     * @param keyNum2 - вторая кнопка
     * @param pressCount - количество нажатий
     */
    void pressKeyBot(int keyNum, int keyNum2, int pressCount);

    /**
     * Метод для получения необходимых полей из конфига.
     * @param configFieldsEnums - массив с необходимыми полями
     * @return - map со значениями нужных полей
     */
    Map<ConfigFieldsEnum, String> getConfig(ConfigFieldsEnum ... configFieldsEnums);

    CountersResponse getCounters(CountersFieldsEnum ... countersFieldsEnums);
}
