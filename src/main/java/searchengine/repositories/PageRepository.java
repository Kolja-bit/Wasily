package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageModel;
import searchengine.model.SitesModel;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageModel,Integer> {

    int countPageBySite(SitesModel s);
    Optional<PageModel> findByPathAndSite(String s, SitesModel s1);
    boolean existsByPathAndSite(String s, SitesModel s1);
    boolean existsByContentAndSite(String s, SitesModel s1);


    int countBySite(SitesModel site);
}
