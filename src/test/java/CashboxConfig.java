import com.google.gson.annotations.SerializedName;

import java.util.*;

/**
 * Created by v.bochechko on 10.04.2018.
 */
public class CashboxConfig {
    @SerializedName("CONFIG_VER")
    private String configVer;
    @SerializedName("KKT_MODE")
    private String kktMode;
    @SerializedName("TERMINAL_MODE")
    private String terminalMode;
    @SerializedName("FS_REPLACE_MODE")
    private String fsReplaceMode;
    @SerializedName("SHIFT_TIMER")
    private String shiftTimer;
    @SerializedName("FISGO_VERSION")
    private String fisgoVersion;
    @SerializedName("KKT_REG_VERSION")
    private String kktRegVersion;
    @SerializedName("ARTICLE")
    private String article;
    @SerializedName("UUID")
    private String uuid;
    @SerializedName("ETH_UP")
    private String ethUp;
    @SerializedName("IP_ADDRESS")
    private String ipAddress;
    @SerializedName("MASK")
    private String mask;
    @SerializedName("ROUTER")
    private String router;
    @SerializedName("WIFI_UP")
    private String wifiUp;
    @SerializedName("NET_WIFI_NAME")
    private String netWifiName;
    @SerializedName("NET_WIFI_KEY")
    private String netWifiKey;
    @SerializedName("NET_WIFI_SIGNAL")
    private String netWifiSignal;
    @SerializedName("NET_PASS")
    private String netPass;
    @SerializedName("FS_NUMBER")
    private String fsNumber;
    @SerializedName("FS_NUMBER_COUNT")
    private String fsNumberCount;
    @SerializedName("FS_NUMBERS_TABLE")
    private String fsNumberTable;
    @SerializedName("CASHIER_INN")
    private String cashierInn;
    @SerializedName("ORGANIZATION_NAME")
    private String organizationName;
    @SerializedName("CALCULATION_ADDRESS")
    private String calculationAddress;
    @SerializedName("CALCULATION_PLACE")
    private String calculationPlace;
    @SerializedName("ORGANIZATION_INN")
    private String organizationInn;
    @SerializedName("ORGANIZATION_KPP")
    private String organizationKpp;
    @SerializedName("KKT_REG_NUM")
    private String kktRegNum;
    @SerializedName("KKT_PLANT_NUM")
    private String kktPlantNum;
    @SerializedName("TAX_SYSTEMS")
    private String taxSystem;
    @SerializedName("CUR_TAX_SYSTEM")
    private String curTaxSystem;
    @SerializedName("ENCRYPTION_SIGN")
    private String encryptionSign;
    @SerializedName("EXCISABLE_SIGN")
    private String excisableSign;
    @SerializedName("CLC_SERVICE_SIGN")
    private String clcServiceSign;
    @SerializedName("GAMBLING_SIGN")
    private String gamblingSign;
    @SerializedName("LOTTERY_SIGN")
    private String lotterySign;
    @SerializedName("PAYING_AGENT_SIGN")
    private String payingAgentSign;
    @SerializedName("OFD_CHOOSE")
    private String ofdChoose;
    @SerializedName("OFD_INN")
    private String ofdInn;
    @SerializedName("OFD_SERVER_ADDRESS")
    private String ofdServerAddress;
    @SerializedName("OFD_NAME")
    private String ofdName;
    @SerializedName("OFD_SERVER_PORT")
    private String ofdServerPort;
    @SerializedName("CHECK_RECEIPT_ADDRESS")
    private String checkReceiptAddress;
    @SerializedName("OFD_SERVER_IP")
    private String ofdServerIp;
    @SerializedName("OPEN_SHIFT_DATE")
    private String openShiftDate;
    @SerializedName("AGENT_MASK")
    private String agentMask;
    @SerializedName("CURRENT_AGENT")
    private String currentAgent;
    @SerializedName("LOG_TYPE")
    private String logType;
    @SerializedName("IS_CABINET_ENABLE")
    private String isCabinetEnable;
    @SerializedName("START_TIME")
    private String startTime;
    @SerializedName("LAST_MSG_OFD")
    private String lastMsgOfd;
    @SerializedName("NDS_FREE_PRICE")
    private String ndsFreePrice;
    @SerializedName("SCALE_TYPE")
    private String scaleType;
    @SerializedName("SCALE_SPEED")
    private String scaleSpeed;
    @SerializedName("SCALE_PORT_NAME")
    private String scalePortName;
    @SerializedName("SCALE_OPTIONS")
    private String scaleOption;
    @SerializedName("SCANNER_TYPE")
    private String scannerType;
    @SerializedName("SCANNER_SPEED")
    private String scannerSpeed;
    @SerializedName("SCANNER_PORT_NAME")
    private String scannerPortName;
    @SerializedName("SCANNER_OPTIONS")
    private String scannerOption;
    @SerializedName("DNS_MAIN")
    private String dnsMain;
    @SerializedName("DNS_ADD")
    private String dnsAdd;
    @SerializedName("ETH_MANUAL")
    private String ethManual;
    @SerializedName("AUTO_CASH_OUT")
    private String autoCashOut;
    @SerializedName("FFD_KKT_VER")
    private String ffdKktver;
    @SerializedName("BSO")
    private String bso;
    @SerializedName("KKT_SIGNS")
    private String kktSign;
    @SerializedName("ADD_KKT_SIGNS")
    private String addKktSign;
    @SerializedName("AUTO_PRINTER_NUM")
    private String autoPrinterNum;
    @SerializedName("AUTO_OPEN_SHIFT")
    private String autoOpenShift;
    @SerializedName("STAGE")
    private String stage;
    @SerializedName("INTERNET_RECIEPT")
    private String internetReciept;
    @SerializedName("CONNECT_TO")
    private String connectTo;
    @SerializedName("CLOUD_REG_STATUS")
    private String cloudRegStatus;
    @SerializedName("CLOUD_TEL")
    private String cloudTel;
    @SerializedName("CLOUD_PIN")
    private String cloudPin;
    @SerializedName("CLOUD_ORG_FNS_NUM")
    private String cloudOrgFnsNum;
    @SerializedName("CLOUD_KKT_FNS_NUM")
    private String cloudKktFnsNum;
    @SerializedName("CABINET_REG_EMAIL")
    private String cabinetRegEmail;
    //флаги биоса
    @SerializedName("LAST_FD_NUM")
    private String lastFdNum;
    @SerializedName("SHIFT_NUM")
    private String shiftNum;
    @SerializedName("RECEIPT_NUM")
    private String receiptNum;
    @SerializedName("IS_SHIFT_OPEN")
    private String isShiftOpen;
    @SerializedName("IS_READY_TO_FISCAL")
    private String isReadyToFiscal;
    @SerializedName("IS_FISCAL_MODE")
    private String isFiscalMode;
    @SerializedName("IS_POST_FISCAL_MODE")
    private String isPostFiscalMode;
    @SerializedName("IS_DOC_OPEN")
    private String isDocOpen;
    @SerializedName("IS_IMMEDIATE_REPLACEMENT")
    private String isImmediateReplacement;
    @SerializedName("IS_RESOURCES_EXHAUSTION")
    private String isResourcesExhaustiom;
    @SerializedName("IS_MEMORY_OVERFLOW")
    private String isMemoryOverflow;
    @SerializedName("UNDELEGATED_DOCS_CNT")
    private String undelegatedDocCnt;
    @SerializedName("FIRST_UNDELEGATED_DOC")
    private String firstUndelegatedDoc;
    @SerializedName("DATE_OF_FIRST_UNDELEGATED_DOC")
    private String dateOfFirstUndelegatedDoc;
    @SerializedName("OFD_DISCONNECT_05D")
    private String ofdDisconnect05d;
    @SerializedName("OFD_DISCONNECT_20D")
    private String ofdDisconnect20d;
    @SerializedName("OFD_DISCONNECT_30D")
    private String ofdDisconnect30d;
    @SerializedName("IS_SHIFT_24H")
    private String isShift24h;
    @SerializedName("REG_DATA_CHANGE")
    private String regDataChange;
    @SerializedName("OFD_DATA_CHANGE")
    private String ofdDataChange;
    @SerializedName("FS_CHANGE_KKT_MODE")
    private String fsChangeKktmode;


