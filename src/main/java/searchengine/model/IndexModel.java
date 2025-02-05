package searchengine.model;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "search_index")
public class IndexModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OnDelete(action = OnDeleteAction.CASCADE)
    //@ManyToOne(cascade = CascadeType.ALL)
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn (name ="page_id",nullable = false,foreignKey = @ForeignKey(name ="page_foreign_key"))
    private PageModel page;
    //@ManyToOne (cascade = CascadeType.ALL)
    @ManyToOne (cascade = CascadeType.MERGE)
    @JoinColumn (name ="lemma_id",nullable = false,foreignKey = @ForeignKey(name ="lemma_foreign_key"))
    private LemmaModel lemma;
    @Column(name ="`rank`",nullable = false)
    private Float rank;
}
