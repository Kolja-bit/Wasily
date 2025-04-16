package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class LemmasIndexingServiceImpl {
    private LuceneMorphology luceneMorph =null;


    private final static Set<String> RUSSIAN_REGEX=Set.of("ПРЕДЛ","СОЮЗ","МЕЖД","МС-П","МС");
    private final static Set<String> ENGLICH_REGEX=Set.of("ADJECTIVE","ARTICLE","PN_ADJ","PREP");

    public HashMap<String,Integer> getMapLemmas(String stringHtml){
        HashMap<String,Integer> mapLemma=new HashMap<>();
        List<String> list1=getListContext(stringHtml);
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
    public List<String> getListLemmas(String query){
        List<String> listLemmas=new ArrayList<>();
        for (String lemma:getMapLemmas(query).keySet()){
            listLemmas.add(lemma);
        }
        return  listLemmas;
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

    public List<String> getListContext(String stringHtml){
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
