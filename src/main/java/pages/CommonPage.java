package pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import org.junit.Assert;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static java.lang.String.format;

//todo добавить классам описание
public class CommonPage extends AbstractPage {

    /*******************************************************************************************************************
     *                                             Локаторы элементов страницы.
     ******************************************************************************************************************/

    // Логотип маркета для перехода на главную страницу
    private final SelenideElement mainLogo = $("#headerPanelLogo");
    //------------------------------------------------------------------------------------------------------------------
    // Карточка сделки по её номеру
    private static final String DEAL_WITH_ID = "[id='%s']";
    //------------------------------------------------------------------------------------------------------------------

    /*******************************************************************************************************************
     *
     *                                        Методы страницы
     *
     ******************************************************************************************************************/

    /**
     * Нажимает на главное лого РТС-ТЕНДЕР Маркет
     */
    public CommonPage pressMainLogo() {
        logger.info("Ожидает главное лого");
        mainLogo.shouldBe(visible, duration);
        sleep(normDelayTimeMs);
        logger.info("Нажимает на главное лого");
        mainLogo.click();
        sleep(normDelayTimeMs);
        return this;
    }


}