package cashbox;

/**
 * Список возможных команд, которые обрабатываются сервером на кассе
 */
public enum CommandEnum {
    KEYPAD_ACTION,
    LCD_SCREEN,
    KEYPAD_MODE,
    CLOSE_SESSION,
    CFG_GET
}
