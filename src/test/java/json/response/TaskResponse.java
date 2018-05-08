package json.response;

import com.google.gson.annotations.SerializedName;
import json.request.data.enums.ConfigFieldsEnum;
import json.response.data.CountersResponse;
import json.response.data.GoodsData;
import json.response.data.PositionData;
import lombok.Getter;

import java.util.Map;

/**
 * Класс, в который парсятся отдельные таски из Json-а с кассы
 */

@Getter
public class TaskResponse {

    @SerializedName("task_id")
    private int taskId;

    private String result;

    @SerializedName("lcd_screen")
    private int[] lcdScreen;

    @SerializedName("keypadMode")
    private int keypadMode;

    @SerializedName("cfg_data")
    private Map<ConfigFieldsEnum, String> configData;

    @SerializedName("counters_data")
    private CountersResponse countersData;

    @SerializedName("loader_status")
    private String loaderStatus;

    @SerializedName("GoodsData")
    private GoodsData goodsData;

    @SerializedName("PositionData")
    private PositionData positionData;

    private String message;

}
