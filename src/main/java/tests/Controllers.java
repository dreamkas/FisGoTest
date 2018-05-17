package tests;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;

@RestController
public class Controllers {

    public static final String PATH_CASH_INFO_REPORT = "src\\test\\resourses\\cash_info_report.txt";
    public static final String PATH_STATS = "src\\test\\resourses\\statistic_json.txt";

    @RequestMapping(value = "/stats", method = RequestMethod.POST)
    public String getJsonStats(@RequestBody String stats) {
        System.out.println("STATS:" + "\n" + "\u001B[34m" + stats + "\n" + "\n" + "\n" + "\033[0m");
        writeInFile(PATH_STATS, stats);
        return stats;
    }

    @RequestMapping(value = "/cashInfoReport", method = RequestMethod.POST)
    public String getJsonCashInfoReport(@RequestBody String cashInfo) {
        System.out.println("CASH INFO REPORT:" + "\n" + "\u001B[32m" + cashInfo + "\n" + "\n" + "\n" + "\033[0m");
        writeInFile(PATH_CASH_INFO_REPORT, cashInfo);
        return cashInfo;
    }

    private void writeInFile(String filePath, String str){
        try (FileWriter writer =  new FileWriter(filePath, false)) {
            writer.write(str);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
