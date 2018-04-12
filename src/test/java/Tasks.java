import com.google.gson.annotations.SerializedName;

/**
 * Класс, в котором формируются таски для отправки на кассу
 */
public class Tasks {
    private int task_id;
    private CommandEnum command;
    private DataKeypad data;
    @SerializedName("cfg_data")
    private CfgData cfgData;

    public Tasks (int task_id, CommandEnum command, DataKeypad dataKeypad, CfgData cfgData) {
        this.task_id = task_id;
        this.command = command;
        this.data = dataKeypad;
        this.cfgData = cfgData;
    }
}
