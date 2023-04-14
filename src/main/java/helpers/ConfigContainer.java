package helpers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Содержит настройки тестовой среды общие для всех тестов.
 * <p>
 * Для каждой новой настройки или параметра необходимо создать get-метод по примеру существующих, например:
 * <p>
 * public String getFakeEmailAddress()
 * {
 * return properties.getProperty("FakeEmailAddress");
 * }
 * <p>
 * Если параметр теста планируется изменять в ходе теста (передача данных между шагами) то его следует объвить как
 * private-поле класса с соответствующими public-методами доступа get и set.
 * <p>
 */
public class ConfigContainer implements Serializable {
    /**
     * Константы класса
     */
    public static final Logger logger = LogManager.getLogger(ConfigContainer.class);

    // Относительный путь к файлу с настройками тестовой среды (параметризованный конфигурационный файл)
    private static final String PROPERTIES_FILE_NAME = "target/test-classes/config.properties";

    // Путь к временной папке со случайно сгенерированным именем
    private String pathToTempFolderWithRandomName;

    /******************************************************************************************************************
     *
     *                                            Поля класса
     *
     ******************************************************************************************************************/

    // Статический экземпляр этого класса (собственно сам ConfigContainer)
    private static volatile ConfigContainer instance;

    // Настройки тестовой среды (считываются из файла config.properties и используются во всех тестовых сценариях)
    private Properties properties = new Properties();

    // Параметры конкретного тестового сценария (id, name и прочее, что генерируется в ходе теста) для передачи между
    // шагами теста. Существуют в памяти только во время выполнения теста.
    private Map<String, String> parameters = new HashMap<>();

    /******************************************************************************************************************
     *
     *                                 Методы доступа к настройкам тестовой среды
     *
     *****************************************************************************************************************/

    /**
     * @return экземпляр этого класса (синглтон).
     */
    public static synchronized ConfigContainer getInstance() {
        if (instance == null) {
            synchronized (ConfigContainer.class) {
                instance = new ConfigContainer();
            }
        }
        return instance;
    }

    public Properties getProperties() {
        return this.properties;
    }

    //Метод преобразовывает кодировку ISO-8859-1 в UTF8. Нужен для правильного восприятия параметров на русском языке.
    public String encodeToUTF8(String stringToEncode) {
        byte[] stringBytes = stringToEncode.getBytes(Charset.forName("ISO-8859-1"));
        return new String(stringBytes, Charset.forName("UTF8"));
    }

    public String encodeOfWin1252ToWin1251(String stringToEncode) {
        byte[] stringBytes = stringToEncode.getBytes(Charset.forName("windows-1252"));
        return new String(stringBytes, Charset.forName("windows-1251"));
    }

    // region Относительный путь к файлу с настройками тестовой среды
    private String getPropertiesFileName() {
        return PROPERTIES_FILE_NAME;
    }

    // endregion
    public String getConfigPropertyWithEncode(String key) {
        return encodeToUTF8(properties.getProperty(key));
    }

