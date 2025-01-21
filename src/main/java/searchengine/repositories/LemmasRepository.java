package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaModel;
import searchengine.model.SitesModel;

import java.util.List;

@Repository
public interface LemmasRepository extends JpaRepository<LemmaModel, Integer> {

}
