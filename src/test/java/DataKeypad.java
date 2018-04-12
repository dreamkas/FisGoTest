/**
 * Класс, для передачи в Json даннх о работе с клавиатурой
 * - key1 - первая кнопка
 * - key2 - вторая кнопка
 * - action - выоплняемое действие (список возможных дейсвий с клавиатурой опписаны в KeypadActionEnum)
 */
public class DataKeypad {
    private int key1;
    private int key2;
    private KeypadActionEnum action;

    public DataKeypad(int key1, int key2, KeypadActionEnum action) {
        this.key1 = key1;
        this.key2 = key2;
        this.action = action;
    }

}
