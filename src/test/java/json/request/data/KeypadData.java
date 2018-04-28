package json.request.data;

import keypad.KeypadActionEnum;
import lombok.AllArgsConstructor;

/**
 * Класс, для передачи в Json даннх о работе с клавиатурой
 * - key1 - первая кнопка
 * - key2 - вторая кнопка
 * - action - выоплняемое действие (список возможных дейсвий с клавиатурой опписаны в KeypadActionEnum)
 */

@AllArgsConstructor
public class KeypadData {
    private int key1;
    private int key2;
    private KeypadActionEnum action;
}
