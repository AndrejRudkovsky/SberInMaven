package pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import helpers.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.zip.ZipFile;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static java.lang.String.format;

public abstract class AbstractPage {
    /*******************************************************************************************************************
     *                                             Поля класса
     ******************************************************************************************************************/
    protected ConfigContainer config;
    protected Logger logger;
    protected DateTimeHelper dateTimeHelper = new DateTimeHelper();
    protected int delayTimeMs = 200000;
    protected long pollingIntervalMs = 50;
    protected int shortDelayTimeMs = 2000;
    protected int normDelayTimeMs = 6000;
    protected int longDelayTimeMs = 15000;
    protected int iterationsReloadPage = 23;
    protected int minRetries = 10;
    protected int moreIterations = 100;
    protected int pauseBeforeReloadMs = 12000;
    protected Duration duration = Duration.ofMinutes(4);
    protected Duration shortDuration = Duration.ofMinutes(1);
    private Integer repeatNumber; // Длина строки-разделителя в символах
    private String delimiter;     // Символ, из которого состоит строка-разделитель

    // не должен быть больше 5 минут, иначе по истечении 5 минут Selenium Grid завершит сессию,
    // из-за бездействия автотеста
    private static final int DEFAULT_POLLING_INTERVAL_SECONDS = 100;

    /*******************************************************************************************************************
     * Конструктор класса. Отвечает за инициализацию всех полей класса
     ******************************************************************************************************************/

    public AbstractPage() {
        this.config = ConfigContainer.getInstance();
        this.logger = LogManager.getLogger(WebDriverContainer.class);
        this.repeatNumber = 85;
        this.delimiter = ".";
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
    }

    /*******************************************************************************************************************
     *                                                 Методы класса
     ******************************************************************************************************************/
    /**
     * Возвращает строку-разделитель.
     *
     * @return строка-разделитель
     */
    public String getDelimiterString() {
        return StringUtils.repeat(delimiter, repeatNumber);
    }

    /**
     * Открывает главную страницу Маркет по строке адреса
     */
    public void toMainPage() {
        String siteURL = config.getConfigProperty("SiteURL");
        config.setParameter("currentURL", siteURL);
        logger.info(siteURL);
        open(siteURL);
    }

    /**
     * Совершает переход на предыдущую страницу
     */
    public AbstractPage backPage() {
        sleep(normDelayTimeMs);
        logger.info(">>> Переход на предыдущую страницу");
        back();
        sleep(shortDelayTimeMs);
        return this;
    }


    /**
     * Распознаёт тип локатора по содержимому
     *
     * @param locator локатор элемента
     * @return тип локатора в стороковом представлении ("css", "xpath")
     */
    protected String getLocatorType(String locator) {
        return locator.contains("//") ? "xpath" : "css";
    }

    /**
     * Возвращает элемент типа SelenideElement ($x или $) автоматически распознав тип локатора по его содержимому.
     *
     * @param locator локатор элемента
     * @return элемент типа SelenideElement ($x или $)
     */
    protected SelenideElement getSelenideElement(String locator) {
        return locator.contains("//") ? $x(locator) : $(locator);
    }

    /**
     * Очищает поле ввода у элемента
     *
     * @param locator локатора элемента xpath или css
     */
    public AbstractPage sendButtonPressesBackSpace(String locator) {
        logger.info(format("Очищает поле ввода у элемента {%s}", locator));
        SelenideElement el = getSelenideElement(locator);
        while (el.val().length() != 0)
            el.sendKeys(Keys.BACK_SPACE);
        return this;
    }

    /**
     * Очищает поле ввода у элемента
     *
     * @param element элемент страницы
     */
    public AbstractPage sendButtonPressesBackSpace(SelenideElement element) {
        logger.info(format("Очищает поле ввода у элемента {%s}", element));
        while (element.val().length() != 0)
            element.sendKeys(Keys.BACK_SPACE);
        return this;
    }

    /**
     * Очищает поле ввода у элемента
     *
     * @param element элемент страницы
     */
    public AbstractPage sendButtonPressesBackSpaceAndDelete(SelenideElement element) {
        logger.info("Очищает поле ввода у элемента {%s}");
        while (element.val().length() != 0) {
            element.sendKeys(Keys.BACK_SPACE);
            element.sendKeys(Keys.DELETE);
        }
        return this;
    }



