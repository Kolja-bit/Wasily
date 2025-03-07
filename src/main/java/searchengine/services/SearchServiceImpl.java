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

import java.util.*;
import java.util.stream.Collectors;

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
            // лист лемм по запросу
            LemmasIndexingServiceImpl lemmasIndexingService=
                    new LemmasIndexingServiceImpl(query);
            List<String> uniqueListLemmas=lemmasIndexingService.getListLemmas();
            SitesModel sitesModel=siteRepository.findByUrl(stringUrl).get();
            String urlSite=stringUrl.substring(0,stringUrl.length()-1);
            String nameSite=sitesModel.getName();
            int numberOfPagesOnSite=pageRepository.countPageBySite(sitesModel);
            Map<String,List<IndexModel>> hashMap=new HashMap<>();
            for (String lemma:uniqueListLemmas){
                if (lemmasRepository.findBySiteAndLemma(sitesModel,lemma).isPresent()) {
                    LemmaModel lemmaModel = lemmasRepository.findBySiteAndLemma(sitesModel, lemma).get();
                    Integer lemmaId=lemmaModel.getId();
                    List<IndexModel> indexModelByLemma = indexRepository.findAllByLemmaId(lemmaId);
                    hashMap.put(lemma,indexModelByLemma);
                }else {
                    String s="слово "+lemma+" отсутствует на страницах сайта "+nameSite;
                }
            }
            //оптимальный не сортированный лист лемм с удалением наибольшего количества страниц
            List<String> optimalListLemmas=new ArrayList<>();
            List<LemmaModel> optimalListLemmaModel=new ArrayList<>();
            for (Map.Entry<String,List<IndexModel>> entry:hashMap.entrySet()){
                int maxNumberOfPagesWithLemma =entry.getValue().size();
                int optimalCountPage=numberOfPagesOnSite-maxNumberOfPagesWithLemma;
                if (optimalCountPage<=20){
                    optimalListLemmaModel.add(lemmasRepository.findByLemma(entry.getKey()).get());
                }
            }
            // оптимальный сортированный лист лемм
            List<LemmaModel> optimalSortedListLemmaModel = optimalListLemmaModel.stream()
                    .sorted(Comparator.comparing(LemmaModel::getFrequency))
                    .collect(Collectors.toList());
            // поиск страниц по первой самой редкой лемме
            Integer idLemma=optimalSortedListLemmaModel.get(0).getId();
            List<PageModel> listPageModelId =indexRepository.findAllByLemmaId(idLemma)
                    .stream()
                    .map(IndexModel::getPage)
                    .collect(Collectors.toList());
            for (int i=1;i<optimalSortedListLemmaModel.size();i++){
                Integer idLemma1=optimalSortedListLemmaModel.get(i).getId();
                List<PageModel> listPageModelId1 =indexRepository.findAllByLemmaId(idLemma1)
                        .stream()
                        .map(IndexModel::getPage)
                        .collect(Collectors.toList());
                listPageModelId.removeAll(listPageModelId1);
            }
            if (listPageModelId.isEmpty()){
                // выводить пустой список listPageModelId
                // возможно response.setData(Collections.singletonList(resultQuery));
                // resultQuery=null
            }else {

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
