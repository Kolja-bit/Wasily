package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.controllers.ApiController;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Sites;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    //private final Random random = new Random();
    //private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    //private final LemmaRepository lemmaRepository;
    @Override
    public StatisticsResponse getStatistics() {
        StatisticsResponse response=new StatisticsResponse();
        StatisticsData statisticsData=new StatisticsData();
        List<Sites> sites=siteRepository.findAll();
        int totalSites=sites.size();
        int totalPages=(int) pageRepository.count();
        //int totalLemmas=(int) lemmaRepository.count();
        boolean indexinglnProgress=sites.stream().anyMatch(site ->site.getStatus().equals("INDEXING"));
        TotalStatistics total=new TotalStatistics();
        total.setSites(totalSites);
        total.setPages(totalPages);
        //total.setLemmas(totalLemmas);
        total.setIndexing(indexinglnProgress);
        statisticsData.setTotal(total);
        List<DetailedStatisticsItem> detailedList= sites.stream()
                .map(site -> {
                DetailedStatisticsItem detailed= new DetailedStatisticsItem();
                detailed.setUrl(site.getUrl());
                detailed.setName(site.getName());
                detailed.setStatus(String.valueOf(site.getStatus()));
                detailed.setStatusTime(site.getStatusTime());
                //detailed.setStatusTime(site.getStatusTime().toEpochSecond(ZoneOffset.UTC));
                detailed.setError(site.getLastError());
                detailed.setPages(pageRepository.countBySite(site));
                return detailed;

    }).collect(Collectors.toList());
        statisticsData.setDetailed(detailedList);
        response.setResult(true);
        response.setStatistics(statisticsData);
        return  response;
    }




        /*String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = random.nextInt(1_000);
            int lemmas = pages * random.nextInt(1_000);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(statuses[i % 3]);
            item.setError(errors[i % 3]);
            item.setStatusTime(System.currentTimeMillis() -
                    (random.nextInt(10_000)));
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }*/
}