    /**
     * Обновляет страницу
     */
    public AbstractPage reloadPage() {
        sleep(shortDelayTimeMs);
        logger.info(">>> (reload) Обновление страницы");
        refresh();
        sleep(shortDelayTimeMs);
        return this;
    }

    /**
     * Очищает данные браузера
     */
    public AbstractPage clear() {
        Selenide.clearBrowserLocalStorage();
        sleep(shortDelayTimeMs);
        Selenide.clearBrowserCookies();
        sleep(shortDelayTimeMs);
        return this;
    }

    /**
     * Нажимает в пустое место для закрытия всплывшего окна
     */
    public void pressIntoEmptySpace() {
        sleep(normDelayTimeMs);
        logger.info(">>> Нажимает в пустое место для закрытия всплывшего окна");
        $("body").shouldBe(visible, duration).click();
        sleep(shortDelayTimeMs);
    }


    /**
     * Отключает у всех <input> возможность открытия Windows-окна выбора файлов
     */
    protected void disableWindowsUploader() {
        logger.info("!!! Отключает у всех <input> возможность открытия Windows-окна выбора файлов");
        executeJavaScript(
                "HTMLInputElement.prototype.click = function() {" +
                        "  if(this.type !== 'file') HTMLElement.prototype.click.call(this);" +
                        "};"
        );
    }

    /**
     * Нажимает на элемент с помощью js-скрипта
     *
     * @param locator селектор элемента на который производится нажатие
     */
    public AbstractPage jsClick(String locator) {
        logger.info(format("Ожидает появления элемента {%s}", locator));
        getSelenideElement(locator).shouldBe(exist, duration);
        logger.info(format("Формирование js-script для локатора {%s}", locator));
        Map<String, String> script = new HashMap<>();
        script.put("css", "document.querySelector(\"%s\").click();");
        script.put("xpath", "document.evaluate(\"%s\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.click();");
        String finalScript = format(script.get(getLocatorType(locator)), locator);
        logger.info(format("Исполняет скрипт клика \n{%s}", finalScript));
        executeJavaScript(finalScript);
        return this;
    }

    /**
     * Нажимает на элемент с помощью js-скрипта
     *
     * @param element элемент на который производится нажатие
     */
    public AbstractPage jsClick(SelenideElement element) {
        logger.info(format("Нажимает на элемент с помощью js-скрипта {%s}", element));
        element.shouldBe(enabled);
        String script = "arguments[0].click();";
        executeJavaScript(script, element);
        return this;
    }

    /**
     * Извлекает числовое значение из российского текста
     *
     * @param str текст российской локализации
     * @return число типа double
     */
    public double extractDoubleNumberFromTextRULocale(String str) {
        logger.info(format("Извлекается число из российского текста {%s}", str));
        return Double.parseDouble(str.replaceAll("[^,.0123456789]", "")
                .replace(",", "."));
    }

    /**
     * Извлекает числовое значение из английского текста
     *
     * @param str текст английской локализации
     * @return число типа double
     */
    public double extractNumberFromTextENGLocale(String str) {
        logger.info("Извлекается число из английского текста");
        return Double.parseDouble(str.replaceAll("[^,.0123456789]", ""));
    }

    /**
     * Извлекает числовое значение из текста
     *
     * @param str строка которая должна содержать число
     * @return Строковое представление числа
     */
    public String extractDoubleFromTextInTextView(String str) {
        logger.info("Извлекается число из текста в текстовом виде без учета локализации языка");
        return str.replaceAll("[^,.0123456789]", "");
    }

    /**
     * Извлекает целое числовое значение из текста
     *
     * @param str строка которая должна содержать целое число
     * @return Строковое представление числа
     */
    public String extractIntegerFromTextInTextView(String str) {
        logger.info("Извлекается целое число из текста в текстовом виде без учета локализации языка");
        return Integer.toString(Integer.parseInt(str.replaceAll("[^0123456789]", "")));
    }

    /**
     * Преобразовывается числовое значение в строковое для ввода
     *
     * @param n обрабатываемое число
     */
    public String formatNumberToString(double n) {
        logger.info(format("Преобразовывается числовое значение {%s} в строковое для ввода", n));
        return Double.toString(n).replace(".", ",");
    }


