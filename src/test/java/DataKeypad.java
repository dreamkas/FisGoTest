/**
 * Created by v.bochechko on 05.04.2018.
 */
public class DataKeypad {
    private int key1;
    private int key2;
    private KeypadActionEnum action;

    public DataKeypad(int key1, int key2, KeypadActionEnum action) {
        this.key1 = key1;
        this.key2 = key2;
        this.action = action;
    }

}
