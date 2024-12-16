package searchengine.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "search_index")
public class SearchIndex implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn (name = "page_id",nullable = false,foreignKey = @ForeignKey(name = "page_foreign_key"))
    private Page page;
    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn (name = "lemma_id",nullable = false,foreignKey = @ForeignKey(name = "lemma_foreign_key"))
    private Lemma lemma;
    @Column(nullable = false)
    private float rank;
}
