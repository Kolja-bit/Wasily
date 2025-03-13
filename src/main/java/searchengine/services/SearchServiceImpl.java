package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
    private final LemmasIndexingServiceImpl lemmasIndexingService;
    public SearchResult getSearch(String query, String stringUrl, int offset, int limit){

        SearchResult response=new SearchResult();
        if (query.isEmpty()){
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
        }else {
            // лист лемм по запросу
            int countPage=0;
            List<SearchResultQuery> searchResultQueryList=new ArrayList<>();


            List<String> uniqueListLemmas=lemmasIndexingService.getListLemmas(query);
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
            log.info(String.valueOf(hashMap));
            //оптимальный не сортированный лист лемм с удалением наибольшего количества страниц
            List<String> optimalListLemmas=new ArrayList<>();
            List<LemmaModel> optimalListLemmaModel=new ArrayList<>();
            for (Entry<String,List<IndexModel>> entry:hashMap.entrySet()){
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
            //создание списка страниц соответствующих леммам из query
            for (int i=1;i<optimalSortedListLemmaModel.size();i++){
                Integer idLemma1=optimalSortedListLemmaModel.get(i).getId();
                List<PageModel> listPageModelId1 =indexRepository.findAllByLemmaId(idLemma1)
                        .stream()
                        .map(IndexModel::getPage)
                        .collect(Collectors.toList());
                listPageModelId.retainAll(listPageModelId1);
            }

            // выдаю пустой список
            if (listPageModelId.isEmpty()){
                /*SearchResultQuery resultQuery=new SearchResultQuery();
                List<SearchResultQuery> searchResultQueryList=new ArrayList<>(0);
                response.setData(searchResultQueryList);
                response.setCount(0);*/

            }else {
                //расчет абсолютной релевантности по страницам
                Map<Integer,Double> mapAbsoluteRelevancePages=new HashMap<>();
                for (PageModel pageModel:listPageModelId){
                    double absoluteRelevancePages=0.0;
                    Integer idPage=pageModel.getId();
                    for (LemmaModel lemmaModel:optimalSortedListLemmaModel){
                        Integer idLemma1=lemmaModel.getId();
                        IndexModel indexModelPages=indexRepository.findByPageIdAndLemmaId(idPage,idLemma1).get();
                        absoluteRelevancePages+=indexModelPages.getRank();
                    }
                    mapAbsoluteRelevancePages.put(idPage,absoluteRelevancePages);
                }
                //расчет максимальной абсолютной релевантности
                double relevanceMax=mapAbsoluteRelevancePages.values()
                        .stream()
                        .max(Double::compareTo).get();
                //расчет относительной релевантности по страницам
                for (Entry<Integer,Double> entry:mapAbsoluteRelevancePages.entrySet()){
                    double value=entry.getValue()/relevanceMax;
                    double scale = Math.pow(10, 1);
                    double rel = Math.ceil(value * scale) / scale;
                    mapAbsoluteRelevancePages.put(entry.getKey(),rel);
                }
                //сортировка HashMap по значениям Relevance
                HashMap<Integer,Double> sortedMap = mapAbsoluteRelevancePages.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));


                //List<SearchResultQuery> searchResultQueryList=new ArrayList<>();
                for (Map.Entry<Integer,Double> entry:sortedMap.entrySet()){
                    SearchResultQuery resultQuery=new SearchResultQuery();
                    PageModel page=pageRepository.findById(entry.getKey()).get();
                    String patchPage=page.getPath();
                    String content=page.getContent();
                    Document document = Jsoup.parse(content);
                    String str1="";

                    String cleanContent = Jsoup.parse(content).text();
                    StringBuilder stringBuilder=new StringBuilder();

                    resultQuery.setSite(urlSite);
                    resultQuery.setSiteName(nameSite);
                    resultQuery.setUri(patchPage);
                    resultQuery.setTitle(document.title());
                    resultQuery.setRelevance(entry.getValue());
                    for (LemmaModel model:optimalSortedListLemmaModel){


                        int keywordIndex = cleanContent.indexOf(model.getLemma());
                        int snippetStart = Math.max(0, keywordIndex - 20);
                        int snippetEnd = Math.min(cleanContent.length(), keywordIndex
                                + model.getLemma().length() + 20);
                        str1 = cleanContent.substring(snippetStart, snippetEnd);
                        //?????
                        //str1 = highlightKeywords(str1, optimalSortedListLemmaModel);
                        //StringBuilder stringBuilder=new StringBuilder();
                        String[] words = str1.split("\\s+");
                        boolean control = false;

                        for (String word : words) {
                            List<String> lemmas = lemmasIndexingService.getListLemmas(word).stream().toList();
                            String lemma = lemmas.isEmpty() ? word : lemmas.get(0);
                            boolean highlightWord = optimalSortedListLemmaModel.contains(lemma);

                            if (highlightWord) {
                                if (!control) {
                                    stringBuilder.append("<b>");
                                }
                                stringBuilder.append(word).append(" ");
                                control = true;
                            } else {
                                if (control) {
                                    stringBuilder.append("</b>");
                                }
                                stringBuilder.append(word).append(" ");
                                control = false;
                            }
                        }
                        if (control) {
                            stringBuilder.append("</b>");
                        }
                        resultQuery.setSnippet(String.valueOf(stringBuilder));

                    }
                    searchResultQueryList.add(resultQuery);
                }

                // расчет количества страниц с полным совпадением с вопросом
                countPage=mapAbsoluteRelevancePages.size();

            }



            response.setResult(true);
            response.setCount(countPage);
            response.setData(searchResultQueryList);
        }
        return response;
    }
}
