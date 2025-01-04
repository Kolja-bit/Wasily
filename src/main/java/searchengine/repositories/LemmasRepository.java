package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Sites;

@Repository
public interface LemmasRepository extends JpaRepository<Lemma, Integer> {
}