    /**
     * Возвращает список имен файлов в указанном каталоге.
     *
     * @param folderName каталог, откуда следует получить список имен файлов
     * @return список имен файлов в указанном каталоге
     */
    private List<String> getFolderFileNames(String folderName) {
        List<String> listOfFileNames = new ArrayList<>();
        File file = new File(folderName);
//        Assert.assertTrue("[ОШИБКА]: указанное в параметре имя не является каталогом", file.isDirectory());
        File[] listOfFiles = file.listFiles();
//        Assert.assertNotNull("[ОШИБКА]: список файлов в каталоге = null", listOfFiles);

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) listOfFileNames.add(listOfFile.getName());
        }
        return listOfFileNames;
    }

    /**
     * Возвращает список имен файлов в указанном каталоге.
     *
     * @param folderName каталог, откуда следует получить список имен файлов
     * @return список имен файлов в указанном каталоге
     */
    private String[] getZipFileNames(String folderName) {
        File file = new File(folderName);
        logger.info(format(">>> Получение наименований файлов с расширением .zip во временной папке: %s", folderName));
        String[] files = file.list(new FilenameFilter() {

            @Override
            public boolean accept(File folder, String name) {
                return name.endsWith(".zip");
            }

        });
        logger.info(">>> В папке содержатся файлы с именами: ");
        for (String filename : files) {
            logger.info(format(">>>> %s", filename));
        }
        return files;
    }

    /**
     * Удаляет временную папку теста для загрузки файлов вместе с содержащимися в ней файлами.
     *
     * @param file путь к папке
     */
    private void deleteTemporaryFolderWithFiles(File file) {
        logger.info(format("Выполняется удаление временной папки и файлов внутри: [%s]", file));
        if (!file.exists()) {
            logger.info(format("Временная папка по заданному пути: [%s] не существует.", file));
            return;
        }

        if (file.isDirectory())
            for (File f : Objects.requireNonNull(file.listFiles())) deleteTemporaryFolderWithFiles(f);
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Проверяет, что открыт необходимый url
     *
     * @param url ожидаемый адрес
     */
    public void verifyUrl(String url) {
        logger.info(format("Проверяет, что открыт { %s }", url));
        Assert.assertEquals(
                WebDriverRunner.url(), url
        );
    }

    /**
     * Проверяет, что открыт необходимый url
     *
     * @param url1 ожидаемый адрес 1
     * @param url2 ожидаемый адрес 2
     */
    public void verifyUrl(String url1, String url2) {
        logger.info(format("Проверяет, что открыт { %s } или { %s }", url1, url2));
        Assert.assertTrue(
                WebDriverRunner.url().equals(url1)
                        || WebDriverRunner.url().equals(url2)
        );
    }

    /**
     * Удаляет указанный файл из указанной директории
     *
     * @param dir  директория, из которой требуется удалить файл
     * @param file наименование файла, который требуется удалить
     */
    public void deleteDocumentFromDir(String dir, String file) {
        logger.info("Проверка наличия ведущей косой черты");
        if (!dir.substring(dir.length() - 1).equals("/")) dir += "/";
        String path = dir + file;
        logger.info(format("Удаляет документ {%s}", path));
        File document = new File(path);
        Assert.assertEquals(file, document.getName());
        boolean isFileDeleted = document.delete();
        Assert.assertTrue(isFileDeleted);
    }

    /**
     * Замедленное заполнение поля ввода. Если поле ввода, при быстром вводе заполняется не корректно.
     * К примеру: вводимое значение "закупка", а получаемое "купказа". Для обхода перемешивания добавлена пауза при вводе.
     *
     * @param element поле ввода, в которое происходит ввод строки
     * @param keys    вводимое строковое значение
     */
    public void slowSendKeys(SelenideElement element, String keys) {
        logger.info(format("Замедленно вводит текст {%s}", keys));
        for (Character key : keys.toCharArray()) {
            element.sendKeys(key.toString());
            sleep(300);
        }
        Assert.assertEquals("Значение установлено не корректно", keys, element.val());
    }

    /**
     * Генерирует строку случайных чисел
     *
     * @param length длина строки
     * @return строка состаящая из случайных чисел
     */
    public String getStringWithRandomDigits(int length) {
        StringBuilder correspondentAccountRandomValue = new StringBuilder();
        for (int index = 0; index < length; index++) {
            correspondentAccountRandomValue.append(new Random().nextInt(9));
        }
        return correspondentAccountRandomValue.toString();
    }

}