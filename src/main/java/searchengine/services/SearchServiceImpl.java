package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
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
    private List<SearchResultQuery> summaryOfSearchQueries=null;
    private Integer numberOfPagesOnSite=0;
    private final LemmasIndexingServiceImpl lemmasIndexingService;
    private volatile boolean controlNull=false;
    private volatile boolean controlData=true;

    public SearchResult getSearch(String query, String stringUrl, int offset, int limit){

        SearchResult response = null;
        try {
            List<String> n = null;
            boolean u = true;
            List<String> uniqueListLemmas1 = null;
            if (hashMap == null) {
                hashMap = new HashMap<>();
            }
            n = new ArrayList<>(hashMap.keySet());
            uniqueListLemmas1 = lemmasIndexingService.getListLemmas(query);
            u = uniqueListLemmas1.equals(n);
            if (query.isEmpty() || query == null) {
                response = new SearchResult();
                response.setResult(false);
                response.setError("Задан пустой поисковый запрос");
            } else {
                response = new SearchResult();
                String siteUrl1 = "";
                if (stringUrl == null) {
                    List<Site> listSites = sitesList.getSites();
                    for (Site site : listSites) {
                        siteUrl1 = site.getUrl();
                        if (controlData || !u) {
                            summaryOfSearchQueries = new ArrayList<>();
                            for (SearchResultQuery searchResultQuery : getCreatingResponse(siteUrl1, query)) {
                                summaryOfSearchQueries.add(searchResultQuery);
                            }
                        }
                    }

                } else {
                    siteUrl1 = stringUrl;
                    if (controlData || !u) {
                        System.out.println(controlData);
                        summaryOfSearchQueries = getCreatingResponse(siteUrl1, query);
                    }

                }

                response.setResult(true);
                response.setCount(sortedMap.size());
                List<SearchResultQuery> stream = summaryOfSearchQueries
                        .stream()
                        //.skip(0)
                        //.limit(3)
                        .skip(offset)
                        .limit(limit)
                        .collect(Collectors.toList());
                response.setData(stream);
                summaryOfSearchQueries.removeAll(stream);
                if (0 < summaryOfSearchQueries.size() && summaryOfSearchQueries.size() < sortedMap.size()) {
                    controlData = false;

                }
                if (summaryOfSearchQueries.size() == 0) {
                    summaryOfSearchQueries = new ArrayList<>();
                    sortedMap = new HashMap<>();
                    controlData = true;
                }
            }
        }catch (Exception e){
            e.getMessage();
            response.setError("500  Ошибка сервера");
        }
        return response;
    }

    public List<SearchResultQuery> getCreatingResponse(String string,String query){

        List<SearchResultQuery> searchResultQueryList=null;
        SitesModel sitesModel=siteRepository.findByUrl(string).get();
        String urlSite=string.substring(0,string.length()-1);
        String nameSite=sitesModel.getName();
        numberOfPagesOnSite=pageRepository.countPageBySite(sitesModel);
        getUniqueMapLemmas(query,sitesModel);
        if (controlNull){
            searchResultQueryList=new ArrayList<>(0);
            controlNull=false;
        }else {
            getListOfPagesCorrespondingToLemmasFromQuery();
            searchResultQueryList=getListOfAnswersByQuery(urlSite,nameSite);
        }
        return searchResultQueryList;
    }
        public boolean getUniqueMapLemmas(String query, SitesModel sitesModel){
        List<String> uniqueListLemmas=lemmasIndexingService.getListLemmas(query);
        hashMap=new HashMap<>();
        for (String lemma:uniqueListLemmas){
            if (lemmasRepository.findBySiteAndLemma(sitesModel,lemma).isPresent()) {
                System.out.println("Hello2");
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
            //int leftoverPagesByLemma=numberOfPagesOnSite-maxNumberOfPagesWithLemma;
            int leftoverPagesByLemma=maxNumberOfPagesWithLemma/numberOfPagesOnSite;
            double optimalCountPage=maxNumberOfPagesWithLemma/numberOfPagesOnSite;
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
            resultQuery.setSite(urlSite);
            resultQuery.setSiteName(nameSite);
            resultQuery.setUri(patchPage);
            resultQuery.setTitle(Jsoup.parse(content).title());
            resultQuery.setRelevance(entry.getValue());
            resultQuery.setSnippet(String.valueOf(getResultingFragment(content)));
            searchResultQueryList.add(resultQuery);
        }
        return searchResultQueryList;
    }
    public StringBuilder getResultingFragment(String string){
        List<LemmaModel> lemmaModelList=getOptimalSortedListLemmas();
        List<String> list=lemmaModelList
                .stream()
                .map(LemmaModel::getLemma)
                .collect(Collectors.toList());
        StringBuilder stringBuilder=new StringBuilder();
        String fragmentText=Jsoup.parse(string).text();
        for (String lemma:list) {
            int indentBeforeLemma=0;
            int indentAfterLemma=0;
            int indexLemma = fragmentText.toLowerCase().indexOf(lemma.substring(0, lemma.length() - 2));
            //int indexLemma = fragmentText.toLowerCase().indexOf(lemma.substring(0, lemma.length() - 1));
            if (indexLemma>151){
                indentBeforeLemma=150;
            }else {
                indentBeforeLemma=0;
            }
            if (fragmentText.length()-(indexLemma+lemma.length())>151){
                indentAfterLemma=150;
            }else {
                indentAfterLemma=0;
            }
            String stringBeforeLemma=fragmentText.substring(indexLemma - indentBeforeLemma, indexLemma);
            String stringLemma="<b>"+ fragmentText.substring(indexLemma, indexLemma + lemma.length()) +"</b>";
            String stringAfterLemma=fragmentText.substring(indexLemma + lemma.length(),
                    indexLemma + lemma.length() + indentAfterLemma);
            stringBuilder.append(stringBeforeLemma)
                    .append(stringLemma)
                    .append(stringAfterLemma).append(" ");
        }
        return stringBuilder;
    }


}
