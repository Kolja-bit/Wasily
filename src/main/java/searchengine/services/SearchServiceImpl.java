package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.search.SearchResult;
import searchengine.dto.search.SearchResultQuery;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SitesModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmasRepository lemmasRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;
    public SearchResult getSearch(String query, String stringUrl, int offset, int limit){

        SearchResult response=new SearchResult();
        if (query.isEmpty()){
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
        }else {
            LemmasIndexingServiceImpl lemmasIndexingService=
                    new LemmasIndexingServiceImpl(query);
            List<String> uniqueListLemmas=lemmasIndexingService.getListLemmas();
            SitesModel sitesModel=siteRepository.findByUrl(stringUrl).get();
            String urlSite=stringUrl.substring(0,stringUrl.length()-1);
            String nameSite=sitesModel.getName();
            List<PageModel> listPageByLemmaId=new ArrayList<>();
            for (String lemma:uniqueListLemmas){
                if (lemmasRepository.findBySiteAndLemma(sitesModel,lemma).isPresent()) {
                    LemmaModel lemmaModel = lemmasRepository.findBySiteAndLemma(sitesModel, lemma).get();
                    Integer lemmaId=lemmaModel.getId();
                    IndexModel indexModel=indexRepository.findByLemmaId(lemmaId).get();
                    PageModel pageByLemmaId=indexModel.getPage();
                    listPageByLemmaId.add(pageByLemmaId);
                }else {
                    String s="слово "+lemma+" отсутствует на страницах сайта "+nameSite;
                }
            }




            double relevance=0.0;
            SearchResultQuery resultQuery=new SearchResultQuery();
            resultQuery.setSite(urlSite);
            resultQuery.setSiteName(nameSite);
            resultQuery.setUri("ddgnf");
            resultQuery.setTitle("cvcbj");
            resultQuery.setSnippet(" gfthjnmi");
            resultQuery.setRelevance(relevance);

            response.setResult(true);
            response.setCount(5);
            response.setData(Collections.singletonList(resultQuery));
        }
        return response;
    }
}
