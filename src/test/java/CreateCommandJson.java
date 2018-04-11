import java.util.List;

/**
 * Created by v.bochechko on 05.04.2018.
 */
public class CreateCommandJson {
    private String uuid = Config.UUID;
    private List <Tasks> tasks;

    public CreateCommandJson (List <Tasks> tasks) {
        this.tasks = tasks;
    }
}
