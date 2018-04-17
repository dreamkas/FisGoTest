package cashbox;

/**
 * данные для прогона тестов
 * UUID кассы, IP, порт, тип
 */
public class Config {
    public static final String UUID = "12345678-1234-1234-1234-123456789012"; //пока не парсится ни на сервере, ни на клиенте

    //параметры для открытия сокета с кассой
    public static final String CASHBOX_TYPE = "DreamkasRF"; //DreamkasRF
    public static final String CASHBOX_IP = "192.168.242.80";
    public static final int CASHBOX_PORT = 3425;

    //параметры подключения по ssh
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    public static final int PORT = 22;
}
