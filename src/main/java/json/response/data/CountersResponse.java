package json.response.data;

import com.google.gson.annotations.SerializedName;
import json.response.data.CountersData.*;
import lombok.Getter;

import java.util.List;

/**
 * https://fisgotestapi.docs.apiary.io/#reference/0/counters_get/counters_get
 */

@Getter
public class CountersResponse {

    @SerializedName("SHIFT_NUM")
    private int shiftNum;

    @SerializedName("NEXT_REC_NUM")
    private int nextRecNum;

    @SerializedName("SALE_SUMS")
    private PaymentMethods saleSums;

    @SerializedName("SALE_CNTS")
    private PaymentMethods saleCounts;

    @SerializedName("RET_SUMS")
    private PaymentMethods returnSums;

    @SerializedName("RET_CNTS")
    private PaymentMethods returnCounts;

    @SerializedName("REC_SUMS")
    private RecSums recSums;

    @SerializedName("DISCOUNT_SUMS")
    private DiscountSums discountSums;

    @SerializedName("SALE_TAXS")
    private Taxs saleTaxs;

    @SerializedName("RET_TAXS")
    private Taxs retTaxs;

    @SerializedName("X_Z_DATA")
    private XZData xzData;

    @SerializedName("DEP_SALE_SUM")
    private List<String> depSaleSum;

    @SerializedName("DEP_RET_SUM")
    private List<String> depRetSum;

    @SerializedName("PURCHASE_CNT")
    private int purchaseCount;

    @SerializedName("RET_PURCHASE_CNT")
    private int returnPurchaseCount;

    @SerializedName("PURCHASE_SUMS")
    private PaymentMethods purchaseSums;

    @SerializedName("RET_PURCHASE_SUMS")
    private PaymentMethods returnPurchaseSums;

    @SerializedName("COR_DATA")
    private CorData corData;

}
