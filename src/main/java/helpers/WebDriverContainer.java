package helpers;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelectorMode;
import com.codeborne.selenide.WebDriverProvider;
import com.codeborne.selenide.WebDriverRunner;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;

import static com.codeborne.selenide.Selenide.open;

public class WebDriverContainer {

    public static final Logger logger = LogManager.getLogger(WebDriverContainer.class);

    /**
     * Возвращает статический экземпляр этого класса (если класс еще не имеет экземпляра, то создает новый экземпляр).
     *
     * @return Статический экземпляр этого класса
     */
    public static synchronized WebDriverContainer getInstance() {
        return new WebDriverContainer();
    }

    /**
     * Возвращает экземпляр WebDriver (инициализирует его если он еще не инициализирован).
     *
     * @return экземпляр Selenium WebDriver
     */
    public WebDriver getWebDriver() {
        return WebDriverRunner.getWebDriver();
    }

    public void reWebDriver() {
        getInstance().getWebDriver().close();
        getInstance().setWebDriver();
    }

    public void reload() {
        getInstance().getWebDriver().navigate().refresh();
    }

    /**
     * Инициализирует статический экземпляр WebDriver.
     */
    public void setWebDriver() {
        this.setChromeDriver();
    }

    /**
     * Установки для работы с браузером Chromium.
     * путь к chrome.exe
     * путь к chromedriver.exe, который обеспечивает работу с chrome.exe
     * путь к плагину CryptoProExt.crx, который обеспечивает работу сертификатов
     */
    private void setChromeDriver() {
        Configuration.selectorMode = SelectorMode.Sizzle; //не убирать
        Configuration.pollingInterval = 50;

        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", ConfigContainer.getInstance().getPathToTempFolderWithRandomName());
        String drivers = ConfigContainer.getInstance().getConfigPropertyWithEncode("ExternalDriverPath");
        System.setProperty("webdriver.chrome.driver", drivers + "chromedriver.exe");
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.SEVERE);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        options.setCapability(ChromeOptions.LOGGING_PREFS, logPrefs);
        options.setCapability("goog:loggingPrefs", logPrefs); //Для работы с Chrome 75+ использовать следующую строку
        options.setBinary(new File(drivers + "chromium/chrome.exe"));
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL); // https://www.skptricks.com/2018/08/timed-out-receiving-message-from-renderer-selenium.html
        options.addArguments("start-maximized");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
        options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc

        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().deleteAllCookies();
        WebDriverRunner.setWebDriver(driver);

        // region Проверяем браузер и его версию
        Capabilities cap = driver.getCapabilities();
        String browserName = cap.getBrowserName().toLowerCase();
        String browserVersion = cap.getBrowserVersion().toLowerCase();
        logger.info(String.format(">>> (beforeTest) Browser is: %s version: %s", browserName, browserVersion));
        // endregion
    }

}