package json.response.data.CountersData;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class XZData {

    @SerializedName("OPERATION_CNT")
    private String operationCnt;

    @SerializedName("DOC_NUM")
    private int docNum;

    @SerializedName("CASH_IN_DRAWER")
    private String cashInDrawer;

    @SerializedName("SALE_CNT")
    private int saleCnt;

    @SerializedName("SALE_SUM")
    private String  saleSum;

    @SerializedName("RET_CNT")
    private int retCnt;

    @SerializedName("RET_SUM")
    private String retSum;

    @SerializedName("CANCELED_CNT")
    private int canceledCnt;

    @SerializedName("CANCELED_SUM")
    private String canceledSum;

    @SerializedName("INSERT_CNT")
    private int insertCnt;

    @SerializedName("INSERT_SUM")
    private String insertSum;

    @SerializedName("RESERVE_CNT")
    private int reserveCnt;

    @SerializedName("RESERVE_SUM")
    private String reserveSum;

}
