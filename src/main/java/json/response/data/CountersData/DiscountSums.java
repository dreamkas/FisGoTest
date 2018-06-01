package json.response.data.CountersData;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class DiscountSums {

    @SerializedName("SALE")
    private String sale;

    @SerializedName("RET")
    private String ret;

}
