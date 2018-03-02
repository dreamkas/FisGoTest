/*
 * Created by v.bochechko on 04.12.2017.
 */
public class KeyEnum {
    private Config config = new Config();

    public int keyCancel = 0,
            keyCheckInSms = 0,
            keyGoods = 0,
            keyMenu = 0,
            key7 = 0,
            key8 = 0,
            key9 = 0,
            keyReversal = 0,
            key4 = 0,
            key5 = 0,
            key6 = 0,
            keyPlus = 0,
            keyPayByCard = 0,
            keyUp = 0,
            key1 = 0,
            key2 = 0,
            key3 = 0,
            keyMinus = 0,
            keyPayByCash = 0,
            keyDown = 0,
            key0 = 0,
            key00 = 0,
            keyComma = 0,
            keyQuantity = 0,
            keyEnter = 0,
            keyPrintDocument = 50;

    public void initKeyEnum() {
        //если дримкас Ф
        if (config.getCashType() == 0) {
            keyCancel = 0x00;
            keyCheckInSms = 0x03;
            keyGoods = 0x06;
            keyMenu= 0x07;
            key7 = 0x0B;
            key8 = 0x0C;
            key9 = 0x0D;
            keyReversal = 0x0E;
            key4 = 0x13;
            key5 = 0x14;
            key6 = 0x15;
            keyPlus = 0x16;
            keyPayByCard = 0x17;
            keyUp = 0x18;
            key1 = 0x1B;
            key2 = 0x1C;
            key3 = 0x1D;
            keyMinus = 0x1E;
            keyPayByCash = 0x1F;
            keyDown = 0x20;
            key0 = 0x23;
            key00 = 0x24;
            keyComma = 0x25;
            keyQuantity = 0x26;
            keyEnter = 0x27;
        }

        //если дримкас РФ
        if (config.getCashType() == 1) {
            key1 = 0x12;
            key2 = 0x13;
            key3 = 0x14;
            key4 = 0x0C;
            key5 = 0x0D;
            key6 = 0x0E;
            key7 = 0x06;
            key8 = 0x07;
            key9 = 0x08;
            key0 = 0x18;
            key00 = 1212;
            //KEY_DIGIT        = 333,
            keyEnter = 0x1C;
            keyUp = 0x03;
            keyDown = 0x02;
            keyReversal = 0x09;
            keyCancel = 0x00;
            keyGoods = 0x0A;
            keyPayByCard = 0x10;
            keyPayByCash = 0x16;
            // KEY_CD_UP_DIR    = 888,
            keyComma = 0x1A;
            keyMenu = 0x04;
            //KEY_SPACE        = 99,
            keyCheckInSms = 555;
            keyPlus = 0x0F;
            keyMinus = 0x15;
            keyQuantity = 0x1B;
            // KEY_REPEAT       = 100,
            // KEY_COST_GET     = 101
        }
    }
}
