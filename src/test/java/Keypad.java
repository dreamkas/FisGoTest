import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

/*
 * Класс инициализации кнопок
 */
public class Keypad {
    public Short key_code;
    public char key_number;
    public Vector<Integer> rus_code = new Vector<>() ;
    public Vector <Integer> eng_code = new Vector<>();
    public Vector <Integer> spec_sym_code = new Vector<>();
    char key_mode_available;

    public static int keys_table_size = 40;

    private KeypadMode keypadMode = new KeypadMode();

    //инициализация клавиатуры и режимов, в которых используются кнопки
    public void initKey(Keypad keys[]) {
        // Keypad[] keys = new Keypad[keypad.keys_table_size];
        //=======================================================================================================
        // KEY №0 - цифра 0 на клавиатуре
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[0].key_code = 35;
        else                     // Дримкас РФ
            keys[0].key_code = 0x18;
        // Доступ в режимах
        keys[0].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.SPEC_SYMBOLS);
        // Цифра
        keys[0].key_number = 0x30;
        // Специальные символы
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))         // НЕ(Антон, прочти это!) Дримкас Ф!
            keys[0].spec_sym_code.add( 0x20 );
        keys[0].spec_sym_code.add(0x40);
        keys[0].spec_sym_code.add(0x23);
        keys[0].spec_sym_code.add(0x24);
        keys[0].spec_sym_code.add(0x25);
        keys[0].spec_sym_code.add(0x26);
        keys[0].spec_sym_code.add(0x2A);
        //=======================================================================================================
        //=======================================================================================================
        // KEY №1 - цифра 1 на клавиатуре
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[1].key_code = 27;
        else                     // Дримкас РФ
            keys[1].key_code = 0x12;
        // Доступ в режимах
        keys[1].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[1].key_number = 0x31;
        // Русские заглавные
        keys[1].rus_code.add(0x94);
        keys[1].rus_code.add(0x95);
        keys[1].rus_code.add(0x96);
        keys[1].rus_code.add(0x97);
        // Руские прописные
        keys[1].rus_code.add(0xE4);
        keys[1].rus_code.add(0xE5);
        keys[1].rus_code.add(0xE6);
        keys[1].rus_code.add(0xE7);
        // Английские заглавные
        keys[1].eng_code.add(0x50);
        keys[1].eng_code.add(0x51);
        keys[1].eng_code.add(0x52);
        keys[1].eng_code.add(0x53);
        // Английские прописные
        keys[1].eng_code.add(0x70);
        keys[1].eng_code.add(0x71);
        keys[1].eng_code.add(0x72);
        keys[1].eng_code.add(0x73);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №2
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[2].key_code = 28;
        else                     // Дримкас РФ
            keys[2].key_code = 0x13;
        // Доступ в режимах
        keys[2].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[2].key_number = 0x32;

        // Русские заглавные
        keys[2].rus_code.add(0x98);
        keys[2].rus_code.add(0x99);
        keys[2].rus_code.add(0x9A);
        keys[2].rus_code.add(0x9B);
        // Руские прописные
        keys[2].rus_code.add(0xE8);
        keys[2].rus_code.add(0xE9);
        keys[2].rus_code.add(0xEA);
        keys[2].rus_code.add(0xEB);
        // Английские заглавные
        keys[2].eng_code.add(0x54);
        keys[2].eng_code.add(0x55);
        keys[2].eng_code.add(0x56);
        // Английские прописные
        keys[2].eng_code.add(0x74);
        keys[2].eng_code.add(0x75);
        keys[2].eng_code.add(0x76);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №3
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[3].key_code = 29;
        else                     // Дримкас РФ
            keys[3].key_code = 0x14;
        // Цифра
        keys[3].key_number = 0x33;

        // Доступ в режимах
        keys[3].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Русские заглавные
        keys[3].rus_code.add(0x9C);
        keys[3].rus_code.add(0x9D);
        keys[3].rus_code.add(0x9E);
        keys[3].rus_code.add(0x9F);
        // Руские прописные
        keys[3].rus_code.add(0xEC);
        keys[3].rus_code.add(0xED);
        keys[3].rus_code.add(0xEE);
        keys[3].rus_code.add(0xEF);
        // Английские заглавные
        keys[3].eng_code.add(0x57);
        keys[3].eng_code.add(0x58);
        keys[3].eng_code.add(0x59);
        keys[3].eng_code.add(0x5A);
        // Английские прописные
        keys[3].eng_code.add(0x77);
        keys[3].eng_code.add(0x78);
        keys[3].eng_code.add(0x79);
        keys[3].eng_code.add(0x7A);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №4
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[4].key_code = 19;
        else                     // Дримкас РФ
            keys[4].key_code = 0x0C;
        // Доступ в режимах
        keys[4].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[4].key_number = 0x34;

        // Русские заглавные
        keys[4].rus_code.add(0x88);
        keys[4].rus_code.add(0x89);
        keys[4].rus_code.add(0x8A);
        keys[4].rus_code.add(0x8B);
        // Руские прописные
        keys[4].rus_code.add(0xA8);
        keys[4].rus_code.add(0xA9);
        keys[4].rus_code.add(0xAA);
        keys[4].rus_code.add(0xAB);
        // Английские заглавные
        keys[4].eng_code.add(0x47);
        keys[4].eng_code.add(0x48);
        keys[4].eng_code.add(0x49);
        // Английские прописные
        keys[4].eng_code.add(0x67);
        keys[4].eng_code.add(0x68);
        keys[4].eng_code.add(0x69);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №5
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[5].key_code = 20;
        else                     // Дримкас РФ
            keys[5].key_code = 0x0D;
        // Доступ в режимах
        keys[5].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[5].key_number = 0x35;

        // Русские заглавные
        keys[5].rus_code.add(0x8C);
        keys[5].rus_code.add(0x8D);
        keys[5].rus_code.add(0x8E);
        keys[5].rus_code.add(0x8F);
        // Руские прописные
        keys[5].rus_code.add(0xAC);
        keys[5].rus_code.add(0xAD);
        keys[5].rus_code.add(0xAE);
        keys[5].rus_code.add(0xAF);
        // Английские заглавные
        keys[5].eng_code.add(0x4A);
        keys[5].eng_code.add(0x4B);
        keys[5].eng_code.add(0x4C);
        // Английские прописные
        keys[5].eng_code.add(0x6A);
        keys[5].eng_code.add(0x6B);
        keys[5].eng_code.add(0x6C);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №6
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[6].key_code = 21;
        else                     // Дримкас РФ
            keys[6].key_code = 0x0E;
        // Доступ в режимах
        keys[6].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[6].key_number = 0x36;

        // Русские заглавные
        keys[6].rus_code.add(0x90);
        keys[6].rus_code.add(0x91);
        keys[6].rus_code.add(0x92);
        keys[6].rus_code.add(0x93);
        // Руские прописные
        keys[6].rus_code.add(0xE0);
        keys[6].rus_code.add(0xE1);
        keys[6].rus_code.add(0xE2);
        keys[6].rus_code.add(0xE3);
        // Английские заглавные
        keys[6].eng_code.add(0x4D);
        keys[6].eng_code.add(0x4E);
        keys[6].eng_code.add(0x4F);
        // Английские прописные
        keys[6].eng_code.add(0x6D);
        keys[6].eng_code.add(0x6E);
        keys[6].eng_code.add(0x6F);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №7
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[7].key_code = 11;
        else                     // Дримкас РФ
            keys[7].key_code = 0x06;
        // Цифра
        keys[7].key_number = 0x37;

        // Доступ в режимах
        keys[7].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.SPEC_SYMBOLS);
        // Специальные символы
        keys[7].spec_sym_code.add(0x2E);
        keys[7].spec_sym_code.add(0x2C);
        keys[7].spec_sym_code.add(0x21);
        keys[7].spec_sym_code.add(0x3F);
        keys[7].spec_sym_code.add(0x28);
        keys[7].spec_sym_code.add(0x29);
        keys[7].spec_sym_code.add(0x3A);
        keys[7].spec_sym_code.add(0x3B);
        keys[7].spec_sym_code.add(0x27);
        keys[7].spec_sym_code.add(0x22);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №8
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[8].key_code = 12;
        else                     // Дримкас РФ
            keys[8].key_code = 0x07;
        // Доступ в режимах
        keys[8].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[8].key_number = 0x38;

        // Русские заглавные
        keys[8].rus_code.add(0x80);
        keys[8].rus_code.add(0x81);
        keys[8].rus_code.add(0x82);
        keys[8].rus_code.add(0x83);
        // Руские прописные
        keys[8].rus_code.add(0xA0);
        keys[8].rus_code.add(0xA1);
        keys[8].rus_code.add(0xA2);
        keys[8].rus_code.add(0xA3);
        // Английские заглавные
        keys[8].eng_code.add(0x41);
        keys[8].eng_code.add(0x42);
        keys[8].eng_code.add(0x43);
        // Английские прописные
        keys[8].eng_code.add(0x61);
        keys[8].eng_code.add(0x62);
        keys[8].eng_code.add(0x63);

        //=======================================================================================================
        //=======================================================================================================
        // KEY №9
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[9].key_code = 13;
        else                     // Дримкас РФ
            keys[9].key_code = 0x08;
        // Доступ в режимах
        keys[9].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.NUMBERS + keypadMode.CYRILLIC + keypadMode.ENGLISH);
        // Цифра
        keys[9].key_number = 0x39;

        // Русские заглавные
        keys[9].rus_code.add(0x84);
        keys[9].rus_code.add(0x85);
        keys[9].rus_code.add(0x86);
        keys[9].rus_code.add(0x87);
        // Руские прописные
        keys[9].rus_code.add(0xA4);
        keys[9].rus_code.add(0xA5);
        keys[9].rus_code.add(0xA6);
        keys[9].rus_code.add(0xA7);
        // Английские заглавные
        keys[9].eng_code.add(0x44);
        keys[9].eng_code.add(0x45);
        keys[9].eng_code.add(0x46);
        // Английские прописные
        keys[9].eng_code.add(0x64);
        keys[9].eng_code.add(0x65);
        keys[9].eng_code.add(0x66);

        //=======================================================================================================
        //=======================================================================================================
        // KEY ОТМЕНА
        keys[10].key_code = 0;
        // Доступ в режимах
        keys[10].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №1
        keys[11].key_code = 1;
        // Доступ в режимах
        keys[11].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №2
        keys[12].key_code = 2;
        // Доступ в режимах
        keys[12].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №3
        keys[13].key_code = 3;
        // Доступ в режимах
        keys[13].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №4
        keys[14].key_code = 4;
        // Доступ в режимах
        keys[14].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №5
        keys[15].key_code = 5;
        // Доступ в режимах
        keys[15].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        //=======================================================================================================
        // KEY №6
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[16].key_code = 6;
        else
            keys[16].key_code = 0x0a;
        // Доступ в режимах
        keys[16].key_mode_available = (char) keypadMode.ACTION_MODE;
        if (Config.CASHBOX_TYPE.equals("DreamkasF")) {
            //=======================================================================================================
            // KEY №7
            keys[17].key_code = 7;
            // Доступ в режимах
            keys[17].key_mode_available = (char) keypadMode.ACTION_MODE;
            //=======================================================================================================
            // KEY №8
            keys[18].key_code = 8;
            // Доступ в режимах
            keys[18].key_mode_available = (char) keypadMode.ACTION_MODE;
            //=======================================================================================================
        }
        // KEY №9
        keys[19].key_code = 9;
        // Доступ в режимах
        keys[19].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №10
        keys[20].key_code = 10;
        // Доступ в режимах
        keys[20].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY BACKSPACE
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[21].key_code = 14;
        else                     // Дримкас РФ
            keys[21].key_code = 0x09;

        // Доступ в режимах
        keys[21].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №15
        keys[22].key_code = 15;
        // Доступ в режимах
        keys[22].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №16
        keys[23].key_code = 16;
        // Доступ в режимах
        keys[23].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №17
        keys[24].key_code = 17;
        // Доступ в режимах
        keys[24].key_mode_available = (char) keypadMode.ACTION_MODE;
        if (Config.CASHBOX_TYPE.equals("DreamkasF")) {
            //=======================================================================================================
            // KEY №18
            keys[25].key_code = 18;
            // Доступ в режимах
            keys[25].key_mode_available = (char) keypadMode.ACTION_MODE;
            //=======================================================================================================
            // KEY №22
            keys[26].key_code = 22;
        }
        else                     // Дримкас РФ
            //=======================================================================================================
            // KEY №21
            keys[26].key_code = 0x15;
        // Доступ в режимах
        keys[26].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №23
        keys[27].key_code = 23;
        // Доступ в режимах
        keys[27].key_mode_available = (char) keypadMode.ACTION_MODE;
        if (Config.CASHBOX_TYPE.equals("DreamkasF")) {
            //=======================================================================================================
            // KEY
            keys[28].key_code = 24;
            // Доступ в режимах
            keys[28].key_mode_available = (char) keypadMode.ACTION_MODE;
        }
        //=======================================================================================================
        // KEY №25
        keys[29].key_code = 25;
        // Доступ в режимах
        keys[29].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №26
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[30].key_code = 26;
        else                     // Дримкас РФ
            keys[30].key_code = 27;
        // Доступ в режимах
        keys[30].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №30
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[31].key_code = 30;
        else                     // Дримкас РФ
            keys[31].key_code = 0x16;
        // Доступ в режимах
        keys[31].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №31
        keys[32].key_code = 31;
        // Доступ в режимах
        keys[32].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №32
        keys[33].key_code = 32;
        // Доступ в режимах
        keys[33].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №33
        keys[34].key_code = 33;
        // Доступ в режимах
        keys[34].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №34
        keys[35].key_code = 34;
        // Доступ в режимах
        keys[35].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY 00
        keys[36].key_code = 36;
        // Доступ в режимах
        keys[36].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY Comma
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[37].key_code = 37;
        else                     // Дримкас РФ
            keys[37].key_code = 0x1A;
        // Доступ в режимах
        keys[37].key_mode_available = (char) (keypadMode.FREE_MODE + keypadMode.SPEC_SYMBOLS + keypadMode.NUMBERS + keypadMode.ACTION_MODE);
        keys[37].key_number = 0x2C;
        keys[37].spec_sym_code.add(0x2B);
        keys[37].spec_sym_code.add(0x2D);
        keys[37].spec_sym_code.add(0x2F);
        keys[37].spec_sym_code.add(0x3D);
        keys[37].spec_sym_code.add(0x5E);
        keys[37].spec_sym_code.add(0x5F);
        keys[37].spec_sym_code.add(0x7B);
        keys[37].spec_sym_code.add(0x7D);
        keys[37].spec_sym_code.add(0xB3);
        keys[37].spec_sym_code.add(0x7E);
        //=======================================================================================================
        // KEY №38
        keys[38].key_code = 38;
        // Доступ в режимах
        keys[38].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================
        // KEY №39
        if (Config.CASHBOX_TYPE.equals("DreamkasF"))
            keys[39].key_code = 39;
        else
            keys[39].key_code = 0x1c;
        // Доступ в режимах
        keys[39].key_mode_available = (char) keypadMode.ACTION_MODE;
        //=======================================================================================================*//*
    }
}