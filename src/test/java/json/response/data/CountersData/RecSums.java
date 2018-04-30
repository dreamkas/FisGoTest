package json.response.data.CountersData;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class RecSums {

    @SerializedName("CANCELED")
    private String canceled;

    @SerializedName("DEFFERED")
    private String deffered;

    @SerializedName("INSERT")
    private String insert;

    @SerializedName("RESERVE")
    private String reserve;




}
