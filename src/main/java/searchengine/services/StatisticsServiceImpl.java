package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SitesModel;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmasRepository lemmaRepository;
    @Override
    public StatisticsResponse getStatistics() {

        List<SitesModel> sitesList = siteRepository.findAll();
        TotalStatistics total = new TotalStatistics();
        total.setSites(sitesList.size());
        //total.setIndexing(true);
        boolean indexing=sitesList.stream().anyMatch(site ->site.getStatus().equals("INDEXING"));
        total.setIndexing(indexing);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
            for(SitesModel site: sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = pageRepository.countPageBySite(site);
            int lemmas = lemmaRepository.countLemmaBySite(site);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(String.valueOf(site.getStatus()));
            item.setError(site.getLastError());
            item.setStatusTime(site.getStatusTime());
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
    }
}
/*StatisticsResponse response=new StatisticsResponse();
        StatisticsData statisticsData=new StatisticsData();
        List<SitesModel> sites=siteRepository.findAll();
        int totalSites=sites.size();
        int totalPages=(int) pageRepository.count();
        int totalLemmas=(int) lemmaRepository.count();
        boolean indexinglnProgress=sites.stream().anyMatch(site ->site.getStatus().equals("INDEXING"));
        TotalStatistics total=new TotalStatistics();
        total.setSites(totalSites);
        total.setPages(totalPages);
        total.setLemmas(totalLemmas);
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
    }*/


        /*String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };*/
