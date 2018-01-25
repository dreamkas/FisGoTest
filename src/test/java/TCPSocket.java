import sun.awt.windows.ThemeReader;

import java.io.*;
import java.net.HttpRetryException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static java.lang.Boolean.FALSE;

/**
 * Created by v.bochechko on 4.12.2017.
 */
public class TCPSocket {
    //*****************Работа с сокетом****************/
    private static String host;

    boolean flagTcpSocket = false;



    public boolean getFlagTcpSocket() {
        return flagTcpSocket;
    }
    public void setFlagTcpSocket(boolean flag) {
        flagTcpSocket = flag;
    }

    boolean flagGetScreen = false;
    public boolean getFlagGetScreen() {
        return flagGetScreen;
    }
    public void setFlagGetScreen(boolean flag) {
        flagGetScreen = flag;
    }
    ///*
    boolean flagKeypadMode = false;
    public boolean getFlagKeypadMode() {
        return flagKeypadMode;
    }
    public void setFlagKeypadMode(boolean flag) {
        flagKeypadMode = flag;
    }//*/
    boolean flagPressKey = false;
    public boolean getFlagPressKey() {
        return flagPressKey;
    }
    public void setFlagPressKey(boolean flag) {
        flagPressKey = flag;
    }

    private static short keypadMode = 0;
    public short getKeypadMode() {
        return keypadMode;
    }
    public void setKeypadMode(short data) {
        keypadMode = data;
    }

    private static boolean readAllInstruction = false;
    public boolean getReadAllInstruction() {
        return readAllInstruction;
    }
    public void setReadAllInstruction(boolean flag) {
        readAllInstruction = flag;
    }
    //*************************************************/

    //*****************Получаем даннные о нажатии***************/
    int keyNumber, pressCount;
    public void setKeyNumber(int keyNum) {
        keyNumber = keyNum;
        addKeyNumArray(keyNumber);
    }
    public void setPressCount(int pressCounts) {
        pressCount = pressCounts;
        addPressCountArray(pressCount);
    }
    public int getKeyNumber() {
        return keyNumber;
    }
    public int getPressCount() {
        return pressCount;
    }

    private ArrayList<Integer> keyNumArray = new ArrayList<Integer>();
    private ArrayList<Integer> pressCountArray = new ArrayList<Integer>();
    private static ArrayList<Integer> keyActionArray = new ArrayList<Integer>();

  //  private Controller controller = new Controller();

    public void clearArrayList () {
        keyNumArray.clear();
        pressCountArray.clear();
    }

    public void addKeyActionArray(int keyAction) {
        keyActionArray.add(keyAction);
    }
    private void addKeyNumArray(int keyNumber) {
        keyNumArray.add(getKeyNumber());
    }
    private void addPressCountArray(int pressCount) {
        pressCountArray.add(getPressCount());
    }

    //****************Формируем команды для передачи************/
    String HEADER = "FisGo", // Заголовок сообщений
            PROTOCOL = "1.1"; // Версия протокола
    int PACKET_NUM = 1,
            CMD = 1,
            LEN = 3,
            DATA = 0,
            CRC = 0;
    int HEADER_SIZE = 5,    // Длинна заголовка
            PROTOCOL_VER_SIZE = 3, // Длинна версии протокола
            PACKET_NUM_SIZE = 8, // Длинна номера пакета
            CMD_SIZE = 2, // Длинна команды
            LEN_SIZE = 2, // Размер поля длинны данных;
            CRC_SIZE = 2,
            DATA_SIZE = LEN;


    private static List<byte[]> commandArray = new ArrayList<byte[]>();
    //private static String strPrintDoc = new String();
    private void addDataToCommandArray(byte[] bytes) {
        commandArray.add(bytes);
    }

    private static boolean flagPause;
    private static int timePause;
    public void setFlagPause(boolean flag, int time) {
        flagPause = flag;
        timePause = time;
    }
    public boolean getFlagPause() {
        return flagPause;
    }

    //------------формирование данных------------/
    //в результате выполнения функции должны получить строку для отправки на сервер
    public byte[] createDataGetScreen(int PACKET_NUM_) throws UnsupportedEncodingException {
        PACKET_NUM = PACKET_NUM_;
        CMD = 2;
        LEN = 0;
        DATA_SIZE = 0;

        byte[] sendByte = new byte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + CRC_SIZE];

