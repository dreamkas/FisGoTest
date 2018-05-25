# FisGoTest

### Начальные установки: 

1. Скачать и установить JDK 8
2. Скачать и установить Maven 
3. Склонить https://github.com/dreamkas/FisGoTest

---

### Команды для запуска тестов: ###
Запускает все тесты:
`mvn clean test` 

Cтроит отчет и открывает во вкладке браузера:
`mvn allure:serve` 

Генерация отчета в директорию target/site/allure-maven/index.html:
`mvn allure:report`

Запустить один набор (класс тестов):
`mvn install -Dtest=ClassName`

---

### Чтобы писать тесты: 

Начальные установки (под аннотацию @BeforeClass)
1. Создать экземпляр класса CashBox, в конструкторе передать UUID, тип кассы (дримкас-ф или касса ф) и IP кассы:

`Cashbox cashBox = new CashBox("12345678-1234-1234-1234-123456789012", CashBoxType.DREAMKASRF, "192.168.242.111");`

2. Создать экземпляр класса Bot, в конструкторе передать экземпляр кассы:

`Bot bot = new Bot(cashBox);`

3. Необходимо установить tcp-соединение с кассой для обмена данными. 

Открыть соединение:
`bot.start();`
Закрыть соединение:
`bot.stop();`

---

### Список команд для взаимодействия с кассой: ###

Описание JSON команд:
https://fisgotestapi.docs.apiary.io/

**Методы класса Bot:**

 Команда для получения скриншота с экрана кассы. Скриншот сохраняется в папку reciveData в каталоге проекта.
    
`String getScreenJson();`

 Команда на получение текущего режима клавиатуры. (кириллица, английский и т.д.)
 @return int - номер обозначающий режиим клавиатуры
  
`int getKeypadMode();`

 Нажатие клавиши на кассе.
      @param keyNum - первая кнопка
      @param keyNum2 - вторая кнопка
      @param pressCount - количество нажатий
     
`void pressKey(int keyNum, int keyNum2, int pressCount);`

 Метод для получения необходимых полей из конфига.
      @param configFieldsEnums - массив с необходимыми полями
      @return - map со значениями нужных полей
   
`Map<ConfigFieldsEnum, String> getConfig(ConfigFieldsEnum ... configFieldsEnums);`

Команда для получения счетчиков
 @param countersFieldsEnums - массив с необходимыми полями
@return объект CountersResponse

`CountersResponse getCounters(CountersFieldsEnum ... countersFieldsEnums);`

Команда для получения статуса лоудера
   @return true - если на экране лоудер

 `boolean isLoaderScreen();`

   Отправка команды по SSH
    @param command - команда.
   @return List<String> - с возращаемыми полями
  
   ` List<String> sendCommandSsh(String command);`
