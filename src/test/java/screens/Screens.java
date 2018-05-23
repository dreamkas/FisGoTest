package screens;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Этот класс возвращает строку из bmp-файдов (экранов с кассы)
 */

public class Screens {

    public Screens() {
        screensInit();
    }

    public String passwodScreen,
            registrationScreen,
            registrationFiscalModeScreen,
            getRegistrationDataFromCabinetScreen,
            incorrectPasswodScreen,
            menuRegistrationScreen,
            wrongRegNumberScreen,
            emptyScreen,
            reRegistrationMenuScreen,
            reRegistrationMenuNotAutonomicScreen,
            documentNotSendedScreen,
            turnOffScreen,
            openShiftMenuScreen,
            shiftMenuOpenShiftScreen,
            lessMoneyInCashboxtScreen,
            freeSaleModeScreen,
            menuAfterPasswdScreen,
            notEnoughMoneyScreen,
            freeSaleModeChange400Screen,
            giveCardAndReceiptScreen,
            consuptionReaultScreen_100Screen,
            incorrectCabinetCode,
            cabinetError,
            cabinetSuccesDisable,
            mainMenuRegistredModeScreen,
            menuSystem;

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
        wrongRegNumberScreen = readScreenFile("./screens/wrongRegNumber.bmp");
        notEnoughMoneyScreen = readScreenFile("./screens/notEnoughMoney.bmp");
        freeSaleModeChange400Screen = readScreenFile("./screens/freeSaleModeChange400.bmp");
        giveCardAndReceiptScreen = readScreenFile("./screens/giveCardAndReceipt.bmp");
        consuptionReaultScreen_100Screen = readScreenFile("./screens/consuptionReaultScreen_100.bmp");
        mainMenuRegistredModeScreen = readScreenFile("./screens/mainMenuRegistredMode.bmp");
        incorrectCabinetCode = readScreenFile("./screens/mainMenuRegistredMode.bmp");
        cabinetError = readScreenFile("./screens/cabinetError.bmp");
        cabinetSuccesDisable = readScreenFile("./screens/cabinetSuccesDisable.bmp");
        menuSystem = readScreenFile("./screens/menuSystem.bmp");
    }

    //Сравниваем экран на кассе с экраном из "базы"
    public boolean compareScreen(ScreenPicture action) {
        try {
            FileInputStream fstream = new FileInputStream("./reciveData/tmpScreen.bmp");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strFromFile = br.readLine();

            switch (action) {
                case PASSWORD:
                    return strFromFile.equals(passwodScreen);

                case INCORRECT_PASSWORD:
                    return strFromFile.equals(incorrectPasswodScreen);

                case AFTER_PASSWORD:
                    return strFromFile.equals(menuAfterPasswdScreen);

                case MENU_REGISTRATION:
                    return strFromFile.equals(menuRegistrationScreen);

                case WRONG_REG_NUMBER:
                    return strFromFile.equals(wrongRegNumberScreen);

                case REGISTRATION_FISCAL_MODE:
                    return strFromFile.equals(registrationFiscalModeScreen);

                case GET_REGISTRATION_DATA_FROM_CABINET:
                    return strFromFile.equals(getRegistrationDataFromCabinetScreen);

                case EMPTY_SCREEN:
                    return strFromFile.equals(emptyScreen);

                case RE_REGISTRATION_MENU:
                    return strFromFile.equals(reRegistrationMenuScreen);

                case RE_REGISTRATION_MENU_NOT_AUTONOMIC:
                    return strFromFile.equals(reRegistrationMenuNotAutonomicScreen);

                case DOCUMENT_NOT_SENDED_SCREEN:
                    return strFromFile.equals(documentNotSendedScreen);

                case TURN_OFF_CASHBOX:
                    return strFromFile.equals(turnOffScreen);

                case OPEN_SHIFT_MENU:
                    return strFromFile.equals(openShiftMenuScreen);

                case SHIFT_MENU_OPEN_SHIFT:
                    return strFromFile.equals(shiftMenuOpenShiftScreen);

                case LESS_MONEY_IN_CASHBOX:
                    return strFromFile.equals(lessMoneyInCashboxtScreen);

                case FREE_SALE_MODE:
                    return strFromFile.equals(freeSaleModeScreen);

                case NOT_ENOUGH_MONEY:
                    return strFromFile.equals(notEnoughMoneyScreen);

                case FREE_SALE_MODE_CHANGE_400:
                    return strFromFile.equals(freeSaleModeChange400Screen);

                case GIVE_CARD_AND_RECEIPT:
                    return strFromFile.equals(giveCardAndReceiptScreen);

                case CONSUMTION_RESULT_SCREEN_100:
                    return strFromFile.equals(consuptionReaultScreen_100Screen);

                case MAIN_MENU_REGISTRED_MODE_SCREEN:
                    return strFromFile.equals(mainMenuRegistredModeScreen);

                case INCORRECT_CABINET_CODE:
                    return strFromFile.equals(incorrectCabinetCode);

                case CABINET_ERROR:
                    return strFromFile.equals(cabinetError);

                case CABINET_SUCCES_DISABLE:
                    return strFromFile.equals(cabinetSuccesDisable);

                case MENU_SYSTEM:
                    return strFromFile.equals(menuSystem);

                default:
                    return false;
            }
        } catch (IOException e) {
            System.out.println("Ошибка сравнения экранов");
            e.printStackTrace();
        }
        return false;
    }
}
