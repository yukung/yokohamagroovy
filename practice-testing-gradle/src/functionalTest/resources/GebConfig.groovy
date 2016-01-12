import org.openqa.selenium.firefox.FirefoxDriver

waiting {
    timeout = 2
}

environments {
    firefox {
        driver = { new FirefoxDriver() }
    }
}

baseUrl = "http://localhost:8080/"
