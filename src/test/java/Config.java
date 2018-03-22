/**
 * @author v.bochechko on 04.12.2017.
 */
public class Config {
    //0 - дримкас Ф, 1 - дримкас РФ
    private static int cashType = 0;
    public int getCashType() {
        return cashType;
    }
    public void setCashType(int type) {
        cashType = type;
    }
}
