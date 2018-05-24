package screens;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Этот класс возвращает строку из bmp-файдов (экранов с кассы)
 */

public class Screens {

    public boolean compareScreen(ScreenPicture expected, String screenString) {
        String strExpected = readScreenFile(expected.getPath());
        return strExpected.equals(screenString);
    }

    private String readScreenFile(String fileName) {
        try {
            FileInputStream fstream = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}
