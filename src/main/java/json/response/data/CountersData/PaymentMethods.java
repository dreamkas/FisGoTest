package json.response.data.CountersData;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class PaymentMethods {

    @SerializedName("CASH")
    private String cash;

    @SerializedName("CARD")
    private String card;

    @SerializedName("ADVANCE")
    private String advance;

    @SerializedName("CREDIT")
    private String credit;

    @SerializedName("EXCHANGE")
    private String exchange;

}
