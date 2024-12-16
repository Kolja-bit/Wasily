package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

                /*links.stream().map((link) -> link.absUrl("href")).forEachOrdered((thisUrl) -> {
                    boolean add = uniqueURL.add(thisUrl);
                    if (add && thisUrl.contains(isMySite()) && !isLink(thisUrl)) {
                        indexingPage(thisUrl, site, doc);
                        //System.out.println(thisUrl);
                        CrawlSitePages task = new CrawlSitePages(thisUrl, site, siteRepository, pageRepository);
                        task.fork();
                        allTask.add(task);

                    }
                });*/

            } catch (Exception ex) {
                ex.printStackTrace();
                ex.getMessage();
            }
            for (CrawlSitePages crawlSitePages : allTask) {
                crawlSitePages.join();
            }


    }
    private synchronized void indexingPage(String string,Sites site, Document document){
        String path = string.substring(site.getUrl().length() - 1);
        Page page=new Page();
        try {
            page.setPath(path);
            page.setSite(site);
            page.setContent(document.html());
            page.setCode(document.connection().response().statusCode());
            pageRepository.save(page);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }catch (Exception e){
            page.setPath(path);
            page.setSite(site);
            page.setContent(e.getMessage());
            page.setCode(document.connection().response().statusCode());
            pageRepository.save(page);
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

    /*private  String url;
    private Configuration configuration=new Configuration();


    public CrawlSitePages(String url) {
        this.url = url;
    }

    public  TreeSet<String>tr1=new TreeSet<>();



    @Override
    protected TreeSet<String> compute() {
        List<CrawlSitePages> taskList=new ArrayList<>();
        try {
            sleep(1000);

            Document doc = configuration.getDocument(url);
            Elements elements = doc.select("a[href]");
            for (Element el:elements){
                String str = el.absUrl("href");
                if (!isLink(str) && isFile(str)) {
                        CrawlSitePages task = new CrawlSitePages(str);
                        task.fork();
                        taskList.add(task);
                    }

            }

            for (CrawlSitePages task:taskList){
                tr1.add(task.url);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return tr1;

    }

    private static boolean isLink(String link){
        link.toLowerCase();
        return link.contains(".jpg")||
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
    private synchronized boolean isFile(String link){
        link.toLowerCase();
        return link.contains(url);
    }*/

}



