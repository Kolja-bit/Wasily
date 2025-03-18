package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
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
import java.util.Map.Entry;
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
    private Map<String,List<IndexModel>> hashMap=null;
    private HashMap<Integer,Double> sortedMap=null;
    private Integer numberOfPagesOnSite=0;
    private final LemmasIndexingServiceImpl lemmasIndexingService;
    private volatile boolean controlNull=false;
    public SearchResult getSearch(String query, String stringUrl, int offset, int limit){

        SearchResult response=new SearchResult();
        if (query.isEmpty()){
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
        }else {
            int countPage=0;
            List<SearchResultQuery> searchResultQueryList=null;
            SitesModel sitesModel=siteRepository.findByUrl(stringUrl).get();
            String urlSite=stringUrl.substring(0,stringUrl.length()-1);
            String nameSite=sitesModel.getName();
            numberOfPagesOnSite=pageRepository.countPageBySite(sitesModel);
            getUniqueMapLemmas(query,sitesModel);
                if (controlNull){
                searchResultQueryList=new ArrayList<>(0);
                    countPage=0;
                    controlNull=false;
            }else {
                    getListOfPagesCorrespondingToLemmasFromQuery();
                searchResultQueryList=getListOfAnswersByQuery(urlSite,nameSite);
                        countPage=sortedMap.size();
                        log.info(String.valueOf(sortedMap));
                }
            response.setResult(true);
            response.setCount(countPage);
            response.setData(searchResultQueryList);
        }
        return response;
    }
        public boolean getUniqueMapLemmas(String query, SitesModel sitesModel){
        List<String> uniqueListLemmas=lemmasIndexingService.getListLemmas(query);
        hashMap=new HashMap<>();
        for (String lemma:uniqueListLemmas){
            if (lemmasRepository.findBySiteAndLemma(sitesModel,lemma).isPresent()) {
                LemmaModel lemmaModel = lemmasRepository.findBySiteAndLemma(sitesModel, lemma).get();
                Integer lemmaId=lemmaModel.getId();
                List<IndexModel> indexModelByLemma = indexRepository.findAllByLemmaId(lemmaId);
                hashMap.put(lemma,indexModelByLemma);
            }else {
                controlNull=true;
                String s="слово "+lemma+" отсутствует на страницах сайта "+sitesModel.getName();
            }
        }
        return controlNull;
    }
    public List<LemmaModel> getOptimalSortedListLemmas(){
        List<String> optimalListLemmas=new ArrayList<>();
        List<LemmaModel> optimalListLemmaModel=new ArrayList<>();
        for (Entry<String,List<IndexModel>> entry:hashMap.entrySet()){
            int maxNumberOfPagesWithLemma =entry.getValue().size();
            log.info(String.valueOf(maxNumberOfPagesWithLemma));
            //int leftoverPagesByLemma=numberOfPagesOnSite-maxNumberOfPagesWithLemma;
            int leftoverPagesByLemma=maxNumberOfPagesWithLemma/numberOfPagesOnSite;
            double optimalCountPage=maxNumberOfPagesWithLemma/numberOfPagesOnSite;

            log.info(String.valueOf(optimalCountPage));
            //не работает
            if (optimalCountPage<=0.2){
            //if (leftoverPagesByLemma<=0.2){
                optimalListLemmaModel.add(lemmasRepository.findByLemma(entry.getKey()).get());
                optimalListLemmas.add(lemmasRepository.findByLemma(entry.getKey()).get().getLemma());
            }
        }
        List<LemmaModel> optimalSortedListLemmaModel = optimalListLemmaModel.stream()
                .sorted(Comparator.comparing(LemmaModel::getFrequency))
                .collect(Collectors.toList());
        return optimalSortedListLemmaModel;
    }
        public boolean getListOfPagesCorrespondingToLemmasFromQuery(){
        List<LemmaModel> optimalSortedListLemmaModel=getOptimalSortedListLemmas();
        List<PageModel> listPageModelId=null;
        if (!optimalSortedListLemmaModel.isEmpty()) {
            Integer idLemma = optimalSortedListLemmaModel.get(0).getId();
             listPageModelId = indexRepository.findAllByLemmaId(idLemma)
                    .stream()
                    .map(IndexModel::getPage)
                    .collect(Collectors.toList());
            for (int i = 1; i < optimalSortedListLemmaModel.size(); i++) {
                Integer idLemma1 = optimalSortedListLemmaModel.get(i).getId();
                List<PageModel> listPageModelId1 = indexRepository.findAllByLemmaId(idLemma1)
                        .stream()
                        .map(IndexModel::getPage)
                        .collect(Collectors.toList());
                listPageModelId.retainAll(listPageModelId1);
            }
        }else {
            controlNull=true;
        }
        getMapPageIdByRelevance(listPageModelId);
        log.info(String.valueOf(controlNull));
        return controlNull;
    }
    public HashMap<Integer,Double> getMapPageIdByRelevance(List<PageModel> listPageModelId){
        Map<Integer,Double> mapAbsoluteRelevancePages=new HashMap<>();
        for (PageModel pageModel:listPageModelId){
            double absoluteRelevancePages=0.0;
            Integer idPage=pageModel.getId();
            for (LemmaModel lemmaModel:getOptimalSortedListLemmas()){
                Integer idLemma2=lemmaModel.getId();
                IndexModel indexModelPages=indexRepository.findByPageIdAndLemmaId(idPage,idLemma2).get();
                absoluteRelevancePages+=indexModelPages.getRank();
            }
            mapAbsoluteRelevancePages.put(idPage,absoluteRelevancePages);
        }
        double relevanceMax=mapAbsoluteRelevancePages.values()
                .stream()
                .max(Double::compareTo).get();
        for (Entry<Integer,Double> entry:mapAbsoluteRelevancePages.entrySet()){
            double value=entry.getValue()/relevanceMax;
            double scale = Math.pow(10, 1);
            double rel = Math.ceil(value * scale) / scale;
            mapAbsoluteRelevancePages.put(entry.getKey(),rel);
        }
        sortedMap = mapAbsoluteRelevancePages.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return sortedMap;
    }
    public List<SearchResultQuery> getListOfAnswersByQuery(String urlSite,String nameSite){
        List<SearchResultQuery> searchResultQueryList=new ArrayList<>();
            for (Map.Entry<Integer,Double> entry:sortedMap.entrySet()){
            SearchResultQuery resultQuery=new SearchResultQuery();
            PageModel page=pageRepository.findById(entry.getKey()).get();
            String patchPage=page.getPath();
            String content=page.getContent();
            String str1="";
            StringBuilder stringBuilder=new StringBuilder();
            resultQuery.setSite(urlSite);
            resultQuery.setSiteName(nameSite);
            resultQuery.setUri(patchPage);
            resultQuery.setTitle(Jsoup.parse(content).title());
            resultQuery.setRelevance(entry.getValue());

            String cleanContent = Jsoup.parse(content).text();
            String s=cleanContent.substring(0,300);
            String lemmaString="<b>"+s+"</b>";
            resultQuery.setSnippet(lemmaString);

            searchResultQueryList.add(resultQuery);
        }
        return searchResultQueryList;
    }


}
