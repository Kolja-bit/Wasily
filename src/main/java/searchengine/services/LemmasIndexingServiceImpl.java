package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SitesModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmasRepository;

import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class LemmasIndexingServiceImpl implements LemmasIndexingService {
    private final String stringHtml;
    private final SitesModel sites;
    private final PageModel page;
    private final LemmasRepository lemmasRepository;
    private final IndexRepository indexRepository;
    private LuceneMorphology luceneMorph =null;


    private final static Set<String> RUSSIAN_REGEX=Set.of("ПРЕДЛ","СОЮЗ","МЕЖД","МС-П","МС");
    private final static Set<String> ENGLICH_REGEX=Set.of("ADJECTIVE","ARTICLE","PN_ADJ","PREP");

    public HashMap<String,Integer> getMapLemmas(){
        HashMap<String,Integer> mapLemma=new HashMap<>();
        List<String> list1=getListContext();
        for (String str:list1){
            getLuceneMorph(str);
            List<String> list = luceneMorph.getMorphInfo(str);
            if (!RUSSIAN_REGEX.contains(list.get(0).split("\\s")[1])){
                if (!ENGLICH_REGEX.contains(list.get(0).split("\\s")[1])) {
                    if (mapLemma.containsKey(luceneMorph.getNormalForms(str).get(0))) {
                        mapLemma.put(luceneMorph.getNormalForms(str).get(0),
                                mapLemma.get(luceneMorph.getNormalForms(str).get(0)) + 1);
                    } else {
                        mapLemma.put(luceneMorph.getNormalForms(str).get(0), 1);
                    }
                }
            }
        }
        return mapLemma;
    }
    public String getWebPageContent(){
        String str= Jsoup.parse(stringHtml).text();
        return str;
    }
    public LuceneMorphology getLuceneMorph(String string){
        try {
            if(string.matches("\\w+")){
                luceneMorph = new EnglishLuceneMorphology();
            }else {
                luceneMorph = new RussianLuceneMorphology();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return luceneMorph;
    }
    public void recordLemmas(){
            HashMap<String, Integer> hashMap = getMapLemmas();
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                LemmaModel lemma = new LemmaModel();

                lemma.setSite(sites);
                lemma.setLemma(entry.getKey());
                //int frequencyLemma=;
                lemma.setFrequency(2);//то число просто так
                IndexModel index = new IndexModel();
                index.setLemma(lemma);
                index.setPage(page);
                index.setRank(Float.valueOf((entry.getValue())));
                indexRepository.save(index);
                lemmasRepository.save(lemma);


        /*IndexModel index=new IndexModel();
        index.setLemma(lemma);
        index.setPage(page);
        index.setRank(Float.valueOf((entry.getValue())));
        indexRepository.save(index);*/
            }

        //log.info(String.valueOf(setLemmaId));
    }
    public List<String> getListContext(){
        List<String> stringList=new ArrayList<>();
        String[]strings=getWebPageContent().replaceAll("[^A-Za-zА-Яа-яЁё]+"," ")
                .trim().split("\\s");
        for (String string:strings) {
            if (string.matches("[А-Яа-яЁё&&[^A-Za-z]]+") || string.matches("[A-Za-z&&[^А-Яа-яЁё]]+")) {
                String str1 = string.toLowerCase();
                stringList.add(str1);
            }
        }
        return stringList;
    }

}
