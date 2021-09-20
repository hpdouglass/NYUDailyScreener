package net.hpdouglass.nyudailyscreener;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A program that automatically fills out the NYU Daily Screener for you at 12:01 AM every morning.
 *
 * The program requires you to log in to your NYU account once to complete the screener, but then
 * just refreshes the result page every morning. Doing this re-submits your screener without having
 * to manually log in each day.
 *
 * @author Harrison Douglass
 */
public class Program {

    /**
     * Driver method to start the program. Determines if a username and password was supplied in the
     * arguments, and if so, starts as headless mode. Otherwise let user log in during the program.
     *
     * @param args Contains username and password, if supplied.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("You must provide the path to the Selenium Chrome Driver.");
            return;
        }

        System.setProperty("webdriver.chrome.driver", args[0]);
        if (args.length >= 3) new Program(args[1], args[2]);
        else new Program(null, null);
    }

    // Program settings.
    private static final String DAILY_SCREENER_URL = "https://www.nyu.edu/nyureturns/dailyscreener";
    private static final int TIMEOUT_SECONDS = 20;
    private static final int MFA_TIMEOUT_SECONDS = 3600;

    // Chrome instance.
    private final WebDriver driver;

    private final String username;
    private final String password;

    /**
     * Constructor for the program class.
     *
     * Initializes the Chrome instance the program will be running in.
     *
     * @param username The username to log in to NYU Home with, if any.
     * @param password The password to log in to NYU Home with, if any.
     */
    Program(String username, String password) {
        this.username = username;
        this.password = password;

        // Run headless if username and password are both present.
        ChromeOptions options = new ChromeOptions();
        if (runningHeadless()) options.addArguments("--headless");

        driver = new ChromeDriver(options);
        startProgram();
    }

    /**
     * The main guts of the program.
     *
     * Automatically fills out the daily screener, including the NYU Home username and password,
     * if it was supplied. The only part that requires manual intervention is the MFA portion.
     *
     * After the screener is completed, the program will refresh the window every morning at
     * 12:01 AM, so that the user receives an email with their pass for the day at that time.
     */
    private void startProgram() {
        // Navigate to the daily screener webpage, as given on the NYU website.
        driver.get(DAILY_SCREENER_URL);

        // Wait for the URL to change to qualtrics, then click the next button.
        new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS)).until(ExpectedConditions.urlContains("nyu.qualtrics.com"));
        driver.findElement(By.id("NextButton")).click();

        // Wait until the next section of questions loads.
        // Answer "yes" to question asking if you have a NetID
        // Then click the next button again.
        new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS)).until(ExpectedConditions.visibilityOfElementLocated(By.id("QR~QID2~1")));
        // Workaround for <span> parent that does not allow a direct click of the correct radio button.
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("arguments[0].click();", driver.findElement(By.id("QR~QID2~1")));
        driver.findElement(By.id("NextButton")).click();

        // If we were provided with a username and password, enter them now.
        if (runningHeadless()) {
            // Wait until we get the NYU Shibboleth "Login" button to appear.
            // Then auto-fill username and password, and press the "login" button.
            new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS)).until(ExpectedConditions.visibilityOfElementLocated(By.name("_eventId_proceed")));
            jse.executeScript("arguments[0].value = \"" + username + "\";", driver.findElement(By.name("j_username")));
            jse.executeScript("arguments[0].value = \"" + password + "\";", driver.findElement(By.name("j_password")));
            jse.executeScript("arguments[0].click();", driver.findElement(By.name("_eventId_proceed")));
        }

        // Wait for user to login, aka when we are back through the redirect chain at Qualtrics.
        // At the end of the chain, there will be a <div> with ID "EndOfSurvey"
        new WebDriverWait(driver, Duration.ofSeconds(MFA_TIMEOUT_SECONDS)).until(ExpectedConditions.visibilityOfElementLocated(By.id("EndOfSurvey")));

        // Infinite loop to then refresh every 12:01 AM, starting the next morning.
        // (No refresh immediately because the user just filled out the Daily Screener)
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Cancel the task if the browser window is no longer open.
                if (driver.toString().contains("null")) {
                    this.cancel();
                    return;
                }

                // Otherwise refresh the page to "fill out" the daily screener.
                System.out.println("Attempting to refresh page at: " + new Date());
                driver.navigate().refresh();
            }
        }, getDateOfFirstExecution(), 1000*60*60*24); // Run every 24 hours.

    }

    /**
     * Get the duration of time until the next refresh should be performed, that is,
     * the duration of time between the current time and 12:01 AM the next morning.
     *
     * @return The duration of time until 12:01 AM the next morning (as a java.time.Duration object).
     */
    public static Date getDateOfFirstExecution() {
        // Get current date/time, add one day, then set the time equal to 12:01 AM.
        Calendar scheduler = Calendar.getInstance();
        scheduler.add(Calendar.DAY_OF_YEAR, 1);
        scheduler.set(Calendar.HOUR, 0);
        scheduler.set(Calendar.MINUTE, 1);
        scheduler.set(Calendar.MILLISECOND, 0);

        return scheduler.getTime();
    }

    /**
     * Determine if the program is/should be running headless (i.e. without GUI).
     * @return True if the program is running headless, false otherwise.
     */
    private boolean runningHeadless() {
        return username != null && password != null;
    }

}