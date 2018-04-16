package remoteAccess;

/**
 * класс содержит sql команды для выполнения на кассе ( возможно, скоро отомрет )
 */
public class SQLCommands {
    private static final String attachCounters = "echo \"attach '/FisGo/countersDb.db' as counters; ";
    private static final String attachConfig = "echo \"attach '/FisGo/configDb.db' as config; ";
    private static final String attachReceipts = "echo \"attach '/FisGo/receiptsDb.db' as receipts; ";
    private static final String attachUsers = "echo \"attach '/FisGo/usersDb.db' as users; ";

    private static final String sqliteCounters = "| sqlite3 /FisGo/countersDb.db";
    private static final String sqliteConfig = " | sqlite3 /FisGo/configDb.db";
    private static final String sqliteReceipts = " | sqlite3 /FisGo/receiptsDb.db";

    private static final String deleteCounters = attachCounters + "delete from counters.COUNTERS;\" " + sqliteConfig + "\n";
    public String getDeleteCounts() {
        return deleteCounters;
    }

    private static final String stageCommand = attachConfig + "select STAGE from config.CONFIG;\" " + sqliteConfig + "\n";
    public String getStageCommand() {
        return stageCommand;
    }

    private static final String openShiftCommand = attachConfig +
            "select OPEN_SHIFT_DATE from config.CONFIG; " + //get(0)
            "select SHIFT_TIMER from config.CONFIG;\" " +   //get(1)
            sqliteConfig + "\n";
    public String getOpenShiftCommand() {
        return openShiftCommand;
    }

    private static final String dateCommand = " date '+%d%m%y%H%M'\n";
    public String getDateCommand() {
        return dateCommand;
    }

    private static final String recieptCountCommand = attachReceipts + "select count (*) from receipts.RECEIPTS;\" " +sqliteReceipts + "\n";
    public String getRecieptCountCommand() {
        return recieptCountCommand;
    }

    private static final String countersAdventValueCommand = attachCounters + "select * from counters.COUNTERS;\" " + sqliteCounters + "\n";
    public String getCountersAdventValueCommand() {
        return countersAdventValueCommand;
    }

    private static final String terminalModeCommand = attachConfig + "select TERMINAL_MODE from config.CONFIG;\"" + sqliteConfig + "\n";
    public String getTerminalModeCommand() {
        return terminalModeCommand;
    }

    private static final String cashInFinalCunterCommand = attachCounters + "select CASH_IN_FINAL from counters.COUNTERS;\" " + sqliteCounters + "\n";
    public String getCashInFinalCunterCommand() {
        System.out.println("cashInFinalCunterCommand = " + cashInFinalCunterCommand);
        return cashInFinalCunterCommand;
    }

    private static final String organiztionInnCommand = attachConfig + "select ORGANIZATION_INN from config.CONFIG;\"" + sqliteConfig + "\n";
    public String getOrganiztionInnCommand () {
        return  organiztionInnCommand;
    }

    private static final String organiztionNameCommand = attachConfig + "select ORGANIZATION_NAME from config.CONFIG;\" " + sqliteConfig + "\n";
    public String getOrganiztionNameCommand () {
        return  organiztionNameCommand;
    }

    private static final String clcAddressCommand = attachConfig + "select CALCULATION_ADDRESS from config.CONFIG;\"" + sqliteConfig + "\n";
    public String getClcAddressCommand () {
        return  clcAddressCommand;
    }

    private static final String clcPlaceCommand = attachConfig + "select CALCULATION_PLACE from config.CONFIG;\"" + sqliteConfig + "\n";
    public String getClcPlaceCommand () {
        return  clcPlaceCommand;
    }

    private static final String regNumCommand = attachConfig + "select KKT_REG_NUM from config.CONFIG;\"" + sqliteConfig + "\n";
    public String getRegNumCommand () {
        return  regNumCommand ;
    }

    private static final String taxSystemCommand = attachConfig + "select TAX_SYSTEMS from config.CONFIG;\"" + sqliteConfig + "\n";
    public String getTaxSystemCommand() {
        return  taxSystemCommand;
    }
}
