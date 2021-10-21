import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.safari.SafariDriver

environments {
    // You need to configure in Safari -> Develop -> Allowed Remote Automation
    safari {
        driver = { new SafariDriver() }
    }

    // run via “./gradlew -Dgeb.env=chrome -Dwebdriver.chrome.driver=/Applications/chromedriver iT”
    chrome {
        driver = { new ChromeDriver() }
    }

    // run via “./gradlew -Dgeb.env=chromeHeadless -Dwebdriver.chrome.driver=/Applications/chromedriver iT”
    chromeHeadless {
        driver = {
            ChromeOptions o = new ChromeOptions()
            o.addArguments('headless')
            new ChromeDriver(o)
        }
    }

    // run via “./gradlew -Dgeb.env=firefoxHeadless iT”
    firefoxHeadless {
        driver = {
            FirefoxOptions o = new FirefoxOptions()
            o.addArguments('-headless')
            new FirefoxDriver(o)
        }
    }

    // run via “./gradlew -Dgeb.env=firefox iT”
    firefox {
        driver = { new FirefoxDriver() }
    }

    // run via “./gradlew -Dgeb.env=htmlUnit iT”
    htmlUnit {
        driver = { new HtmlUnitDriver() }
    }
}
