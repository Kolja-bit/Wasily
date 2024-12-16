package searchengine.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "lemma")
public class Lemma implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn (name = "site_id",nullable = false,foreignKey = @ForeignKey(name = "site_foreign_key"))
    private Sites site;
    @Column(columnDefinition = "VARCHAR(255)",nullable = false)
    private String lemma;
    @Column(nullable = false)
    private Integer frequency;
}
