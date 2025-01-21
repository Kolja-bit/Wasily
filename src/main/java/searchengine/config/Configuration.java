package searchengine.config;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.event.KeyEvent;


@Component
public class Configuration {
    private String user="Mozilla/5.0 (Windows; U; WindowsNT 5.1;" +
            " en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private String referrer="http://www.google.com";

    @SneakyThrows
    public Document getDocument(String url)  {
        Document doc = Jsoup.connect(url).userAgent(user).referrer(referrer).get();
        return doc;
    }
    @SneakyThrows
    public void getControl(){
        System.setProperty("java.awt.headless", "false");
        Robot robot=new Robot();
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    /*<dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>4.19.1</version>
        </dependency>*/
}
