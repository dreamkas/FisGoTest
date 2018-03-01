import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * Created by v.bochechko on 4.12.2017.
 * Класс работы с сокетом
 */

public class TCPSocket {
    //флаг получения экрана с кассы. true - получаем экран, false, соответственно нет
    //используется в потоке получения экрана
    private boolean flagReceiveScreen = false;
    private boolean getFlagReceiveScreen() {
        return flagReceiveScreen;
    }
    public void setFlagReceiveScreen(boolean flag) {
        flagReceiveScreen = flag;
    }
    //----------------------------------------------------------------------------
    //флаг получения режима ввода клавиатуры кассы. true - получить режим ввода, false, соответственно нет
    //используется в потоке получения режима работы клавиатуры
    private boolean flagKeypadMode = false;
    private boolean getFlagKeypadMode() {
        return flagKeypadMode;
    }
    public void setFlagKeypadMode(boolean flag) {
        flagKeypadMode = flag;
    }
    //----------------------------------------------------------------------------

    //флаг нажатия кнопки на кассе. true - нажать на кнопу
    //используется в потоке нажатия на кнопку
    private boolean flagPressKey = false;
    private boolean getFlagPressKey() {
        return flagPressKey;
    }
    private void setFlagPressKey(boolean flag) {
        flagPressKey = flag;
    }
    //----------------------------------------------------------------------------

    //сохранение режима ввода, полученного с кассы
    private static short keypadMode = 0;
    public short getKeypadMode() {
        return keypadMode;
    }
    private void setKeypadMode(short data) {
        keypadMode = data;
    }
    //----------------------------------------------------------------------------

    //данные для формирование команды, которая будет передана на сервер
    private String HEADER = "FisGo", // Заголовок сообщений
            PROTOCOL = "1.1"; // Версия протокола
    private int PACKET_NUM = 1,
            CMD = 1,
            LEN = 3,
            DATA = 0,
            CRC = 0;
    private int HEADER_SIZE = 5,    // Длинна заголовка
            PROTOCOL_VER_SIZE = 3, // Длинна версии протокола
            PACKET_NUM_SIZE = 8, // Длинна номера пакета
            CMD_SIZE = 2, // Длинна команды
            LEN_SIZE = 2, // Размер поля длинны данных;
            CRC_SIZE = 2,
            DATA_SIZE = LEN;
    //----------------------------------------------------------------------------

    //сокет для соединения с сервером
    private static Socket workSocket;

    //флаг паузы, поток спит на время timePause
    private static boolean flagPause;
    private static int timePause;
    public void setFlagPause(boolean flag, int time) {
        flagPause = flag;
        timePause = time;
    }
    private boolean getFlagPause() {
        return flagPause;
    }
    //----------------------------------------------------------------------------

