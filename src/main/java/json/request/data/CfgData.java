package json.request.data;

import json.request.data.enums.ConfigFieldsEnum;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Класс для добавления в Json списка полей конфига, которые будут запрошены с кассы
 */

@AllArgsConstructor
public class CfgData {

    private List<ConfigFieldsEnum> fields;

}
