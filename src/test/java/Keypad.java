import java.util.List;
import java.util.Vector;

/*
 * Created by v.bochechko on 04.12.2017.
 */
public class Keypad {
    Keypad() {
    }

    public Short key_code;
    public char key_number;
    public List<Integer> rus;
    public Vector<Integer> rus_code = new Vector<>() ;
    public Vector <Integer> eng_code = new Vector<>();
    public Vector <Integer> spec_sym_code = new Vector<>();
    char key_mode_available;

    public static int keys_table_size = 40;
}