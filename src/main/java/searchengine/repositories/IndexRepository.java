package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.IndexModel;

import java.util.List;
import java.util.Optional;


public interface IndexRepository extends JpaRepository<IndexModel, Integer> {
    List<IndexModel> findAllByPageId(Integer i);
    List<IndexModel> findAllByLemmaId(Integer i);
    Optional<IndexModel> findByPageIdAndLemmaId(Integer i,Integer i1);

    Optional<IndexModel> findByLemmaId(Integer integer);

    void deleteAllIndexModelByPageId(Integer i);

    /*@Modifying
    @Query("DELETE FROM search_index WHERE page_id=:i ")
    void deleteAllByPageId(@Param("i") Integer i);
    @Query("SELECT i FROM IndexModel i JOIN FETCH i.page p JOIN FETCH i.lemma l WHERE p = :page")
    List<IndexModel> findByPage(@Param("page") PageModel page);*/
}
