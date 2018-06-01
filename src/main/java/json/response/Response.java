package json.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Response {

    @SerializedName("tasks")
    List<TaskResponse> taskResponseList;

}
