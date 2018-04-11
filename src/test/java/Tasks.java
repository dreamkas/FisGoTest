import org.junit.Test;

/**
 * Created by v.bochechko on 05.04.2018.
 */
public class Tasks {
    private int task_id;
    private CommandEnum command;
    private DataKeypad data;
    private Cfg_data cfg_data;

    public Tasks (int task_id, CommandEnum command, DataKeypad dataKeypad, Cfg_data cfg_data){
        this.task_id = task_id;
        this.command = command;
        this.data = dataKeypad;
        this.cfg_data = cfg_data;
    }
}
