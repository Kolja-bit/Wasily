package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.config.Configuration;
import searchengine.model.Page;
import searchengine.model.SiteIndexingStatus;
import searchengine.model.Sites;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;


public class CrawlSitePages extends RecursiveAction {
    private Configuration configuration=new Configuration();
    public static Set<String> uniqueURL = new HashSet<>();

    //public static String mySite="PlayBack.ru";
    private String url;
    private Sites site;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;



    public CrawlSitePages(String url,Sites site,SiteRepository siteRepository,PageRepository pageRepository) {
        this.url = url;
        this.site=site;
        this.siteRepository=siteRepository;
        this.pageRepository=pageRepository;
    }


    @Override
    protected void compute() {

            Set<CrawlSitePages> allTask = new HashSet<>();
            try {
                sleep(1000);
                Document doc = configuration.getDocument(url);
                Elements links = doc.select("a[href]");

                if (links.isEmpty()) {
                    site.setStatus(SiteIndexingStatus.INDEXED);
                    site.setStatusTime(LocalDateTime.now());
                    site.setLastError("Индексация сайта окончена");
                    siteRepository.save(site);
                    return;
                }
                if (SitesIndexingService.control){
                    site.setStatus(SiteIndexingStatus.FAILED);
                    site.setStatusTime(LocalDateTime.now());
                    site.setLastError("Индексация остановлена пользователем");
                    siteRepository.save(site);
                    return;
                }
                for (Element element:links){
                    String thisUrl=element.absUrl("href");
                    boolean add = uniqueURL.add(thisUrl);
                    if (add && thisUrl.contains(isMySite()) && !isLink(thisUrl)) {
                        indexingPage(thisUrl, site, doc);
                        CrawlSitePages task = new CrawlSitePages(thisUrl, site, siteRepository, pageRepository);
                        task.fork();
                        allTask.add(task);

                    }
                }


            } catch (Exception ex) {
                ex.printStackTrace();
                ex.getMessage();
            }
            for (CrawlSitePages crawlSitePages : allTask) {
                crawlSitePages.join();
            }


    }
    private synchronized void indexingPage(String currentUrl,Sites site, Document document){
        String path = currentUrl.substring(site.getUrl().length() - 1);
        //Page page=new Page();
        try {
            Page page=new Page();
            page.setPath(path);
            page.setSite(site);
            page.setContent(document.html());
            page.setCode(document.connection().response().statusCode());
            pageRepository.save(page);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private synchronized String isMySite(){
        String[] p= url.split("/");
        String url1=p[0]+"//"+p[2]+"/";
        return url1;
    }
    private static boolean isLink(String link){
        link.toLowerCase();
        return link.contains("youtube")||
                link.contains("JPG")||
                link.contains("tel:")||
                link.contains(".php")||
                link.contains(".jpg")||
                link.contains(".jpeg")||
                link.contains(".png")||
                link.contains(".gif")||
                link.contains(".webp")||
                link.contains(".pdf")||
                link.contains(".eps")||
                link.contains(".xlsx")||
                link.contains(".doc")||
                link.contains(".pptx")||
                link.contains(".docx")||
                link.contains(".?_ga")||
                link.contains("#");
    }

}



