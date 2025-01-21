package searchengine.services;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.Configuration;
import searchengine.model.PageModel;
import searchengine.model.SiteStatusModel;
import searchengine.model.SitesModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;


public class PagesIndexingServiceImpl extends RecursiveAction {
    private Configuration configuration=new Configuration();
    public static Set<String> uniqueURL = new HashSet<>();

    //public static String mySite="PlayBack.ru";
    private String url;
    private SitesModel site;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmasRepository lemmasRepository;
    private IndexRepository indexRepository;



    public PagesIndexingServiceImpl(String url, SitesModel site, SiteRepository siteRepository, PageRepository pageRepository,
                                    LemmasRepository lemmasRepository, IndexRepository indexRepository) {
        this.url = url;
        this.site=site;
        this.siteRepository=siteRepository;
        this.pageRepository=pageRepository;
        this.lemmasRepository=lemmasRepository;
        this.indexRepository=indexRepository;
    }


    @Override
    protected void compute() {

            Set<PagesIndexingServiceImpl> allTask = new HashSet<>();
            try {
                sleep(1000);
                Document doc = configuration.getDocument(url);
                Elements links = doc.select("a[href]");

                if (links.isEmpty()) {
                    site.setStatus(SiteStatusModel.INDEXED);
                    site.setStatusTime(LocalDateTime.now());
                    site.setLastError("Индексация сайта окончена");
                    siteRepository.save(site);
                    return;
                }
                if (SitesIndexingServiceImpl.control){
                    site.setStatus(SiteStatusModel.FAILED);
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
                        PagesIndexingServiceImpl task = new PagesIndexingServiceImpl(thisUrl, site, siteRepository,
                                pageRepository,lemmasRepository,indexRepository);
                        task.fork();
                        allTask.add(task);

                    }
                }


            } catch (Exception ex) {
                ex.printStackTrace();
                ex.getMessage();
            }
            for (PagesIndexingServiceImpl crawlSitePages : allTask) {
                crawlSitePages.join();
            }


    }
    public synchronized void indexingPage(String currentUrl, SitesModel site, Document document){
        String path = currentUrl.substring(site.getUrl().length() - 1);
        try {
            //sleep(1000);
            PageModel page=new PageModel();
            page.setPath(path);
            page.setSite(site);
            page.setContent(document.html());
            page.setCode(document.connection().response().statusCode());
            pageRepository.save(page);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);

            /*LemmasIndexingService lemmasIndexingService=new LemmasIndexingService(page.getContent(),
                    site,page, lemmasRepository,indexRepository);
            lemmasIndexingService.recordLemmas();*/

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



