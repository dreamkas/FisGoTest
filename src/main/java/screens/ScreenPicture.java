package screens;

import lombok.Getter;

/**
 * Список возможных экранов с кассы
 */
public enum ScreenPicture {
    PASSWORD("screens/password.bmp"),
    INCORRECT_PASSWORD("screens/incorrectPassword.bmp"),
    AFTER_PASSWORD("screens/menuAfterPasswdScreen"),
    MENU_REGISTRATION("screens/menuRegistration.bmp"),
    WRONG_REG_NUMBER("screens/wrongRegNumberScreen.bmp"),
    REGISTRATION_FISCAL_MODE("screens/registrationFiscalModeScreen.bmp"),
    GET_REGISTRATION_DATA_FROM_CABINET("screens/getRegistrationDataFromCabinetScreen.bmp"),
    EMPTY_SCREEN("screens/emptyScreen.bmp"),
    RE_REGISTRATION_MENU("screens/reRegistrationMenuScreen.bmp"),
    RE_REGISTRATION_MENU_NOT_AUTONOMIC("screens/reRegistrationMenuNotAutonomicScreen.bmp"),
    DOCUMENT_NOT_SENDED_SCREEN("screens/documentNotSendedScreen.bmp"),
    TURN_OFF_CASHBOX("screens/turnOffScreen.bmp"),
    OPEN_SHIFT_MENU("screens/openShiftMenuScreen.bmp"),
    SHIFT_MENU_OPEN_SHIFT("screens/shiftMenuOpenShiftScreen.bmp"),
    LESS_MONEY_IN_CASHBOX("screens/lessMoneyInCashboxtScreen.bmp"),
    FREE_SALE_MODE("screens/freeSaleMode.bmp"),
    NOT_ENOUGH_MONEY("screens/notEnoughMoneyScreen.bmp"),
    FREE_SALE_MODE_CHANGE_400("screens/freeSaleModeChange400Screen.bmp"),
    GIVE_CARD_AND_RECEIPT("screens/giveCardAndReceiptScreen.bmp"),
    CONSUMTION_RESULT_SCREEN_100("screens/consuptionReaultScreen_100Screen.bmp"),
    MAIN_MENU_REGISTRED_MODE_SCREEN("screens/mainMenuRegistredModeScreen.bmp"),
    INCORRECT_CABINET_CODE("screens/incorrectCabinetCode.bmp"),
    CABINET_ERROR("screens/cabinetError.bmp"),
    CABINET_SUCCES_DISABLE("screens/cabinetSuccesDisable.bmp"),
    MENU_INTERNET("screens/menuWiFi.bmp"),
    SCREEN_SUCCES_CONNECT_ETHERNET("screens/screenSuccessConnectEthernet.bmp"),
    MENU_SYSTEM("screens/menuSystem.bmp");

    @Getter
    private final String path;

    ScreenPicture(String path) {
        this.path = path;
    }
}