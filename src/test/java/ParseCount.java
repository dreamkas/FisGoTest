/**
 * Класс будет использоваться для работы со счетчиками
 */
public class ParseCount {
    public static final int ID = 0,
            CASH_ON_TOP = 1,
            ADVENT = 2,                         //меняется при продаже прихода наличными
            ADVENT_CNT = 3,                     //меняется при продаже прихода наличными
            CONSUMPTION = 4,                    //меняется при продаже расхода наличными
            CONSUMPTION_CNT = 5,                //меняется при продаже расхода наличными
            ADVENT_RETURN = 6,                  //меняется при возврате прихода наличными
            ADVENT_RETURN_CNT = 7,              //меняется при возврате прихода наличными
            CONSUMPTION_RETURN = 8,             //меняется при возврате расхода наличными
            CONSUMPTION_RETURN_CNT = 9,         //меняется при возврате расхода наличными

            INSERTION = 10,                     //меняется при внесении
            INSERTION_CNT = 11,                 //меняется при внесении
            RESERVE = 12,                       //меняется при изъятии
            RESERVE_CNT = 13,                   //меняется при изъятии
            CASH_IN_FINAL = 14,                 //меняется при продаже прихода, продаже расхода, возврате прихода, возврате расхода, внесении, изъятии (все операции наличными)

            ADVENT_CARD = 15,                   //меняется при продаже прихода электронными
            ADVENT_CARD_CNT = 16,               //меняется при продаже прихода электронными
            ADVENT_RETURN_CARD = 17,            //меняется при возврате прихода электронными
            ADVENT_RETURN_CARD_CNT = 18,        //меняется при возврате прихода электронными
            ADVENT_TOTAL = 19,                  //меняется при продаже прихода наличными и электронными
            CONSUMPTION_TOTAL = 20,             //меняется при продаже расхода наличными и электронными
            ADVENT_RETURN_TOTAL = 21,           //меняется при возврате прихода наличными и электронными
            CONSUMPTION_RETURN_TOTAL = 22,      //меняется при возврате расхода наличными и электронными
            REALIZATION_TOTAL = 23,             //меняется при любых операциях, кроме изъятия и внесения (?)
            REPORT_CNT = 24,

            CASH = 25,                          //все операции наличными
            CARD = 26,                          //все операции электронными
            CASH_CNT = 27,                      //все операции наличными
            CARD_CNT = 28,                      //все операции электронными

            CONSUMPTION_CARD = 29,              //меняется при продаже расхода электронными
            CONSUMPTION_CARD_CNT = 30,          //меняется при продаже расхода электронными
            CONSUMPTION_RETURN_CARD = 31,       //меняется при возврате расхода электронными
            CONSUMPTION_RETURN_CARD_CNT = 32,   //меняется при возврате расхода электронными
            ADVENT_TOTAL_ABS = 33,              //меняется при продаже прихода наличными и электронными
            CONSUMPTION_TOTAL_ABS = 34,         //меняется при продаже расхода наличными и электронными
            ADVENT_RETURN_TOTAL_ABS = 35,       //меняется при возврате прихода наличными и электронными
            CONSUMPTION_RETURN_TOTAL_ABS = 36,  //меняется при возврате расхода наличными и электронными
            REALIZATION_TOTAL_ABS = 37,         //меняется при любых операциях
            CURR_RECEIPT_NUM = 38,
            CURR_SHIFT_NUM = 39;

    public int[] parseCountValueFromStr(String countStr) {
        int [] countArray = new int[40];
        String[] parts = (countStr).split("\\|");
        for (int i = 0; i < countArray.length ; i++) {
            countArray[i] = Integer.parseInt(parts[i]);
        }
        return countArray;
    }
}