    // открываем сокет, коннектимся к IP кассы, порт 3245 и получаем сокет сервера
    //запускается поток получения экрана
    public void createSocket(String host, int port) {
        try {
            workSocket = new Socket(host, port);
            GetScreenThread getScreenThread = new GetScreenThread(workSocket);
            getScreenThread.t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------

    //функция останавливает получение экрана с кассы и запускает поток получения режима ввода
    public void serverGetKepadMode() {
        try {
            GetKepadModeThread getKepadModeThread = new GetKepadModeThread(workSocket);
            getKepadModeThread.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------

    //функция запускает поток нажатия кнопки
    public void sendPressKey(int keyNum, int keyNum2, int pressAction) {
        try {
            setFlagPressKey(true);
            SendPressKeyThread sendPressKeyThread = new SendPressKeyThread(workSocket, keyNum, keyNum2, pressAction);
            sendPressKeyThread.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------

    //закрытие сокета
    public void socketClose() {
        try {
            setFlagReceiveScreen(false);
            setFlagKeypadMode(false);
            setFlagPressKey(false);
            // передаем команду закрытия соединения
            workSocket.getOutputStream().write(createData(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------

    //поток получения экрана с кассы
    public class GetScreenThread implements Runnable {
        Socket socket;
        Thread t;

        public GetScreenThread(Socket socket) { // через конструтор передадим параметр
            t = new Thread(this, "ScreenThread");
            this.socket = socket;            // передаём в конструктор все параметры, которые могут пигодится потоку, сохраняем параметры как поля
        }
        public void run() {
            try {
                System.out.println("t.isAlive() = " + t.isAlive());
                int i = 0;
                // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();
                while (getFlagReceiveScreen() && !socket.isClosed()) {
                    // передаем данные, читаем ответ
                    sout.write(createDataGetScreen(i));
                    byte buf[] = new byte[64 * 1024];
                    int r = sin.read(buf);
                    String data = new String(buf, 0, r);
                    if (data.contains("BM>"))
                        savePicture(buf);
                    i++;
                    if (i == 255) i = 0;
                    Thread.sleep(200);
                    if (getFlagKeypadMode() || getFlagPressKey())
                        Thread.sleep(1);
                }
                socket.close();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    //----------------------------------------------------------------------------

    //Преобразование полученного массива байт в картинку
    private void savePicture(byte byteRead[]) {
        byte byteToFile[] = new byte[1086];
        for (int i = 0; i < byteToFile.length; i++)
            byteToFile[i] = byteRead[i + HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE];
        try {FileOutputStream fos = new FileOutputStream(new File("reciveData\\tmpScreen.bmp"));
            fos.write(byteToFile);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------

    //поток получения режима ввода клавиатуры
    public class GetKepadModeThread implements Runnable {
        Socket socket;
        Thread t;

        public GetKepadModeThread(Socket socket ) { // через конструтор передадим параметр
            t = new Thread(this, "GetKepadModeThread");
            this.socket = socket;           // передаём в конструктор все параметры, которые могут пигодится потоку сохраняем параметры как поля
            t.start();
        }

        public void run() {
            try {
                short i = 0;
                setKeypadMode(i);
                // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();
                if (getFlagKeypadMode()) {
                    // передаем данные, читаем ответ
                    sout.write(createData(i));
                    byte bufClose[] = new byte[64 * 1024];
                    int rClose = sin.read(bufClose);
                    setKeypadMode(bufClose[rClose - 3]);
                    //  String data = new String(bufClose, 0, rClose);
                    //  System.out.println("keypadMode socket = " + keypadMode);
                    //  System.out.println("data = " + data);
                    setFlagKeypadMode(false);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    //----------------------------------------------------------------------------

    //поток нажатия на кнопку
    public class SendPressKeyThread  implements Runnable {
        Socket socket;
        int keyNum;
        int keyNum2;
        int pressAction;
        Thread t;

        public SendPressKeyThread(Socket socket, int keyNum, int keyNum2, int pressAction) { // через конструтор передадим параметр
            t = new Thread(this, "PressKeyThread");
            t.start();
            this.socket = socket;           // передаём в конструктор все параметры, которые могут пигодится потоку сохраняем параметры как поля
            this.keyNum = keyNum;
            this.keyNum2 = keyNum2;
            this.pressAction = pressAction;
        }

        public void run() {
            try {
                if (getFlagPressKey()) {
                    TimeUnit.MILLISECONDS.sleep(850);
                    int i = 0;
                    // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
                    InputStream sin = socket.getInputStream();
                    OutputStream sout = socket.getOutputStream();
                    // передаем данные, читаем ответ
                    sout.write(createDataPressBtn(i, 1, 3, keyNum, keyNum2, pressAction));
                    byte bufClose[] = new byte[64 * 1024];
                    int rClose = sin.read(bufClose);
                    //String data = new String(bufClose, 0, rClose);
                    //System.out.println("data = " + data);
                    if (getFlagPause()) {
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
    //----------------------------------------------------------------------------

    //------------формирование данных------------/
    //в результате выполнения функции должны получить строку для отправки на сервер
    //466973476f312e31010000000000000003000000BA66
    private byte[] createDataGetScreen(int PACKET_NUM_) throws UnsupportedEncodingException {
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
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = Byte.parseByte("0");
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

    private byte[] createData(int PACKET_NUM_) throws UnsupportedEncodingException {
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
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = Byte.parseByte("0");
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

    private byte[] createDataPressBtn(int PACKET_NUM_, int CMD_, int LEN_, int KEYNUM1_, int KEYNUM2_, int ACTION_ ) throws UnsupportedEncodingException {
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
                sendByte[HEADER_SIZE + PROTOCOL_VER_SIZE + PACKET_NUM_SIZE + CMD_SIZE + LEN_SIZE + DATA_SIZE] = Byte.parseByte("0");
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

    //------------------------DEBUG------------------------/
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
