package com.test.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class DriverManager {

	public static ThreadLocal<WebDriver> threadDriver = new ThreadLocal<WebDriver>();

	public static WebDriver getDriver() {
		WebDriver driver = DriverManager.threadDriver.get();
		if (driver == null) {
			System.setProperty("webdriver.chrome.driver","E:\\workspace\\eclipse4.5\\ATP\\webDriver\\chromedriver.exe");
			driver = new  ChromeDriver();
			//			driver = new FirefoxDriver();
			threadDriver.set(driver);			
		}
		return driver;
	}

}
