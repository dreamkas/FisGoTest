package json.response.data.CountersData;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class CorData {

    @SerializedName("CNT")
    private String cnt;
    @SerializedName("CASH")
    private String cash;
    @SerializedName("CARD")
    private String card;


}
