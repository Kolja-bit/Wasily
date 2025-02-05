package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaModel;
import searchengine.model.SitesModel;

import java.util.Optional;

@Repository
public interface LemmasRepository extends JpaRepository<LemmaModel, Integer> {
    boolean existsBySiteAndLemma(SitesModel s,String s1);
    Optional<LemmaModel> findBySiteAndLemma(SitesModel s, String s1);
}
