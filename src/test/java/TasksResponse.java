import java.util.List;

/**
 * Created by v.bochechko on 06.04.2018.
 */
public class TasksResponse {
    private List<TaskResponse> tasks = null;

    public TasksResponse (List<TaskResponse> tasks) {
        this.tasks = tasks;
    }

    public void savePicture(int taskId) {
        tasks.get(taskId).savePicture();
    }

    public int getKeypadMode(int taskId) {
        return tasks.get(taskId).getKeypad_mode();
    }

    public String getTaskResult(int taskId) {
        if(tasks != null) {
            return tasks.get(taskId).getResult();
        } else {
            return "Error, list is null";
        }
    }

    public List<String> getConfigValue(int taskId, List<ConfigFieldsEnum> configFieldsEnum) {
        return tasks.get(taskId).getFieldsValue(configFieldsEnum);
    }

    public void print() {
     //   System.out.println();
     //   System.out.println("tasks : ");
     //   tasks.get(0).print();
       // System.out.println();
      //  tasks.get(1).print();
      //  System.out.println();
        /*tasks.get(2).print();
        System.out.println();
        tasks.get(3).print();*/
    }

}
