package json.fromSever;

import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import cashbox.ConfigFieldsEnum;

/**
 * Класс, в который парсятся отдельные таски из Json-а с кассы
 */

public class TaskResponse {
    private int task_id;
    @Getter
    private String result;
    private int[] lcd_screen = null;
    @Getter
    private int keypad_mode;
    private CashboxConfig cfg_data = null;

    public TaskResponse (int task_id, String result, int[] lcd_screen, int keypad_mode, CashboxConfig cfg_data) { // List<json.fromSever.CashboxConfig>cfg_data) {
        this.task_id = task_id;
        this.result = result;
        this.lcd_screen = lcd_screen;
        this.keypad_mode = keypad_mode;
        this.cfg_data = cfg_data;
    }

    //Преобразование полученного массива от сервера в картинку
    public void savePicture(){
        if (lcd_screen != null) {
            byte byteToFile[] = new byte[lcd_screen.length];
            for (int i = 0; i < lcd_screen.length; i++)
                byteToFile[i] = (byte) lcd_screen[i];
            try {
                FileOutputStream fos = new FileOutputStream(new File("reciveData\\tmpScreen.bmp"));
                fos.write(byteToFile);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //возвращаем значения на заданный массив полей конфига
    public List<String> getFieldsValue (List<ConfigFieldsEnum> configFieldsEnum){
        return cfg_data.getFieldsValue(configFieldsEnum);
    }
}
