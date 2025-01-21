package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexModel;
import searchengine.model.PageModel;

import java.util.List;


public interface IndexRepository extends JpaRepository<IndexModel, Integer> {
    List<IndexModel> findAllByPageId(Integer i);

    void deleteAllIndexModelByPageId(Integer i);

    /*@Modifying
    @Query("DELETE FROM search_index WHERE page_id=:i ")
    void deleteAllByPageId(@Param("i") Integer i);
    @Query("SELECT i FROM IndexModel i JOIN FETCH i.page p JOIN FETCH i.lemma l WHERE p = :page")
    List<IndexModel> findByPage(@Param("page") PageModel page);*/
}
