package steps;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Обеспечивает запуск тестов в Cucumber с указанными опциями (см. аннотацию @CucumberOptions).
 * Created by Vladimir V. Klochkov on 20.04.2016.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "json:target/cucumber.json",
                "pretty", "html:target/site/cucumber-pretty", "json:target/cucumber.json"
        },
        features = "src/test/resources/features",
        tags = "~@ignore"
)


//-Dcucumber.options="--tags @integration-sab-request" -PServicing',
public class CucumberRunnerTest {
    // Этот класс всегда должен быть пустым ! Имя класса обязательно должно закачиваться словом Test !
}
