package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteStatusModel;
import searchengine.model.SitesModel;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SitesModel, Integer> {
    Optional<SitesModel> findByUrl(String s);
    boolean existsByStatus(SiteStatusModel s);
    boolean existsByUrl(String s);
}
