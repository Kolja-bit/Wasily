package searchengine.model;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "lemma")
public class LemmaModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn (name ="site_id",nullable = false,foreignKey = @ForeignKey(name = "site_foreign_key"))
    private SitesModel site;
    @Column(columnDefinition = "VARCHAR(255)",nullable = false)
    private String lemma;
    @Column(nullable = false)
    private Integer frequency;
}
