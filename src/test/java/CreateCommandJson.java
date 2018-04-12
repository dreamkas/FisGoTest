import java.util.List;

/**
 * класс, из которого формируется Json для передачи в сокет который
 */
public class CreateCommandJson {
    private String uuid;
    private List <Tasks> tasks;

    public CreateCommandJson (List <Tasks> tasks, String uuid) {
        this.tasks = tasks;
        this.uuid = uuid;
    }
}
