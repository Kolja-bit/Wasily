package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class LemmasIndexingServiceImpl {
    private final String stringHtml;
    //private final SitesModel sites;
    //private final PageModel page;
    //private final LemmasRepository lemmasRepository;
    //private final IndexRepository indexRepository;
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
    public List<String> getListLemmas(){
        List<String> listLemmas=new ArrayList<>();
        for (String lemma:getMapLemmas().keySet()){
            listLemmas.add(lemma);
        }
        return  listLemmas;
    }
    /*public String getWebPageContent(){
        String str= Jsoup.parse(stringHtml).text();
        return str;
    }*/
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
    /*public void recordLemmas(SitesModel sites, PageModel page){

            HashMap<String, Integer> hashMap = getMapLemmas();
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                if (!lemmasRepository.existsBySiteAndLemma(sites,entry.getKey())){

                    LemmaModel lemma = new LemmaModel();
                    lemma.setSite(sites);
                    lemma.setLemma(entry.getKey());
                    lemma.setFrequency((entry.getValue()));
                    lemmasRepository.save(lemma);

                    IndexModel index = new IndexModel();
                    index.setLemma(lemma);
                    index.setPage(page);
                    index.setRank(Float.valueOf((entry.getValue())));
                    indexRepository.save(index);
                }else {
                    LemmaModel lemmaModel1=lemmasRepository.findBySiteAndLemma(sites,entry.getKey()).get();
                    int repetitionOfLemmasOnSite=lemmaModel1.getFrequency()+entry.getValue();
                    lemmaModel1.setFrequency(repetitionOfLemmasOnSite);
                    lemmasRepository.save(lemmaModel1);

                    IndexModel index=new IndexModel();
                    index.setLemma(lemmaModel1);
                    index.setPage(page);
                    index.setRank(Float.valueOf((entry.getValue())));
                    indexRepository.save(index);
                }
            }
    }*/
    public List<String> getListContext(){
        List<String> stringList=new ArrayList<>();
        String[]strings=stringHtml.replaceAll("[^A-Za-zА-Яа-яЁё]+"," ")
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
