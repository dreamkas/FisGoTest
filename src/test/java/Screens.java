import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by v.bochechko on 02.10.2017.
 * Этот класс возвращает строку из bmp-файдов (экранов с кассы)
 */

public class Screens {

    Screens () {
        screensInit();
    }

    public String passwodScreen;
    public String registrationScreen;
    public String registrationFiscalModeScreen;
    public String getRegistrationDataFromCabinetScreen;
    public String incorrectPasswodScreen;
    public String menuRegistrationScreen;
    public String wrongRegNumberScreen;
    public String emptyScreen;
    public String reRegistrationMenuScreen;
    public String reRegistrationMenuNotAutonomicScreen;
    public String documentNotSendedScreen;
    public String turnOffScreen;
    public String openShiftMenuScreen;
    public String shiftMenuOpenShiftScreen;
    public String lessMoneyInCashboxtScreen;
    public String freeSaleModeScreen;
    public String menuAfterPasswdScreen;

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

    private void screensInit() {
        passwodScreen = readScreenFile ("./screens/password.bmp");
        incorrectPasswodScreen = readScreenFile ("./screens/incorrectPassword.bmp");
        menuRegistrationScreen = readScreenFile ("./screens/menuRegistration.bmp");
        emptyScreen = readScreenFile ("./screens/emptyScreen.bmp");
        registrationFiscalModeScreen = readScreenFile ("./screens/fiscalModeRegistration.bmp");
        getRegistrationDataFromCabinetScreen = readScreenFile ("./screens/getRegistrationDataFromCabinet.bmp");
        reRegistrationMenuScreen = readScreenFile ("./screens/reRegistrationMenu.bmp");
        reRegistrationMenuNotAutonomicScreen = readScreenFile ("./screens/reRegistrationMenuNotAutonomic.bmp");
        documentNotSendedScreen = readScreenFile ("./screens/documentNotSended.bmp");
        turnOffScreen = readScreenFile("./screens/turnOff.bmp");
        openShiftMenuScreen = readScreenFile("./screens/openShiftMenu.bmp");
        shiftMenuOpenShiftScreen = readScreenFile("./screens/shiftMenuOpenShift.bmp");
        lessMoneyInCashboxtScreen = readScreenFile("./screens/lessMoneyInCashbox.bmp");
        freeSaleModeScreen = readScreenFile("./screens/freeSaleMode.bmp");
        menuAfterPasswdScreen = readScreenFile("./screens/menuAfterPasswd.bmp");
        wrongRegNumberScreen = readScreenFile("./screens/wrongRegNumber.bmp");;
    }
}
