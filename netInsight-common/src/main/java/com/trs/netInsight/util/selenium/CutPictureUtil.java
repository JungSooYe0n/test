package com.trs.netInsight.util.selenium;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author 李可南
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021年07月29日 09:54:00
 */
@Service
public class CutPictureUtil {
    /**
     * 网页转图片
     *
     *
     * @param savePath  保存图片地址
     * @param url	访问网页地址
     *
     * @return boolean
     */
    public static boolean saveHtml(String savePath,String url){

        // 谷歌驱动地址
        String path ="";
        String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")){  //微软系统
            path+="c:/Program Files (x86)/Google/Chrome/Application/chromedriver.exe";
        }else{//linux系统
            path+="/usr/bin/chromedriver";
        }
        //System.out.println(os);
        File file = new File(path);// path: chromedirver.exe文件路径
        System.out.println(file.exists());
        ChromeDriverService service =new  ChromeDriverService.Builder().usingDriverExecutable(file).usingAnyFreePort().build(); // 新建service 方便后面关闭chromedriver
        WebDriver driver = null;
        try {
            System.out.println("---进入try方法---");
            service.start(); // 开启服务
            ChromeOptions chromeOptions = new ChromeOptions();

            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--no-sandbox");//无头浏览器

            chromeOptions.addArguments("--disable-gpu");//无界面

            chromeOptions.addArguments("disable-infobars");//防止Chrome显示“Chrome正在被自动化软件控制”的通知

            chromeOptions.addArguments("lang=zh_CN.UTF-8");

            //设置必要参数
//            DesiredCapabilities dcaps = new DesiredCapabilities();
//            //ssl证书支持
//            chromeOptions.setCapability("acceptSslCerts", true);
//            //截屏支持
//            chromeOptions.setCapability("takesScreenshot", true);
//            //css搜索支持
//            chromeOptions.setCapability("cssSelectorsEnabled", true);

            //生成无头浏览器
            driver = new ChromeDriver(service, chromeOptions);
            //设置隐性等待
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            long start = System.currentTimeMillis();
            //打开页面
            driver.get(url);
            // 获取页面尺寸
            Long width = (Long) ((ChromeDriver) driver).executeScript("return Math.max(document.body.scrollWidth, " +
                    "document.body.offsetWidth, document.documentElement.clientWidth, document.documentElement" +
                    ".scrollWidth, document.documentElement.offsetWidth);");

            Long height = (Long) ((ChromeDriver) driver).executeScript("return Math.max(document.body.scrollHeight, " +
                    "document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement" +
                    ".scrollHeight, document.documentElement.offsetHeight);");
//          重新设置页面尺寸
            Dimension dimension = new Dimension(width.intValue()+100, height.intValue());
            driver.manage().window().setSize(dimension);
            Long time = 2000L;
            if(url.contains("weibo.com")){
                time = 5000L;
            }
            Thread.sleep(time);

            //指定了OutputType.FILE做为参数传递给getScreenshotAs()方法，其含义是将截取的屏幕以文件形式返回。
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Thread.sleep(2000L);
            FileCopyUtils.copy(srcFile, new File(savePath)); // 复制文件 savePath：图片保存路径
            System.out.println("图片生成完毕,耗时:"+ (System.currentTimeMillis() - start)+"ms");
            return true;
        } catch (Exception e) {
            System.out.println("保存异常,e="+ e.getMessage());
        } finally {
            // 关闭服务
            driver.quit();
            service.stop();
        }
        return false;
    }
}
