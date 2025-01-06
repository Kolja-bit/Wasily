package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import org.springframework.stereotype.Service;
import searchengine.config.Configuration;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.error.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.SiteIndexingStatus;
import searchengine.model.Sites;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

@Slf4j
@Service
@RequiredArgsConstructor
public class SitesIndexingService implements SitesIndexingServiceImpl {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmasRepository lemmasRepository;
    private final SitesList sitesList;
    private final IndexingResponse response = new IndexingResponse();
    private final Configuration configuration=new Configuration();

    public static volatile boolean control = false;


    @Override
    public IndexingResponse getStartIndexingSites() {
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAll();
        if (siteRepository.existsByStatus(SiteIndexingStatus.INDEXING)) {
            response.setResult(false);
            response.setError("Индексация уже запущена");
        }
        if (siteRepository.existsByStatus(SiteIndexingStatus.INDEXED) ||
                siteRepository.findAll().isEmpty()) {
            response.setResult(true);
            response.setError("");

            Thread thread=new Thread(()->{
                indexingSite();
            });
            thread.start();
        }
        return response;
    }


    @Override
    public  IndexingResponse getStopIndexingSites(){
        if (siteRepository.existsByStatus(SiteIndexingStatus.INDEXED) ||
                siteRepository.findAll().isEmpty()) {
            response.setResult(false);
            response.setError("Индексация не запущена");
        }
        if (siteRepository.existsByStatus(SiteIndexingStatus.INDEXING)||
                siteRepository.existsByStatus(SiteIndexingStatus.FAILED)||
                siteRepository.count()==sitesList.getSites().size()) {
            response.setResult(true);
            response.setError("");


                control = true;
                log.info(String.valueOf(control));


        }
        return response;
    }
    @Override
    public IndexingResponse addUpdatePage(String url){
        //String[] p= url.split("/");
        //String url1=p[0]+"//"+p[2]+"/";
        String url1=url.split("/")[0]+"//"+url.split("/")[2]+"/";
        Sites sites=siteRepository.findByUrl(url1).orElseThrow();
        String path = url.substring(url1.length() - 1);
        if (pageRepository.existsByPathAndSite(path,sites)){//ошибка нет добавления страницы
            //есть только обновление страницы

            Integer pageId=pageRepository.findByPathAndSite(path,sites).get().getId();
            pageRepository.deleteById(pageId);

            try {
                sleep(1000);
                Document doc=configuration.getDocument(url);
                Page page=new Page();
                page.setPath(path);
                page.setSite(sites);
                page.setContent(doc.html());
                page.setCode(doc.connection().response().statusCode());
                pageRepository.save(page);
                LemmasIndexingService lemmasIndexingService=new LemmasIndexingService(page.getContent(),
                        sites, lemmasRepository);
                lemmasIndexingService.recordLemmas();

            } catch (Exception e) {
                e.printStackTrace();
            }
            response.setResult(true);
            response.setError("");
        }else {
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов,"
                    +" указанных в конфигурационном файле");
        }

        return response;
    }

    public void indexingSite() {

        List<Site> list = sitesList.getSites();

        Thread thread=null;
        for (Site site : list) {
            thread=new Thread(()->{
                String siteUrl = site.getUrl();
                Sites sites = new Sites();
                sites.setStatus(SiteIndexingStatus.INDEXING);
                sites.setUrl(siteUrl);
                sites.setName(site.getName());
                sites.setStatusTime(LocalDateTime.now());
                siteRepository.save(sites);
                ForkJoinPool forkJoinPool = new ForkJoinPool(20);
                CrawlSitePages crawlSitePages = new CrawlSitePages(siteUrl, sites, siteRepository, pageRepository);
                forkJoinPool.invoke(crawlSitePages);


                log.info("vvvvv");
                log.info(Thread.currentThread().getName());
            });
            thread.start();
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
            /*for (Site str:list) {
                String s = str.getUrl();
                //Thread thread1 = new Thread(() -> {
                    //System.out.println(Thread.currentThread().getName());
                    CrawlSitePages obj = new CrawlSitePages(s);
                    //ForkJoinPool forkJoinPool=new ForkJoinPool(20);
                    forkJoinPool.invoke(obj);
                    if (!forkJoinPool.isTerminating()) {
                        System.out.println(Thread.currentThread().getName() + "Hello");
                    }
                //});
                //thread1.start();

            }*/



        /*List<Site> list = sitesList.getSites();
        for (int i = 0; i < list.size(); i++) {
            Sites sites = new Sites();
            sites.setStatus(SiteIndexingStatus.INDEXING);
            sites.setUrl(list.get(i).getUrl());
            sites.setName(list.get(i).getName());
            sites.setStatusTime(LocalDateTime.now());
            siteRepository.save(sites);
            CrawlSitePages crawlSitePages = new CrawlSitePages(list.get(i).getUrl());
            new ForkJoinPool().invoke(crawlSitePages);

            if (crawlSitePages.tr1.size() == 0) {
                sites.setStatus(SiteIndexingStatus.FAILED);
                sites.setLastError("Ошибка индексации сайта" + " - "
                        + list.get(i).getName());
                sites.setStatusTime(LocalDateTime.now());
                siteRepository.save(sites);
            }
            System.out.println(crawlSitePages.tr1.size());

            for (String url : crawlSitePages.tr1) {
                String path = url.substring(list.get(i).getUrl().length() - 1);
                try {
                    sleep(1000);

                    Document doc=configuration.getDocument(url);
                    Thread.interrupted();
                    Page page=new Page();
                    page.setPath(path);
                    page.setSite(sites);
                    page.setContent(doc.html());
                    page.setCode(doc.connection().response().statusCode());
                    pageRepository.save(page);
                    sites.setStatusTime(LocalDateTime.now());
                    siteRepository.save(sites);
                } catch (Exception e) {
                    if (e.getMessage().equals("sleep interrupted")){
                        sites.setStatus(SiteIndexingStatus.FAILED);
                        sites.setStatusTime(LocalDateTime.now());
                        sites.setLastError("Индексация остановлена пользователем");
                        siteRepository.save(sites);
                        return;
                    }else {
                        sites.setStatus(SiteIndexingStatus.FAILED);
                        sites.setStatusTime(LocalDateTime.now());
                        sites.setLastError("Ошибка индексации страницы" + " - "
                                + path + " - " + e.getMessage());
                        siteRepository.save(sites);
                    }

                }
            }
            if (siteRepository.findByStatus(SiteIndexingStatus.INDEXING).isPresent()) {
                sites.setStatus(SiteIndexingStatus.INDEXED);
                siteRepository.save(sites);
            }
        }
        configuration.getControl();
        log.info("Hello");*/



}
