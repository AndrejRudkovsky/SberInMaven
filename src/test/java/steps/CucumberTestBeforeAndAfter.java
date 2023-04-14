package steps;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.junit5.BrowserStrategyExtension;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import helpers.ConfigContainer;
import helpers.WebDriverContainer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.ClassRule;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import pages.CommonPage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import static com.codeborne.selenide.Selenide.sleep;
import static java.lang.String.format;

public class CucumberTestBeforeAndAfter {
    public static final Logger logger = LogManager.getLogger(WebDriverContainer.class);
    /******************************************************************************************************************
     *
     *                                          Методы класса
     *
     *****************************************************************************************************************/

    /**
     * Рестарт браузера после выполнения каждого тест класса (добавлено по совету А. Солнцева) test
     */
    @ClassRule
    public static BrowserStrategyExtension perClass = new BrowserStrategyExtension();

    /**
     * Код, который выполняется до каждого сценария.
     */
    @Before
    public void setUp() throws IOException, InterruptedException {

            logger.info(">>> (beforeTest) Уничтожает все существующие процессы chromedriver.exe");
            Runtime.getRuntime().exec("cmd /c taskkill /IM chromedriver.exe /F");
        logger.info(">>> (beforeTest) Устанавливает настройки тестовой среды из файла [config.properties]");
        ConfigContainer.getInstance().loadConfig().generatePathToTempFolder();

        // region Выводим дополнительную информацию о компьютере, на котором выполняются тесты

        logger.info(">>> (beforeTest) Computer name is : " + InetAddress.getLocalHost().getHostName());
        logger.info(">>> (beforeTest) IP address is    : " + InetAddress.getLocalHost().getHostAddress());
        logger.info(">>> (beforeTest) OS name is       : " + System.getProperty("os.name"));
        logger.info(">>> (beforeTest) OS version is    : " + System.getProperty("os.version"));
        logger.info(">>> (beforeTest) User logged in as: " + System.getProperty("user.name"));

        // endregion

        // Инициализируем статический экземпляр WebDriver
        WebDriverContainer.getInstance().setWebDriver();
        new CommonPage().toMainPage();
    }

    /**
     * Код, который выполняется после каждого сценария.
     */
    @After
    public void tearDown(Scenario scenario) throws Exception {
        // Получаем текущий экземпляр WebDriver
        WebDriver driver = WebDriverContainer.getInstance().getWebDriver();

        // Делаем скриншот в случае аварийного завершения теста
        if (scenario.isFailed()) {
            try {
                scenario.attach(WebDriverRunner.getWebDriver().getCurrentUrl(), "URL","Current Page URL");
                byte[] screenshot = ((TakesScreenshot) WebDriverRunner.getWebDriver())
                        .getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png","screenshot");
                new File("target/site").mkdir();
                try {
                    String pathToDeleteFolder = ConfigContainer.getInstance().loadConfig().getPathToTempFolderWithRandomName();
                    logger.info(">>> (afterTest) Попытка удаления временной папки: " + pathToDeleteFolder);
                    File folderToDelete = new File(pathToDeleteFolder);
                    folderToDelete.delete();
                    logger.info(">>> (afterTest) Временная папка удалена");
                } catch (Exception e) {
                    logger.info(">>> (afterTest) Попытка удаления временной папки провалена: " + e);
                }
            } catch (WebDriverException somePlatformsNotSupportScreenshots) {
                logger.error(somePlatformsNotSupportScreenshots.getMessage());
            }
        }

        // Печатаем ошибки, которые выводятся в консоли браузера
        printErrorsFromBrowserConsole(driver);
//        printPathsOfRequestsWithErrorsFromNetwork(driver);

        logger.info(">>> (afterTest) Завершение сеанса");
        driver.quit();

        // Печатаем список использованных в текущем тесте параметров
        ConfigContainer.getInstance().printParameters();

        // Пауза для успешного завершения
        sleep(6000);
    }

    /**
     * Выводит ошибки, которые отображаются в консоли браузера
     *
     * @param driver экземпляр драйвера, с которым мы работаем
     */
    void printErrorsFromBrowserConsole(WebDriver driver) {
        logger.info(">>> (afterTest) Ошибки консоли браузера:");
        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : logEntries) {
            logger.error(entry.toString());
        }
    }

}
