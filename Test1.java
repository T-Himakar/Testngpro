package TestFlipkart;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class Test1 {

    WebDriver driver;
    WebDriverWait wait;

    // Extent objects
    static ExtentReports extent;
    static ExtentTest test;

    // Locators
    By searchBox       = By.xpath("//input[@name='q']");
    By searchBtn       = By.xpath("//button[@type='submit']");
    By closeLoginPopup = By.xpath("//button[contains(@class,'_2KpZ6l') and contains(@class,'_2doB4z')]");

    // ---------- Extent setup ----------
    @BeforeSuite
    public void setupReport() {
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/FlipkartReport.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);

        extent.setSystemInfo("Project", "Flipkart E2E");
        extent.setSystemInfo("Tester", "Himakar");
    }

    // ---------- Browser setup ----------
    @BeforeClass
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        System.out.println("Browser Launched");
    }

    @Test
    public void openFlipkartAndAddFirstMobileToCart() {

        // create test node in report
        test = extent.createTest("Flipkart - Till Payment Page");

        driver.get("https://www.flipkart.com/");
        test.info("Opened Flipkart home page");

        // Close login popup if present
        try {
            wait.until(ExpectedConditions.elementToBeClickable(closeLoginPopup)).click();
            test.info("Login popup closed");
        } catch (Exception e) {
            test.info("Login popup not displayed");
        }

        // Search mobiles
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox)).sendKeys("mobiles");
        wait.until(ExpectedConditions.elementToBeClickable(searchBtn)).click();
        test.info("Searched for 'mobiles'");

        // Wait for product links
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a[href*='/p/']")));
        List<WebElement> products = driver.findElements(By.cssSelector("a[href*='/p/']"));
        test.info("Number of products found: " + products.size());

        if (products.size() == 0) {
            test.fail("No products found on results page");
            return;
        }

        WebElement firstProduct = products.get(0);
        String title = firstProduct.getAttribute("title");
        test.info("Clicking first product: " + title);
        firstProduct.click();

        // Switch to new tab
        String parent = driver.getWindowHandle();
        Set<String> windows = driver.getWindowHandles();
        for (String w : windows) {
            if (!w.equals(parent)) {
                driver.switchTo().window(w);
                break;
            }
        }
        test.info("Switched to product detail tab");

        // Add to cart
        WebElement addToCart = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Add to cart']")));
        addToCart.click();
        test.pass("Product added to cart");

        // Place order (navigation towards payment page)
        WebElement placeOrderBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//span[normalize-space()='Place Order']")));
        placeOrderBtn.click();
        test.pass("Clicked on Place Order (navigated towards payment page)");
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
        System.out.println("Browser Closed");
    }

    // ---------- Flush report at the end ----------
    @AfterSuite
    public void flushReport() {
        extent.flush();
        System.out.println("Extent Report generated at: reports/FlipkartReport.html");
    }
}
