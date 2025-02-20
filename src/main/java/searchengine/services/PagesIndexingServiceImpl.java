package searchengine.services;


import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.Configuration;
import searchengine.model.PageModel;
import searchengine.model.SitesModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

@Slf4j
public class PagesIndexingServiceImpl extends RecursiveAction {
    //public class PagesIndexingServiceImpl  {
    private Configuration configuration=new Configuration();
    //public static Set<String> uniqueURL = new HashSet<>();
    public static Set<String> uniqueURL = new CopyOnWriteArraySet<>();
    private String url;
    private SitesModel site;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmasRepository lemmasRepository;
    private IndexRepository indexRepository;
    public static volatile boolean statusControlIndexed=false;
    public static volatile boolean statusControlFailed=false;



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
                    statusControlIndexed=true;
                    return;
                }
                if (SitesIndexingServiceImpl.control){
                    statusControlFailed=true;
                    return;
                }
                for (Element element:links){
                    String thisUrl=element.absUrl("href");
                    boolean add = uniqueURL.add(thisUrl);
                    if (add && thisUrl.contains(isMySite()) && !isLink(thisUrl)) {
                            indexingPage(thisUrl, site);
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

        public synchronized void indexingPage(String currentUrl, SitesModel site) {
            String path = currentUrl.substring(site.getUrl().length() - 1);
                if (!pageRepository.existsByPathAndSite(path, site)) {
                    try {
                        //sleep(1000);
                        PageModel page = new PageModel();
                        page.setPath(path);
                        page.setSite(site);
                        page.setContent(configuration.getDocument(currentUrl).html());
                        page.setCode(configuration.getDocument(currentUrl).connection().response().statusCode());
                        pageRepository.save(page);
                        log.info(String.valueOf(page.getPath()));

                        LemmasIndexingServiceImpl lemmasIndexingService =
                                new LemmasIndexingServiceImpl(page,lemmasRepository,indexRepository);
                        lemmasIndexingService.recordLemmas();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    site.setStatusTime(LocalDateTime.now());
                    siteRepository.save(site);
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

/*select
        s.id as site_id, s.name as site_name, s.url as site_url,
        count(p.id) as page_count_by_site,
        count(l.id) as lemma_count,
        count(i.id) as index_count_by_page
        from search_engine.site s
        join search_engine.page p on p.site_id = s.id
        left join search_engine.lemma l on l.site_id = s.id
        left join search_engine.search_index i on i.page_id = p.id
        group by s.id, s.url, s.name*/


