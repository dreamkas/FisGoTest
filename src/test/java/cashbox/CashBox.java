package cashbox;

import keypad.KeyEnum;

/**
 * данные для прогона тестов
 * UUID кассы, IP, порт, тип
 */
public class CashBox {
    public String UUID;// = "12345678-1234-1234-1234-123456789012"; //пока не парсится ни на сервере, ни на клиенте

    //параметры для открытия сокета с кассой
    public CashBoxType CASHBOX_TYPE;// = "DreamkasF"; //DreamkasRF
    public String CASHBOX_IP; //= "192.168.242.101";
    public static final int CASHBOX_PORT = 3425;


    //параметры подключения по ssh
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    public static final int PORT = 22;

    public KeyEnum keyEnum = new KeyEnum();

    public CashBox(String UUID, CashBoxType CASHBOX_TYPE, String CASHBOX_IP) {
        this.UUID = UUID;
        this.CASHBOX_TYPE = CASHBOX_TYPE;
        this.CASHBOX_IP = CASHBOX_IP;
        keyEnum.initKeyEnum(this.CASHBOX_TYPE);

    }
}
