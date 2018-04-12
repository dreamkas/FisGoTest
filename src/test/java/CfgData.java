import java.util.List;

/**
 * Класс для добавления в Json списка полей конфига, которые будут запрошены с кассы
 */
public class CfgData {
    private List<ConfigFieldsEnum> fields;

    public CfgData(List<ConfigFieldsEnum> listCfgField) {
        this.fields = listCfgField;
    }
}