        StringBuilder tmpStrBldr = new StringBuilder();
        char[] tmp = HEADER.toCharArray();

        int a;
        for (int i = 0; i < tmp.length; i++) {
            a = (int) tmp[i];
            sendByte[i] = (byte) tmp[i]; //!!!!!!!!!
            tmpStrBldr.append(Integer.toHexString(a));
        }

        tmp = PROTOCOL.toCharArray();
        for (int i = 0; i < tmp.length; i++) {
            a = (int) tmp[i];
            tmpStrBldr.append(Integer.toHexString(a));
            sendByte[HEADER_SIZE + i] = (byte) tmp[i]; //!!!!!!!!!!
        }

        if (PACKET_NUM <= 0xFF) {
            for (int i = 0; i < PACKET_NUM_SIZE - 1; i++) {
                tmpStrBldr.append("00");
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + i] = Byte.parseByte("00");
            }
            if (PACKET_NUM <= 0x0F) {
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE - 1] = (byte) PACKET_NUM;//!!!!!!!!!!
            }
            if (PACKET_NUM > 0x0F) {
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE - 1] = (byte) PACKET_NUM;//!!!!!!!!!!
            }
        }
        if ((PACKET_NUM > 0xFF) && (PACKET_NUM <= 0xFFFF)) {
            for (int i = 0; i < PACKET_NUM_SIZE - 2; i++) {
                tmpStrBldr.append("00");
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + i] = Byte.parseByte("00");
            }
            if (PACKET_NUM <= 0x0FFF)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0x0FFF)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE - 2] = (byte) PACKET_NUM;
        }
        if ((PACKET_NUM > 0xFFFF) && (PACKET_NUM <= 0xFFFFFF)) {
            for (int i = 0; i < PACKET_NUM_SIZE - 3; i++)
                tmpStrBldr.append("00");
            if (PACKET_NUM <= 0x0FFFFF)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0x0FFFFF)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
        }
        if ((PACKET_NUM > 0xFFFFFF))//&& (PACKET_NUM <= 0xFFFFFFFF-1))
        {
            for (int i = 0; i < PACKET_NUM_SIZE - 4; i++)
                tmpStrBldr.append("00");
            if (PACKET_NUM <= 0xFFFFFFF)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0xFFFFFFF)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
        }

        ///************поле CMD**********
        tmpStrBldr.append("0" + Integer.toHexString(CMD));
        tmpStrBldr.append("00");
        sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE] = (byte) CMD;
        sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + 1] = Byte.parseByte("00");

        ///************поле LEN**********
        tmpStrBldr.append("0000");
        sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE] = (byte) LEN;
        sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + 1] = Byte.parseByte("00");


        String crcString = tmpStrBldr.toString();

        CRC = crc16_clc(crcString);
        // CRC = crc16_clc("00000000000000001"); // crc - длина 3 цифры
        // crcString = crcString + Integer.toHexString(CRC);

        ///**********************
        String crcStrHex = Integer.toHexString(CRC);
        //  System.out.println("crcStrHex = " + crcStrHex);
        //  System.out.println("CRC = " + CRC);

        if (CRC <= 0xFF) {
            if (CRC <= 0x0F)
                tmpStrBldr.append("0" + Integer.toHexString(CRC));
            if (CRC > 0x0F)
                tmpStrBldr.append(Integer.toHexString(CRC));
            tmpStrBldr.append("0");
            //  sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + 1] = Byte.parseByte("0");
            //  sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = (byte) CRC;

            if (crcStrHex.length() < 3){
                //    System.out.println("crcStrHex.length() < 3");
                String tmpCrcDiv = null;
                if (crcStrHex.length() == 2) {
                    tmpCrcDiv = crcStrHex.substring(crcStrHex.length() - 2, crcStrHex.length());
                }
                if (crcStrHex.length() == 1) {
                    tmpCrcDiv = crcStrHex.substring(crcStrHex.length() - 1, crcStrHex.length());
                }
                //   System.out.println("tmpCrcDiv " + tmpCrcDiv);
                int crcHexDiv = Integer.parseInt(tmpCrcDiv,16);
                //  System.out.println("crcHexDiv " +crcHexDiv);
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = Byte.parseByte("0");;
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + 1] = (byte) crcHexDiv;
            }
        }
        //  for (int i = 0; i <sendByte.length; i++ )
        ///     System.out.print(" " +sendByte[i]);
        // System.out.println();
        if ((CRC > 0xFF) && (CRC <= 0xFFFF)) {
            if (CRC <= 0x0FFF)
                tmpStrBldr.append("0" + Integer.toHexString(CRC));
            if (CRC > 0x0FFF)
                tmpStrBldr.append(Integer.toHexString(CRC));

            if ((crcStrHex.length() == 4) || (crcStrHex.length() == 3)) {
                String tmpCrcWhole = null;
                if (crcStrHex.length() == 4) {
                    tmpCrcWhole = crcStrHex.substring(0, 2);
                }

                if (crcStrHex.length() == 3) {
                    tmpCrcWhole = crcStrHex.substring(0, 1);
                }

                String tmpCrcDiv = crcStrHex.substring(crcStrHex.length() - 2, crcStrHex.length());
                //    System.out.println("tmpCrcWhole " + tmpCrcWhole);
                //  System.out.println("tmpCrcDiv " + tmpCrcDiv);
                int crcHexWhole = Integer.parseInt(tmpCrcWhole, 16);
                int crcHexDiv = Integer.parseInt(tmpCrcDiv, 16);
                //   System.out.println("crcHexWhole " + crcHexWhole);
                //        System.out.println("crcHexDiv " + crcHexDiv);
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = (byte) crcHexWhole;
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + 1] = (byte) crcHexDiv;
            }
        }

        // System.out.println("tmpStrBldr = " + tmpStrBldr);
        return sendByte;
    }

    public byte[] createData(int PACKET_NUM_) throws UnsupportedEncodingException {
        PACKET_NUM = PACKET_NUM_;
        if (getFlagKeypadMode())
            CMD = 3;
        else
            CMD = 4;
        LEN = 0;
        DATA_SIZE = 0;

        byte[] sendByte = new byte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + CRC_SIZE];

        StringBuilder tmpStrBldr = new StringBuilder();
        char[] tmp = HEADER.toCharArray();

        int a;
        for (int i = 0; i < tmp.length; i++) {
            a = (int) tmp[i];
            sendByte[i] = (byte) tmp[i]; //!!!!!!!!!
            tmpStrBldr.append(Integer.toHexString(a));
        }

        tmp = PROTOCOL.toCharArray();
        for (int i = 0; i < tmp.length; i++) {
            a = (int) tmp[i];
            tmpStrBldr.append(Integer.toHexString(a));
            sendByte[HEADER_SIZE + i] = (byte) tmp[i]; //!!!!!!!!!!
        }

        if (PACKET_NUM <= 0xFF) {
            for (int i = 0; i < PACKET_NUM_SIZE - 1; i++) {
                tmpStrBldr.append("00");
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + i] = Byte.parseByte("00");
            }
            if (PACKET_NUM <= 0x0F) {
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE - 1] = (byte) PACKET_NUM;//!!!!!!!!!!
            }
            if (PACKET_NUM > 0x0F) {
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE - 1] = (byte) PACKET_NUM;//!!!!!!!!!!
            }
        }
        if ((PACKET_NUM > 0xFF) && (PACKET_NUM <= 0xFFFF)) {
            for (int i = 0; i < PACKET_NUM_SIZE - 2; i++)
                tmpStrBldr.append("00");
            if (PACKET_NUM <= 0x0FFF)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0x0FFF)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
        }
        if ((PACKET_NUM > 0xFFFF) && (PACKET_NUM <= 0xFFFFFF)) {
            for (int i = 0; i < PACKET_NUM_SIZE - 3; i++)
                tmpStrBldr.append("00");
            if (PACKET_NUM <= 0x0FFFFF)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0x0FFFFF)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
        }
        if ((PACKET_NUM > 0xFFFFFF))//&& (PACKET_NUM <= 0xFFFFFFFF-1))
        {
            for (int i = 0; i < PACKET_NUM_SIZE - 4; i++)
                tmpStrBldr.append("00");
            if (PACKET_NUM <= 0xFFFFFFF)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0xFFFFFFF)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
        }

        ///************поле CMD**********
        tmpStrBldr.append("0" + Integer.toHexString(CMD));
        tmpStrBldr.append("00");
        sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE] = (byte) CMD;
        sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + 1] = Byte.parseByte("00");

        ///************поле LEN**********
        tmpStrBldr.append("0000");
        sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE] = (byte) LEN;
        sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + 1] = Byte.parseByte("00");


        String crcString = tmpStrBldr.toString();

        CRC = crc16_clc(crcString);
        // CRC = crc16_clc("00000000000000001"); // crc - длина 3 цифры
        // crcString = crcString + Integer.toHexString(CRC);

        ///**********************
        String crcStrHex = Integer.toHexString(CRC);
        //  System.out.println("crcStrHex = " + crcStrHex);
        //  System.out.println("CRC = " + CRC);

        if (CRC <= 0xFF) {
            if (CRC <= 0x0F)
                tmpStrBldr.append("0" + Integer.toHexString(CRC));
            if (CRC > 0x0F)
                tmpStrBldr.append(Integer.toHexString(CRC));
            tmpStrBldr.append("0");
            //  sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + 1] = Byte.parseByte("0");
            //  sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = (byte) CRC;

            if (crcStrHex.length() < 3){
                //    System.out.println("crcStrHex.length() < 3");
                String tmpCrcDiv = null;
                if (crcStrHex.length() == 2) {
                    tmpCrcDiv = crcStrHex.substring(crcStrHex.length() - 2, crcStrHex.length());
                }
                if (crcStrHex.length() == 1) {
                    tmpCrcDiv = crcStrHex.substring(crcStrHex.length() - 1, crcStrHex.length());
                }
                //   System.out.println("tmpCrcDiv " + tmpCrcDiv);
                int crcHexDiv = Integer.parseInt(tmpCrcDiv,16);
                //  System.out.println("crcHexDiv " +crcHexDiv);
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = Byte.parseByte("0");;
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + 1] = (byte) crcHexDiv;
            }
        }
        //  for (int i = 0; i <sendByte.length; i++ )
        ///     System.out.print(" " +sendByte[i]);

        if ((CRC > 0xFF) && (CRC <= 0xFFFF)) {
            if (CRC <= 0x0FFF)
                tmpStrBldr.append("0" + Integer.toHexString(CRC));
            if (CRC > 0x0FFF)
                tmpStrBldr.append(Integer.toHexString(CRC));

            if ((crcStrHex.length() == 4) || (crcStrHex.length() == 3)) {
                String tmpCrcWhole = null;
                if (crcStrHex.length() == 4) {
                    tmpCrcWhole = crcStrHex.substring(0, 2);
                }

                if (crcStrHex.length() == 3) {
                    tmpCrcWhole = crcStrHex.substring(0, 1);
                }

                String tmpCrcDiv = crcStrHex.substring(crcStrHex.length() - 2, crcStrHex.length());
                //    System.out.println("tmpCrcWhole " + tmpCrcWhole);
                //  System.out.println("tmpCrcDiv " + tmpCrcDiv);
                int crcHexWhole = Integer.parseInt(tmpCrcWhole, 16);
                int crcHexDiv = Integer.parseInt(tmpCrcDiv, 16);
                //      System.out.println("crcHexWhole " + crcHexWhole);
                //        System.out.println("crcHexDiv " + crcHexDiv);
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = (byte) crcHexWhole;
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + 1] = (byte) crcHexDiv;
            }
        }

       // System.out.println("tmpStrBldr = " + tmpStrBldr);
        return sendByte;
    }

    public byte[] createDataPressBtn(int PACKET_NUM_, int CMD_, int LEN_, int KEYNUM1_, int KEYNUM2_, int ACTION_ ) throws UnsupportedEncodingException {
        PACKET_NUM = PACKET_NUM_;
        CMD = CMD_;
        LEN = LEN_;
//        DATA = DATA_;
        DATA_SIZE = LEN_;
        byte[] sendByte = new byte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + CRC_SIZE];

        StringBuilder tmpStrBldr = new StringBuilder();
        char[] tmp = HEADER.toCharArray();

        int a;
        for (int i = 0; i < tmp.length; i++) {
            a = (int) tmp[i];
            sendByte[i] = (byte) tmp[i]; //!!!!!!!!!
            tmpStrBldr.append(Integer.toHexString(a));
        }

        tmp = PROTOCOL.toCharArray();
        for (int i = 0; i < tmp.length; i++) {
            a = (int) tmp[i];
            tmpStrBldr.append(Integer.toHexString(a));
            sendByte[HEADER_SIZE + i] = (byte) tmp[i]; //!!!!!!!!!!
        }

        if (PACKET_NUM <= 0xFF) {
            for (int i = 0; i < PACKET_NUM_SIZE - 1; i++) {
                tmpStrBldr.append("00");
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + i] = Byte.parseByte("00");
            }
            if (PACKET_NUM <= 0x0F)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0x0F)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE - 1] = (byte) PACKET_NUM;
        }
        if ((PACKET_NUM > 0xFF) && (PACKET_NUM <= 0xFFFF)) {
            for (int i = 0; i < PACKET_NUM_SIZE - 2; i++)
                tmpStrBldr.append("00");
            if (PACKET_NUM <= 0x0FFF)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0x0FFF)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE] = (byte) PACKET_NUM;
        }
        if ((PACKET_NUM > 0xFFFF) && (PACKET_NUM <= 0xFFFFFF)) {
            for (int i = 0; i < PACKET_NUM_SIZE - 3; i++)
                tmpStrBldr.append("00");
            if (PACKET_NUM <= 0x0FFFFF)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0x0FFFFF)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
        }
        if ((PACKET_NUM > 0xFFFFFF))//&& (PACKET_NUM <= 0xFFFFFFFF-1))
        {
            for (int i = 0; i < PACKET_NUM_SIZE - 4; i++)
                tmpStrBldr.append("00");
            if (PACKET_NUM <= 0xFFFFFFF)
                tmpStrBldr.append("0" + Integer.toHexString(PACKET_NUM));
            if (PACKET_NUM > 0xFFFFFFF)
                tmpStrBldr.append(Integer.toHexString(PACKET_NUM));
        }

        ///**********************
        if (CMD <= 0xFF) {
            if (CMD <= 0x0F) {
                tmpStrBldr.append("0" + Integer.toHexString(CMD));
                // sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE] = Byte.parseByte("0"); //!!!!!!!!!!
            }
            if (CMD > 0x0F)
                tmpStrBldr.append(Integer.toHexString(CMD));
            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE] = (byte) CMD;
            tmpStrBldr.append("00");
            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + 1] = Byte.parseByte("00");
        }
        if ((CMD > 0xFF) && (CMD <= 0xFFFF)) {
            if (CMD <= 0x0FFF)
                tmpStrBldr.append("0" + Integer.toHexString(CMD));
            if (CMD > 0x0FFF)
                tmpStrBldr.append(Integer.toHexString(CMD));
        }

        //******
        if (LEN <= 0xFF) {
            if (LEN <= 0x0F)
                tmpStrBldr.append("0" + Integer.toHexString(LEN));
            if (LEN > 0x0F)
                tmpStrBldr.append(Integer.toHexString(LEN));
            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE] = (byte) LEN;
            for (int i = 0; i < LEN_SIZE - 1; i++)
                tmpStrBldr.append("00");
            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + 1] = Byte.parseByte("00");
        }
        if ((LEN > 0xFF) && (LEN <= 0xFFFF)) {
            if (LEN <= 0x0FFF)
                tmpStrBldr.append("0" + Integer.toHexString(LEN));
            if (LEN > 0x0FFF)
                tmpStrBldr.append(Integer.toHexString(LEN));
            for (int i = 0; i < LEN_SIZE - 2; i++)
                tmpStrBldr.append("00");
        }
        if ((LEN > 0xFFFF) && (LEN <= 0xFFFFFF)) {
            if (LEN <= 0x0FFFFF)
                tmpStrBldr.append("0" + Integer.toHexString(LEN));
            if (LEN > 0x0FFFFF)
                tmpStrBldr.append(Integer.toHexString(LEN));
        }

        //*************
        if (DATA_SIZE > 0) {
            if (KEYNUM1_ <= 0x0F)
                tmpStrBldr.append("0" + Integer.toHexString(KEYNUM1_));
            if (KEYNUM1_ > 0x0F)
                tmpStrBldr.append(Integer.toHexString(KEYNUM1_));
            if (KEYNUM2_ <= 0x0F)
                tmpStrBldr.append("0" + Integer.toHexString(KEYNUM2_));
            if (KEYNUM2_ > 0x0F)
                tmpStrBldr.append(Integer.toHexString(KEYNUM2_));
            if (ACTION_ <= 0x0F)
                tmpStrBldr.append("0" + Integer.toHexString(ACTION_));
            if (ACTION_ > 0x0F)
                tmpStrBldr.append(Integer.toHexString(ACTION_));

            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE] = (byte) KEYNUM1_;
            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + 1] = (byte) KEYNUM2_;
            sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + 2] = (byte) ACTION_;
        }

        String crcString = tmpStrBldr.toString();

        CRC = crc16_clc(crcString);
        // CRC = crc16_clc("00000000000000001"); // crc - длина 3 цифры
        // crcString = crcString + Integer.toHexString(CRC);

        ///**********************
        String crcStrHex = Integer.toHexString(CRC);
        //  System.out.println("crcStrHex = " + crcStrHex);
        //  System.out.println("CRC = " + CRC);

        if (CRC <= 0xFF) {
            if (CRC <= 0x0F)
                tmpStrBldr.append("0" + Integer.toHexString(CRC));
            if (CRC > 0x0F)
                tmpStrBldr.append(Integer.toHexString(CRC));
            tmpStrBldr.append("0");
            //  sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + 1] = Byte.parseByte("0");
            //  sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = (byte) CRC;

            if (crcStrHex.length() < 3){
                //    System.out.println("crcStrHex.length() < 3");
                String tmpCrcDiv = null;
                if (crcStrHex.length() == 2) {
                    tmpCrcDiv = crcStrHex.substring(crcStrHex.length() - 2, crcStrHex.length());
                }
                if (crcStrHex.length() == 1) {
                    tmpCrcDiv = crcStrHex.substring(crcStrHex.length() - 1, crcStrHex.length());
                }
                //   System.out.println("tmpCrcDiv " + tmpCrcDiv);
                int crcHexDiv = Integer.parseInt(tmpCrcDiv,16);
                //  System.out.println("crcHexDiv " +crcHexDiv);
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = Byte.parseByte("0");;
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + 1] = (byte) crcHexDiv;
            }
        }
        //  for (int i = 0; i <sendByte.length; i++ )
        ///     System.out.print(" " +sendByte[i]);
        // System.out.println();
        if ((CRC > 0xFF) && (CRC <= 0xFFFF)) {
            if (CRC <= 0x0FFF)
                tmpStrBldr.append("0" + Integer.toHexString(CRC));
            if (CRC > 0x0FFF)
                tmpStrBldr.append(Integer.toHexString(CRC));

            if ((crcStrHex.length() == 4) || (crcStrHex.length() == 3)) {
                String tmpCrcWhole = null;
                if (crcStrHex.length() == 4)
                    tmpCrcWhole = crcStrHex.substring(0, 2);

                if (crcStrHex.length() == 3)
                    tmpCrcWhole = crcStrHex.substring(0, 1);

                String tmpCrcDiv = crcStrHex.substring(crcStrHex.length() - 2, crcStrHex.length());
                //    System.out.println("tmpCrcWhole " + tmpCrcWhole);
                //  System.out.println("tmpCrcDiv " + tmpCrcDiv);
                int crcHexWhole = Integer.parseInt(tmpCrcWhole, 16);
                int crcHexDiv = Integer.parseInt(tmpCrcDiv, 16);
                //      System.out.println("crcHexWhole " + crcHexWhole);
                //        System.out.println("crcHexDiv " + crcHexDiv);
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = (byte) crcHexWhole;
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE + 1] = (byte) crcHexDiv;
            }
        }

       // System.out.println("tmpStrBldr = " + tmpStrBldr);
        return sendByte;
    }

    private int crc16_clc(String inputStr) throws UnsupportedEncodingException {
        int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)

        boolean isHex = true;

        int strLen = inputStr.length();
        int[] intArray;

        if (isHex) {
            if (strLen % 2 != 0) {
                inputStr = inputStr.substring(0, strLen - 1) + "0"
                        + inputStr.substring(strLen - 1, strLen);
                strLen++;
            }

            intArray = new int[strLen / 2];
            int ctr = 0;
            for (int n = 0; n < strLen; n += 2) {
                intArray[ctr] = Integer.valueOf(inputStr.substring(n, n + 2), 16);
                ctr++;
            }
        } else {
            intArray = new int[inputStr.getBytes().length];
            int ctr = 0;
            for (byte b : inputStr.getBytes()) {
                intArray[ctr] = b;
                ctr++;
            }
        }

        for (int b : intArray) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        //System.out.println("CRC16-CCITT = " + Integer.toHexString(crc));

        return crc;
    }

    private static Socket workSocket;
    //466973476f312e31010000000000000003000000BA66
    public int createSocket(String host, int port) {
        try {
           // System.out.println("in create sock");
            // открываем сокет и коннектимся к IP кассы, порт 3245, получаем сокет сервера
            Socket socket = new Socket(host, port);
            workSocket = socket;
            GetScreenThread getScreenThread = new GetScreenThread(workSocket);
            getScreenThread.t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public class GetScreenThread  implements Runnable {

        Socket socket;
        Thread t;

        public GetScreenThread(Socket socket) { // через конструтор передадим параметр
            //  System.out.println("GetScreenThread 1");
            t = new Thread(this, "ScreenThread");
            this.socket = socket;            // передаём в конструктор все параметры, которые могут пигодится потоку
            // сохраняем параметры как поля
        }
        public void run() {
            try {
                int i = 0;
                //   System.out.println("****************in get screen thread***********************");
                // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();
                while (flagGetScreen) {
                    // передаем данные, читаем ответ
                    sout.write(createDataGetScreen(i));
                    byte buf[] = new byte[64 * 1024];
                    int r = sin.read(buf);
                    String data = new String(buf, 0, r);
                    byte tmp[] = new byte[r];
                    for (int k = 0; k < r; k++)
                        tmp[k] = buf[k];
                    if (data.indexOf("BM>") != -1)
                        savePicture(tmp);
                    i++;
                    if (i == 255) i = 0;
                    Thread.sleep(200);

                    if (getReadAllInstruction()) {
                        System.out.println("getReadAllInstruction = true");
                        for (int afterInst = 0; afterInst < 40; afterInst++) {
                            // передаем данные, читаем ответ
                            sout.write(createDataGetScreen(i));
                            byte bufAfter[] = new byte[64 * 1024];
                            int rAfter = sin.read(bufAfter);
                            //System.out.println("rAfter = " + rAfter);
                            String dataAfter = new String(bufAfter, 0, rAfter);
                            // System.out.println("dataAfter = " + dataAfter);
                            byte tmpAfter[] = new byte[rAfter];
                            for (int k = 0; k < rAfter; k++)
                                tmpAfter[k] = bufAfter[k];
                            if (dataAfter.indexOf("BM>") != -1)
                                savePicture(tmpAfter);

                            i++;
                            if (i == 255) i = 0;
                            Thread.sleep(200);
                        }
                        setFlagGetScreen(false);
                    }
                }
                sout.write(createData(i));
                socket.close();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    //**********************************************************/
    private void savePicture(byte byteRead[]) {
        // System.out.println("*in savePicture* - enter");
        byte byteToFile[] = new byte[1086];
        for (int i = 0; i < byteToFile.length; i++)
            byteToFile[i] = byteRead[i + HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE];
        try {FileOutputStream fos = new FileOutputStream(new File("reciveData\\tmpScreen.bmp"));
            fos.write(byteToFile);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //     System.out.println("savePicture exit");
        //controller.setUpdateImgFlag(true);
    }

    public void serverGetKepadMode() {
        try {
            //  System.out.println("sendData 1");
            GetKepadModeThread getKepadModeThread = new GetKepadModeThread(workSocket);
            getKepadModeThread.t.join();
            //System.out.println("senddata 3");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static int keypadSleep = 0;
    public void setKeypadSleep (int data) {
        keypadSleep = data;
    }
    public class GetKepadModeThread implements Runnable {

        Socket socket;
        Thread t;

        public GetKepadModeThread(Socket socket ) { // через конструтор передадим параметр
            t = new Thread(this, "GetKepadModeThread");
            this.socket = socket;           // передаём в конструктор все параметры, которые могут пигодится потоку сохраняем параметры как поля
            t.start();
            //  System.out.println("GetKepadModeThread 1");
        }

        public void run() {
            try {
                //  System.out.println("GetKepadModeThread 2");
                short i = 0;
                setKeypadMode(i);
                // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
                InputStream sin = workSocket.getInputStream();
                OutputStream sout = workSocket.getOutputStream();

                if (flagKeypadMode) {
                    if (keypadSleep != 0)
                        TimeUnit.SECONDS.sleep(keypadSleep);
                    //       System.out.println("GetKepadModeThread 3");
                    // передаем данные, читаем ответ
                    sout.write(createData(i));
                    byte bufClose[] = new byte[64 * 1024];
                    int rClose = sin.read(bufClose);
                    keypadMode = bufClose[rClose - 3] ;
                    String data = new String(bufClose, 0, rClose);
                    //System.out.println("keypadMode socket = " + keypadMode);
                    //System.out.println("data = " + data);
                    setFlagKeypadMode(false);
                    keypadSleep = 0;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private static boolean flagPauseEnter = true;
    public void setFlagPauseEnter (boolean flag) {
        flagPauseEnter = flag;
    }

    public void sendPressKey(int keyNum, int keyNum2, int pressAction) {
        try {
            setFlagPressKey(true);
            SendPressKeyThread sendPressKeyThread = new SendPressKeyThread(workSocket, keyNum, keyNum2, pressAction);
            sendPressKeyThread.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class SendPressKeyThread  implements Runnable {
        Socket socket;
        int keyNum;
        int keyNum2;
        int pressAction;
        Thread t;

        public SendPressKeyThread(Socket socket, int keyNum, int keyNum2, int pressAction) { // через конструтор передадим параметр
            ///System.out.println("SendPressKeyThread 1");
            t = new Thread(this, "PressKeyThread");
            t.start();
            this.socket = socket;           // передаём в конструктор все параметры, которые могут пигодится потоку сохраняем параметры как поля
            this.keyNum = keyNum;
            this.keyNum2 = keyNum2;
            this.pressAction = pressAction;
        }

        public void run() {
            try {
                //  System.out.println("flagPauseEnter = " +flagPauseEnter );
                TimeUnit.MILLISECONDS.sleep(300);
                if (flagPauseEnter) {
                 //   System.out.println("IN PAUSE");
                    TimeUnit.MILLISECONDS.sleep(500);
                    //System.out.println("with pause");
                }
                if (keypadSleep != 0)
                    TimeUnit.SECONDS.sleep(keypadSleep);
                //   System.out.println("SendPressKeyThread 2");
                int i = 0;
                // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
                InputStream sin = workSocket.getInputStream();
                OutputStream sout = workSocket.getOutputStream();

                if (flagPressKey) {
                    //     System.out.println("SendPressKeyThread 3");
                    // передаем данные, читаем ответ
                    sout.write(createDataPressBtn(i, 1, 3, keyNum, keyNum2, pressAction));
                    byte bufClose[] = new byte[64 * 1024];
                    int rClose = sin.read(bufClose);
                    String data = new String(bufClose, 0, rClose);
                //    System.out.println("data = " + data);
                    if (getFlagPause()) {
                     //   System.out.println("flagPause = true");
                        TimeUnit.SECONDS.sleep(timePause);
                    }
                    setFlagPressKey(false);
                    setFlagPause(false, 0);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }




    public void socketClose() {
        try {

            setFlagKeypadMode(false);
            createData(1);
            setFlagTcpSocket(false);
            setFlagGetScreen(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /******************************DEBUG**********************************************************/
    private void listPrint(List<byte[]> commandArray) {
        System.out.println("commandArray:");
        for (byte[] array : commandArray) {
            for (int x : array) {
                System.out.print(" " + x);
            }
            System.out.println();
        }
    }


}

