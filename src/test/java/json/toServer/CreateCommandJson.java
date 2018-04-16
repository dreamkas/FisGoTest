package json.toServer;

import java.util.List;

import cashbox.Config;
/**
 * класс, из которого формируется Json для передачи в сокет который
 */
public class CreateCommandJson {
    private String uuid = Config.UUID;
    private List <Tasks> tasks;

    public CreateCommandJson (List <Tasks> tasks) {
        this.tasks = tasks;
    }
}
