package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Sites;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LemmasIndexingService implements LemmasIndexingServiceImpl {
    private final String url;
    private final PageRepository pageRepository;
    private final LemmasRepository lemmasRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private final static Set<String> SERVICE_PARTS_OF_SPEECH=Set.of("ПРЕДЛ","СОЮЗ","МЕЖД","МС-П","МС");
    @Override
    public String getHtml(){
        String[] p= url.split("/");
        String url1=p[0]+"//"+p[2]+"/";
        Sites sites=siteRepository.findByUrl(url1).orElseThrow();
        String path = url.substring(url1.length() - 1);
        String html=pageRepository.findByPathAndSite(path,sites).get().getContent();
        return html;
    }

    public HashMap<String,Integer> getMapLemmas(){
        String[]strings=getCleaningWebPageFromTags()
                .replaceAll("[^A-Za-zА-Яа-я]+"," ").trim().split("\\s");
        HashMap<String,Integer> mapLemma=new HashMap<>();
        LuceneMorphology luceneMorph = null;
        for (String string:strings){
            String str=string.toLowerCase();
            try {
                luceneMorph = new RussianLuceneMorphology();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<String> list = luceneMorph.getMorphInfo(str);
            if (!SERVICE_PARTS_OF_SPEECH.contains(list.get(0).split("\\s")[1])){
                if (mapLemma.containsKey(luceneMorph.getNormalForms(str).get(0))){
                    mapLemma.put(luceneMorph.getNormalForms(str).get(0),
                            mapLemma.get(luceneMorph.getNormalForms(str).get(0))+1);
                }else {
                    mapLemma.put(luceneMorph.getNormalForms(str).get(0), 1);
                }
            }
        }
        return mapLemma;
    }
    public String getCleaningWebPageFromTags(){
        String str= Jsoup.parse(getHtml()).text();
        return str;
    }
    public void recordLemmas(){
        HashMap<String,Integer>hashMap=getMapLemmas();
        Sites sites=siteRepository.findByUrl(url).orElseThrow();
        for (Map.Entry<String,Integer>entry: hashMap.entrySet()){
        Lemma lemma=new Lemma();
        lemma.setSite(sites);
        lemma.setLemma(entry.getKey());
        lemma.setFrequency(entry.getValue());
        lemmasRepository.save(lemma);
        }
    }


}