    private Map<String,String> configMap = new HashMap<>();

    public CashboxConfig(String CONFIG_VER, String KKT_MODE, String TERMINAL_MODE, String FS_REPLACE_MODE,
                         String SHIFT_TIMER, String FISGO_VERSION, String KKT_REG_VERSION, String ARTICLE, String UUID,
                         String ETH_UP, String IP_ADDRESS, String MASK, String ROUTER, String WIFI_UP, String NET_WIFI_NAME,
                         String NET_WIFI_KEY, String NET_WIFI_SIGNAL, String NET_PASS, String FS_NUMBER, String FS_NUMBER_COUNT,
                         String FS_NUMBERS_TABLE, String CASHIER_INN, String ORGANIZATION_NAME, String CALCULATION_ADDRESS,
                         String CALCULATION_PLACE, String ORGANIZATION_INN, String ORGANIZATION_KPP, String KKT_REG_NUM,
                         String KKT_PLANT_NUM, String TAX_SYSTEMS, String CUR_TAX_SYSTEM, String ENCRYPTION_SIGN,
                         String EXCISABLE_SIGN, String CLC_SERVICE_SIGN, String GAMBLING_SIGN, String LOTTERY_SIGN,
                         String PAYING_AGENT_SIGN, String OFD_CHOOSE, String OFD_INN, String OFD_SERVER_ADDRESS,
                         String OFD_NAME, String OFD_SERVER_PORT, String CHECK_RECEIPT_ADDRESS, String OFD_SERVER_IP,
                         String OPEN_SHIFT_DATE, String AGENT_MASK, String CURRENT_AGENT, String LOG_TYPE,
                         String IS_CABINET_ENABLE, String START_TIME, String LAST_MSG_OFD, String NDS_FREE_PRICE,
                         String SCALE_TYPE, String SCALE_SPEED, String SCALE_PORT_NAME, String SCALE_OPTIONS,
                         String SCANNER_TYPE, String SCANNER_SPEED, String SCANNER_PORT_NAME, String SCANNER_OPTIONS,
                         String DNS_MAIN, String DNS_ADD, String ETH_MANUAL, String AUTO_CASH_OUT, String FFD_KKT_VER,
                         String BSO, String KKT_SIGNS, String ADD_KKT_SIGNS, String AUTO_PRINTER_NUM, String AUTO_OPEN_SHIFT,
                         String STAGE, String INTERNET_RECIEPT, String CONNECT_TO, String CLOUD_REG_STATUS, String CLOUD_TEL,
                         String CLOUD_PIN, String CLOUD_ORG_FNS_NUM, String CLOUD_KKT_FNS_NUM, String CABINET_REG_EMAIL,
                         String LAST_FD_NUM, String SHIFT_NUM, String RECEIPT_NUM, String IS_SHIFT_OPEN, String IS_READY_TO_FISCAL,
                         String IS_FISCAL_MODE, String IS_POST_FISCAL_MODE, String IS_DOC_OPEN, String IS_IMMEDIATE_REPLACEMENT,
                         String IS_RESOURCES_EXHAUSTION, String IS_MEMORY_OVERFLOW, String UNDELEGATED_DOCS_CNT,
                         String FIRST_UNDELEGATED_DOC, String DATE_OF_FIRST_UNDELEGATED_DOC, String OFD_DISCONNECT_05D,
                         String OFD_DISCONNECT_20D, String OFD_DISCONNECT_30D, String IS_SHIFT_24H,
                         String REG_DATA_CHANGE, String OFD_DATA_CHANGE, String FS_CHANGE_KKT_MODE) {

        this.configVer = CONFIG_VER;
        this.kktMode = KKT_MODE;
        this.terminalMode = TERMINAL_MODE;
        this.fsReplaceMode = FS_REPLACE_MODE;
        this.shiftTimer = SHIFT_TIMER;
        this.fisgoVersion = FISGO_VERSION;
        this.kktRegVersion = KKT_REG_VERSION;
        this.article = ARTICLE;
        this.uuid = UUID;
        this.ethUp = ETH_UP;
        this.ipAddress = IP_ADDRESS;
        this.mask = MASK;
        this.router = ROUTER;
        this.wifiUp = WIFI_UP;
        this.netWifiName = NET_WIFI_NAME;
        this.netWifiKey = NET_WIFI_KEY;
        this.netWifiSignal = NET_WIFI_SIGNAL;
        this.netPass = NET_PASS;
        this.fsNumber = FS_NUMBER;
        this.fsNumberCount = FS_NUMBER_COUNT;
        this.fsNumberTable = FS_NUMBERS_TABLE;
        this.cashierInn = CASHIER_INN;
        this.organizationName = ORGANIZATION_NAME;
        this.calculationAddress = CALCULATION_ADDRESS;
        this.calculationPlace = CALCULATION_PLACE;
        this.organizationInn = ORGANIZATION_INN;
        this.organizationKpp = ORGANIZATION_KPP;
        this.kktRegNum = KKT_REG_NUM;
        this.kktPlantNum = KKT_PLANT_NUM;
        this.taxSystem = TAX_SYSTEMS;
        this.curTaxSystem = CUR_TAX_SYSTEM;
        this.encryptionSign = ENCRYPTION_SIGN;
        this.excisableSign = EXCISABLE_SIGN;
        this.clcServiceSign = CLC_SERVICE_SIGN;
        this.gamblingSign = GAMBLING_SIGN;
        this.lotterySign = LOTTERY_SIGN;
        this.payingAgentSign = PAYING_AGENT_SIGN;
        this.ofdChoose = OFD_CHOOSE;
        this.ofdInn = OFD_INN;
        this.ofdServerAddress = OFD_SERVER_ADDRESS;
        this.ofdName = OFD_NAME;
        this.ofdServerPort = OFD_SERVER_PORT;
        this.checkReceiptAddress = CHECK_RECEIPT_ADDRESS;
        this.ofdServerIp = OFD_SERVER_IP;
        this.openShiftDate = OPEN_SHIFT_DATE;
        this.agentMask = AGENT_MASK;
        this.currentAgent = CURRENT_AGENT;
        this.logType = LOG_TYPE;
        this.isCabinetEnable = IS_CABINET_ENABLE;
        this.startTime = START_TIME;
        this.lastMsgOfd = LAST_MSG_OFD;
        this.ndsFreePrice = NDS_FREE_PRICE;
        this.scaleType = SCALE_TYPE;
        this.scaleSpeed = SCALE_SPEED;
        this.scalePortName = SCALE_PORT_NAME;
        this.scaleOption = SCALE_OPTIONS;
        this.scannerType = SCANNER_TYPE;
        this.scannerSpeed = SCANNER_SPEED;
        this.scannerPortName = SCANNER_PORT_NAME;
        this.scannerOption = SCANNER_OPTIONS;
        this.dnsMain = DNS_MAIN;
        this.dnsAdd = DNS_ADD;
        this.ethManual = ETH_MANUAL;
        this.autoCashOut = AUTO_CASH_OUT;
        this.ffdKktver = FFD_KKT_VER;
        this.bso = BSO;
        this.kktSign = KKT_SIGNS;
        this.addKktSign = ADD_KKT_SIGNS;
        this.autoPrinterNum = AUTO_PRINTER_NUM;
        this.autoOpenShift = AUTO_OPEN_SHIFT;
        this.stage = STAGE;
        this.internetReciept = INTERNET_RECIEPT;
        this.connectTo = CONNECT_TO;
        this.cloudRegStatus = CLOUD_REG_STATUS;
        this.cloudTel = CLOUD_TEL;
        this.cloudPin = CLOUD_PIN;
        this.cloudOrgFnsNum = CLOUD_ORG_FNS_NUM;
        this.cloudKktFnsNum = CLOUD_KKT_FNS_NUM;
        this.cabinetRegEmail = CABINET_REG_EMAIL;
        //флаги биоса
        this.lastFdNum = LAST_FD_NUM;
        this.shiftNum = SHIFT_NUM;
        this.receiptNum = RECEIPT_NUM;
        this.isShiftOpen = IS_SHIFT_OPEN;
        this.isReadyToFiscal = IS_READY_TO_FISCAL;
        this.isFiscalMode = IS_FISCAL_MODE;
        this.isPostFiscalMode = IS_POST_FISCAL_MODE;
        this.isDocOpen = IS_DOC_OPEN;
        this.isImmediateReplacement = IS_IMMEDIATE_REPLACEMENT;
        this.isResourcesExhaustiom = IS_RESOURCES_EXHAUSTION;
        this.isMemoryOverflow = IS_MEMORY_OVERFLOW;
        this.undelegatedDocCnt = UNDELEGATED_DOCS_CNT;
        this.firstUndelegatedDoc = FIRST_UNDELEGATED_DOC;
        this.dateOfFirstUndelegatedDoc = DATE_OF_FIRST_UNDELEGATED_DOC;
        this.ofdDisconnect05d = OFD_DISCONNECT_05D;
        this.ofdDisconnect20d = OFD_DISCONNECT_20D;
        this.ofdDisconnect30d = OFD_DISCONNECT_30D;
        this.isShift24h = IS_SHIFT_24H;
        this.regDataChange = REG_DATA_CHANGE;
        this.ofdDataChange = OFD_DATA_CHANGE;
        this.fsChangeKktmode = FS_CHANGE_KKT_MODE;
    }






