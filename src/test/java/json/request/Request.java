package json.request;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Request {

    private String uuid;

    @SerializedName("tasks")
    private List<TasksRequest> tasksRequestList;
}
