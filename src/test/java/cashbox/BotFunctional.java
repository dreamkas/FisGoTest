package cashbox;

import json.request.data.enums.ConfigFieldsEnum;
import json.request.data.enums.CountersFieldsEnum;
import json.response.data.CountersResponse;

import java.util.List;
import java.util.Map;

public interface BotFunctional {

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
    String getScreenJson();

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
    void pressKey(int keyNum, int keyNum2, int pressCount);

    /**
     * Метод для получения необходимых полей из конфига.
     * @param configFieldsEnums - массив с необходимыми полями
     * @return - map со значениями нужных полей
     */
    Map<ConfigFieldsEnum, String> getConfig(ConfigFieldsEnum ... configFieldsEnums);

    /**
     * Команда для получения счетчиков
     * @param countersFieldsEnums - массив с необходимыми полями
     * @return объект CountersResponse
     */
    CountersResponse getCounters(CountersFieldsEnum ... countersFieldsEnums);

    /**
     * Команда для получения статуса лоудера
     * @return true - если на экране лоудер
     */
     boolean isLoaderScreen();

    /**
     * Отправка команды по SSH
     * @param command - команда.
     * @return List<String> - с возращаемыми полями
     */
     List<String> sendCommandSsh(String command);
}
