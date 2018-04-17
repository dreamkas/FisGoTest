package json.fromSever;

import java.util.List;

import cashbox.ConfigFieldsEnum;
/**
 * Класс, в который парсится Json тасок с кассы
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
}
