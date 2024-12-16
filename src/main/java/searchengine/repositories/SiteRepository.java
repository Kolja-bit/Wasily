package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteIndexingStatus;
import searchengine.model.Sites;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Sites, Integer> {
    Optional<Sites> findByUrl(String s);
    Optional<Sites> findByName(String s);
    Optional<Sites> findByStatus(SiteIndexingStatus s);
    void deleteByName(String s);
    boolean existsByStatus(SiteIndexingStatus s);
    boolean existsByName(String s);
    Optional<Sites> findByStatusAndName(SiteIndexingStatus s,String n);
}