    // Функция для получения значения полей
    public List<String> getFieldsValue (List<ConfigFieldsEnum> configFieldsEnum){
        System.out.println("CashboxConfig getFieldsValue");
        for (ConfigFieldsEnum str: configFieldsEnum) {
            System.out.println(String.valueOf(str));
        }

        List<String> valueConfigFields = new ArrayList<>();

        for (ConfigFieldsEnum configEnum: configFieldsEnum) {
            switch (String.valueOf(configEnum)) {
                case "CONFIG_VER":
                    valueConfigFields.add(configVer);
                    break;
                case "KKT_MODE":
                    valueConfigFields.add(kktMode);
                    break;
                case "TERMINAL_MODE":
                    valueConfigFields.add(terminalMode);
                    break;
                case "FS_REPLACE_MODE":
                    valueConfigFields.add(fsReplaceMode);
                    break;
                case "SHIFT_TIMER":
                    valueConfigFields.add(shiftTimer);
                    break;
                case "FISGO_VERSION":
                    valueConfigFields.add(fisgoVersion);
                    break;
                case "KKT_REG_VERSION":
                    valueConfigFields.add(kktRegVersion);
                    break;
                case "ARTICLE":
                    valueConfigFields.add(article);
                    break;
                case "UUID":
                    valueConfigFields.add(uuid);
                    break;
                case "ETH_UP":
                    valueConfigFields.add(ethUp);
                    break;
                case "IP_ADDRESS":
                    valueConfigFields.add(ipAddress);
                    break;
                case "MASK":
                    valueConfigFields.add(mask);
                    break;
                case "ROUTER":
                    valueConfigFields.add(router);
                    break;
                case "WIFI_UP":
                    valueConfigFields.add(wifiUp);
                    break;
                case "NET_WIFI_NAME":
                    valueConfigFields.add(netWifiName);
                    break;
                case "NET_WIFI_KEY":
                    valueConfigFields.add(netWifiKey);
                    break;
                case "NET_WIFI_SIGNAL":
                    valueConfigFields.add(netWifiSignal);
                    break;
                case "NET_PASS":
                    valueConfigFields.add(netPass);
                    break;
                case "FS_NUMBER":
                    valueConfigFields.add(fsNumber);
                    break;
                case "FS_NUMBER_COUNT":
                    valueConfigFields.add(fsNumberCount);
                    break;
                case "FS_NUMBERS_TABLE":
                    valueConfigFields.add(fsNumberTable);
                    break;
                case "CASHIER_INN":
                    valueConfigFields.add(cashierInn);
                    break;
                case "ORGANIZATION_NAME":
                    valueConfigFields.add(organizationName);
                    break;
                case "CALCULATION_ADDRESS":
                    valueConfigFields.add(calculationAddress);
                    break;
                case "CALCULATION_PLACE":
                    valueConfigFields.add(calculationPlace);
                    break;
                case "ORGANIZATION_INN":
                    valueConfigFields.add(organizationInn);
                    break;
                case "ORGANIZATION_KPP":
                    valueConfigFields.add(organizationKpp);
                    break;
                case "KKT_REG_NUM":
                    valueConfigFields.add(kktRegNum);
                    break;
                case "KKT_PLANT_NUM":
                    valueConfigFields.add(kktPlantNum);
                    break;
                case "TAX_SYSTEMS":
                    valueConfigFields.add(taxSystem);
                    break;
                case "CUR_TAX_SYSTEM":
                    valueConfigFields.add(curTaxSystem);
                    break;
                case "ENCRYPTION_SIGN":
                    valueConfigFields.add(encryptionSign);
                    break;
                case "EXCISABLE_SIGN":
                    valueConfigFields.add(excisableSign);
                    break;
                case "CLC_SERVICE_SIGN":
                    valueConfigFields.add(clcServiceSign);
                    break;
                case "GAMBLING_SIGN":
                    valueConfigFields.add(gamblingSign);
                    break;
                case "LOTTERY_SIGN":
                    valueConfigFields.add(lotterySign);
                    break;
                case "PAYING_AGENT_SIGN":
                    valueConfigFields.add(payingAgentSign);
                    break;
                case "OFD_CHOOSE":
                    valueConfigFields.add(ofdChoose);
                    break;
                case "OFD_INN":
                    valueConfigFields.add(ofdInn);
                    break;
                case "OFD_SERVER_ADDRESS":
                    valueConfigFields.add(ofdServerAddress);
                    break;
                case "OFD_NAME":
                    valueConfigFields.add(ofdName);
                    break;
                case "OFD_SERVER_PORT":
                    valueConfigFields.add(ofdServerPort);
                    break;
                case "CHECK_RECEIPT_ADDRESS":
                    valueConfigFields.add(checkReceiptAddress);
                    break;
                case "OFD_SERVER_IP":
                    valueConfigFields.add(ofdServerIp);
                    break;
                case "OPEN_SHIFT_DATE":
                    valueConfigFields.add(openShiftDate);
                    break;
                case "AGENT_MASK":
                    valueConfigFields.add(agentMask);
                    break;
                case "CURRENT_AGENT":
                    valueConfigFields.add(currentAgent);
                    break;
                case "LOG_TYPE":
                    valueConfigFields.add(logType);
                    break;
                case "IS_CABINET_ENABLE":
                    valueConfigFields.add(isCabinetEnable);
                    break;
                case "START_TIME":
                    valueConfigFields.add(startTime);
                    break;
                case "LAST_MSG_OFD":
                    valueConfigFields.add(lastMsgOfd);
                    break;
                case "NDS_FREE_PRICE":
                    valueConfigFields.add(ndsFreePrice);
                    break;
                case "SCALE_TYPE":
                    valueConfigFields.add(scaleType);
                    break;
                case "SCALE_SPEED":
                    valueConfigFields.add(scaleSpeed);
                    break;
                case "SCALE_PORT_NAME":
                    valueConfigFields.add(scalePortName);
                    break;
                case "SCALE_OPTIONS":
                    valueConfigFields.add(scaleOption);
                    break;
                case "SCANNER_TYPE":
                    valueConfigFields.add(scannerType);
                    break;
                case "SCANNER_SPEED":
                    valueConfigFields.add(scannerSpeed);
                    break;
                case "SCANNER_PORT_NAME":
                    valueConfigFields.add(scalePortName);
                    break;
                case "SCANNER_OPTIONS":
                    valueConfigFields.add(scannerOption);
                    break;
                case "DNS_MAIN":
                    valueConfigFields.add(dnsMain);
                    break;
                case "DNS_ADD":
                    valueConfigFields.add(dnsAdd);
                    break;
                case "ETH_MANUAL":
                    valueConfigFields.add(ethManual);
                    break;
                case "AUTO_CASH_OUT":
                    valueConfigFields.add(autoCashOut);
                    break;
                case "FFD_KKT_VER":
                    valueConfigFields.add(ffdKktver);
                    break;
                case "BSO":
                    valueConfigFields.add(bso);
                    break;
                case "KKT_SIGNS":
                    valueConfigFields.add(kktSign);
                    break;
                case "ADD_KKT_SIGNS":
                    valueConfigFields.add(addKktSign);
                    break;
                case "AUTO_PRINTER_NUM":
                    valueConfigFields.add(autoPrinterNum);
                    break;
                case "AUTO_OPEN_SHIFT":
                    valueConfigFields.add(autoOpenShift);
                    break;
                case "STAGE":
                    valueConfigFields.add(stage);
                    break;
                case "INTERNET_RECIEPT":
                    valueConfigFields.add(internetReciept);
                    break;
                case "CONNECT_TO":
                    valueConfigFields.add(connectTo);
                    break;
                case "CLOUD_REG_STATUS":
                    valueConfigFields.add(cloudRegStatus);
                    break;
                case "CLOUD_TEL":
                    valueConfigFields.add(cloudTel);
                    break;
                case "CLOUD_PIN":
                    valueConfigFields.add(cloudPin);
                    break;
                case "CLOUD_ORG_FNS_NUM":
                    valueConfigFields.add(cloudOrgFnsNum);
                    break;
                case "CLOUD_KKT_FNS_NUM":
                    valueConfigFields.add(cloudKktFnsNum);
                    break;
                case "CABINET_REG_EMAIL":
                    valueConfigFields.add(cabinetRegEmail);
                    break;
                case "LAST_FD_NUM":
                    valueConfigFields.add(lastFdNum);
                    break;
                case "SHIFT_NUM":
                    valueConfigFields.add(shiftNum);
                    break;
                case "RECEIPT_NUM":
                    valueConfigFields.add(receiptNum);
                    break;
                case "IS_SHIFT_OPEN":
                    valueConfigFields.add(isShiftOpen);
                    break;
                case "IS_READY_TO_FISCAL":
                    valueConfigFields.add(isReadyToFiscal);
                    break;
                case "IS_FISCAL_MODE":
                    valueConfigFields.add(isFiscalMode);
                    break;
                case "IS_POST_FISCAL_MODE":
                    valueConfigFields.add(isPostFiscalMode);
                    break;
                case "IS_DOC_OPEN":
                    valueConfigFields.add(isDocOpen);
                    break;
                case "IS_IMMEDIATE_REPLACEMENT":
                    valueConfigFields.add(isImmediateReplacement);
                    break;
                case "IS_RESOURCES_EXHAUSTION":
                    valueConfigFields.add(isResourcesExhaustiom);
                    break;
                case "IS_MEMORY_OVERFLOW":
                    valueConfigFields.add(isMemoryOverflow);
                    break;
                case "UNDELEGATED_DOCS_CNT":
                    valueConfigFields.add(undelegatedDocCnt);
                    break;
                case "FIRST_UNDELEGATED_DOC":
                    valueConfigFields.add(firstUndelegatedDoc);
                    break;
                case "DATE_OF_FIRST_UNDELEGATED_DOC":
                    valueConfigFields.add(dateOfFirstUndelegatedDoc);
                    break;
                case "OFD_DISCONNECT_05D":
                    valueConfigFields.add(ofdDisconnect05d);
                    break;
                case "OFD_DISCONNECT_20D":
                    valueConfigFields.add(ofdDisconnect20d);
                    break;
                case "OFD_DISCONNECT_30D":
                    valueConfigFields.add(ofdDisconnect30d);
                    break;
                case "IS_SHIFT_24H":
                    valueConfigFields.add(isShift24h);
                    break;
                case "REG_DATA_CHANGE":
                    valueConfigFields.add(regDataChange);
                    break;
                case "OFD_DATA_CHANGE":
                    valueConfigFields.add(ofdDataChange);
                    break;
                case "FS_CHANGE_KKT_MODE":
                    valueConfigFields.add(fsChangeKktmode);
                    break;
                default:
                    break;
            }
        }
        return valueConfigFields;
    }

    public void print() {
    }
}
