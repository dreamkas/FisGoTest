package json.request;

import com.google.gson.annotations.SerializedName;
import json.request.data.CfgData;
import json.request.data.KeypadData;
import json.request.data.enums.CommandEnum;
import json.request.data.enums.CountersFieldsEnum;

import java.util.List;

/**
 * Класс, в котором формируются таски для отправки на кассу
 */
public class TasksRequest {

    @SerializedName("task_id")
    private int taskId;

    @SerializedName("command")
    private CommandEnum command;

    @SerializedName("data")
    private KeypadData keypadData;

    @SerializedName("cfg_data")
    private CfgData cfgData;

    @SerializedName("counters")
    private List<CountersFieldsEnum> countersData;

    @SerializedName("code")
    private String goodsCode;

    @SerializedName("pos_num")
    private Integer positionNumber;

    /**
     * Конструктор на создание таски для нажатия кнопки
     * @param taskId - номер такси
     * @param command - команда
     * @param keypadData - данные для нажатия кнопок
     */
    public TasksRequest(int taskId, CommandEnum command, KeypadData keypadData) {
        this.taskId = taskId;
        this.command = command;
        this.keypadData = keypadData;
    }

    /**
     * Конструктор на создание таски для получения конфига
     * @param taskId - номер таски
     * @param command - команда
     * @param cfgData - данные для получения конфига
     */
    public TasksRequest(int taskId, CommandEnum command, CfgData cfgData) {
        this.taskId = taskId;
        this.command = command;
        this.cfgData = cfgData;
    }

    /**
     * Конструктор для создания таски для команд без данных (получение экрана, режим клавиатуры и т.д)
     * @param taskId - номер таски
     * @param command - команда
     */
    public TasksRequest(int taskId, CommandEnum command) {
        this.taskId = taskId;
        this.command = command;
    }

    /**
     * Конструктор для создания таски для получения счетчиков
     * @param taskId - номер
     * @param command - команда
     * @param countersData - список необходимых счетчиков
     */
    public TasksRequest(int taskId, CommandEnum command, List<CountersFieldsEnum> countersData){
        this.taskId = taskId;
        this.countersData = countersData;
        this.command = command;
    }

    /**
     * Конструктор для создание таски на получение информации о товаре.
     * @param taskId - номер
     * @param command - команда
     * @param goodsCode - комер товара
     */
    public TasksRequest(int taskId, CommandEnum command, String goodsCode) {
        this.taskId = taskId;
        this.command = command;
        this.goodsCode = goodsCode;
    }

    /**
     * Конструктор на создание таски для получаения информации о позиции в чеке
     * @param taskId - номер
     * @param command - команда
     * @param positionNumber - номер позиции
     */
    public TasksRequest(int taskId, CommandEnum command, Integer positionNumber) {
        this.taskId = taskId;
        this.command = command;
        this.positionNumber = positionNumber;
    }

}
