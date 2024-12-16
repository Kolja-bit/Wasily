package searchengine.repositories;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Sites;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page,Integer> {
    Optional<Page> findByPath(String s);
    Optional<Page> findByPathAndSite(String s,Sites s1);
    boolean existsByPathAndSite(String s,Sites s1);
    //void deleteById(Integer i);

    int countBySite(Sites site);
}
