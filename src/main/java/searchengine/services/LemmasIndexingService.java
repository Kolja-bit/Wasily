package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Sites;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;


import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@Slf4j
//@Service
@RequiredArgsConstructor
public class LemmasIndexingService implements LemmasIndexingServiceImpl {
    private final String stringHtml;
    private final Sites sites;

    private LuceneMorphology luceneMorph =null;

    private final LemmasRepository lemmasRepository;
    private final static Set<String> RUSSIAN_REGEX=Set.of("ПРЕДЛ","СОЮЗ","МЕЖД","МС-П","МС");
    private final static Set<String> ENGLICH_REGEX=Set.of("ADJECTIVE","ARTICLE","PN_ADJ","PREP");

    public HashMap<String,Integer> getMapLemmas(){
        String[]strings=getWebPageContent().replaceAll("[^A-Za-zА-Яа-я]+"," ")
                .trim().split("\\s");
        HashMap<String,Integer> mapLemma=new HashMap<>();
        for (String string:strings){
            String str=string.toLowerCase();
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
        HashMap<String,Integer>hashMap=getMapLemmas();
        for (Map.Entry<String,Integer>entry: hashMap.entrySet()){
        Lemma lemma=new Lemma();
        lemma.setSite(sites);
        lemma.setLemma(entry.getKey());
        lemma.setFrequency(entry.getValue());
        lemmasRepository.save(lemma);
        }
    }

}