    public String getConfigProperty(String key) {
        logger.info("(config) Получение значения по ключу: " + key);
        return properties.getProperty(key);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // region Методы получения информации о пользователе

    // endregion
    // -----------------------------------------------------------------------------------------------------------------


    /**
     * @return возвращает ссылку на сайт (текущий стенд)
     */
    String getSiteUrl() {
        return properties.getProperty("SiteURL");
    }

    /******************************************************************************************************************
     *
     *                                           Методы класса
     *
     ******************************************************************************************************************/


    /**
     * Загружает настройки тестовой среды из файла [config.properties].
     */
    public ConfigContainer loadConfig() {
        logger.info("(config) Загружает настройки тестовой среды из файла [config.properties].");
        InputStream input = null;
        try {
            input = new FileInputStream(getPropertiesFileName());
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

    /**
     * Загружает настройки тестовой среды из файла
     *
     * @param path путь к добавляемому конфиг-файлу
     */
    public void loadConfig(String path) {
        InputStream input = null;
        try {
            input = new FileInputStream(path);
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Возвращает уникальное имя.
     *
     * @param namePrefix префикс уникального имени
     * @return уникальное имя
     */
    public String generateUniqueName(String namePrefix) {
        String dateString = new SimpleDateFormat("ddMMyyyyHHmmSS").format(new Date());
        return namePrefix != null ? namePrefix + dateString : dateString;
    }


    /**
     * Возвращает полный путь к файлу
     *
     * @param fileName имя файла
     * @return полный путь к файлу
     */
    private String getFullPathTo(String fileName) {
        return new File(fileName).getAbsolutePath();
    }

    /**
     * Возвращает тестовое имя
     */
    public String getAuctionWithPreferencesTestType() {
        return properties.getProperty("AuctionWithPreferencesTestType");
    }

    // endregion

    // region Параметры конкретного тестового сценария

    public void setParameter(String key, String value) {
        logger.info("  установлен ключ: [" + key + "] и параметр: [" + value + "]");

        // Блокируем возможность записать в параметр значения null или "пустая строка"
        Assert.assertTrue("  [Ошибка]: Попытка установить значение ключа равно NULL",
                key != null);
        Assert.assertTrue("  [Ошибка]: Попытка установить значение параметра = null",
                value != null);
        Assert.assertFalse("  [Ошибка]: Попытка установить пустое значение ключа",
                key.equals(""));
        Assert.assertFalse(" [Ошибка]: Попытка установить пустое значение параметра",
                value.equals(""));

        // Защита от "дурака" - перезапись существующего параметра в большинстве случаев признак ошибки в коде
        if (parameters.get(key) != null) {
            logger.info("  [ВНИМАНИЕ]: перезапись значения параметра, старое значение: " +
                    "[" + parameters.get(key) + "], новое значение: [" + value + "]");
        }

        parameters.put(key, value);
    }

    public String getParameter(String key) {
        // Контролируем переданное значение ключа для поиска параметра (не null и не пустая строка)
        Assert.assertTrue("  [Ошибка]: значение переданного ключа = null !", key != null);
        Assert.assertFalse("  [Ошибка]: пустое значение переданного ключа !", key.equals(""));

        String value = parameters.get(key);

        // Контролируем полученное по ключу значение параметра (не null и не пустая строка)
        logger.info("  получен ключ: <" + key + "> и параметр: <" + value + ">");
        Assert.assertTrue("  [Ошибка]: значение полученного параметра равно NULL", value != null);
        Assert.assertFalse("  [Ошибка]: пустое значение полученного параметра", value.equals(""));

        return value;
    }
    // endregion


    public void generatePathToTempFolder() {
        String path;
            path = System.getProperty("user.dir") + "\\temp"
                    + new SimpleDateFormat("ddMMyyyyHHmmSS").format(new Date());
        logger.info("(config) Путь к директории для загрузки файлов: " + path);
        this.pathToTempFolderWithRandomName = path;
    }

    /**
     * Возвращает путь к временной папке со случайно сгенерированным именем.
     *
     * @return путь к временной папке со случайно сгенерированным именем
     */
    public String getPathToTempFolderWithRandomName() {
        return this.pathToTempFolderWithRandomName;
    }

    /**
     * Печатает список всех параметров, использованных в текущем тесте.
     */
    public void printParameters() {
        logger.info(String.
                format("=========   С П И С О К   И С П О Л Ь З О В А Н Н Ы Х   П А Р А М Е Т Р О В   [%d]   =========",
                        parameters.size()));
        for (Map.Entry<String, String> parameter : parameters.entrySet())
            logger.info(String.format(
                    ">>> (printParameters) Ключ: [%s], Значение: [%s].", parameter.getKey(), parameter.getValue()));
    }
}
