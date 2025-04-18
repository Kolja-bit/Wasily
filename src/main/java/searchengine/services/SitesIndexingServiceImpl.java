package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Configuration;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.error.IndexingResponse;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SitesIndexingServiceImpl implements SitesIndexingService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmasRepository lemmasRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;
    private final IndexingResponse response = new IndexingResponse();
    private final Configuration configuration=new Configuration();

    public static volatile boolean control = false;


    @Override
    public IndexingResponse getStartIndexingSites() {
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAll();
        if (siteRepository.existsByStatus(SiteStatusModel.INDEXING)) {
            response.setResult(false);
            response.setError("Индексация уже запущена");
        }
        if (siteRepository.existsByStatus(SiteStatusModel.INDEXED) ||
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
        if (siteRepository.existsByStatus(SiteStatusModel.INDEXED) ||
                siteRepository.findAll().isEmpty()) {
            response.setResult(false);
            response.setError("Индексация не запущена");
        }
        if (siteRepository.existsByStatus(SiteStatusModel.INDEXING)||
                siteRepository.existsByStatus(SiteStatusModel.FAILED)||
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
        String url1=url.split("/")[0]+"//"+url.split("/")[2]+"/";
        SitesModel sites=siteRepository.findByUrl(url1).orElseThrow();
        String path = url.substring(url1.length() - 1);
        PageModel pageModel =pageRepository.findByPathAndSite(path, sites).get();
        if (siteRepository.existsByUrl(url1)){
            if (pageRepository.existsByPathAndSite(path,sites)) {
                deletePageModelIndexModelLemmaModel(pageModel,path,sites);
            }
                try {
                    PagesIndexingServiceImpl pagesIndexingService=new PagesIndexingServiceImpl(url,
                            sites,siteRepository,pageRepository,lemmasRepository,indexRepository);
                    pagesIndexingService.indexingPage(url,sites);


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
                SitesModel sites = new SitesModel();
                sites.setStatus(SiteStatusModel.INDEXING);
                sites.setUrl(siteUrl);
                sites.setName(site.getName());
                sites.setStatusTime(LocalDateTime.now());
                siteRepository.save(sites);
                ForkJoinPool forkJoinPool = new ForkJoinPool(20);
                PagesIndexingServiceImpl crawlSitePages = new PagesIndexingServiceImpl(siteUrl, sites, siteRepository,
                        pageRepository,lemmasRepository,indexRepository);
                forkJoinPool.invoke(crawlSitePages);

                if (crawlSitePages.statusControlIndexed && crawlSitePages.statusControlFailed==false){
                    sites.setStatus(SiteStatusModel.INDEXED);
                    sites.setStatusTime(LocalDateTime.now());
                    sites.setLastError("Индексация сайта окончена");
                    siteRepository.save(sites);
                    configuration.getControl();
                }
                if (crawlSitePages.statusControlFailed){
                    sites.setStatus(SiteStatusModel.FAILED);
                    sites.setStatusTime(LocalDateTime.now());
                    sites.setLastError("Индексация остановлена пользователем");
                    siteRepository.save(sites);
                }

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

    public  void deletePageModelIndexModelLemmaModel(PageModel pageModel,String path,SitesModel sitesModel){
        Integer pageId=pageModel.getId();
        List<IndexModel> listIndexModel=indexRepository.findAllByPageId(pageId);
        List<Integer>listIdIndexModel=listIndexModel
                .stream()
                .map(IndexModel::getId)
                .collect(Collectors.toList());

        HashMap<Integer,Long> mapLemmaIdRank=new HashMap<>();
        for (IndexModel indexModel:listIndexModel){
            mapLemmaIdRank.put(indexModel.getLemma().getId(),indexModel.getRank().longValue());
        }

        if (!listIdIndexModel.isEmpty()) {
            indexRepository.deleteAllByIdInBatch(listIdIndexModel);
        }

        for (Map.Entry<Integer,Long>entry: mapLemmaIdRank.entrySet()){
            LemmaModel lemmaModel=lemmasRepository.findById(entry.getKey()).get();
            Integer newFrequency=lemmaModel.getFrequency()-entry.getValue().intValue();
            lemmaModel.setFrequency(newFrequency);
            lemmasRepository.save(lemmaModel);
        }
        pageRepository.deleteById(pageId);
    }
}
