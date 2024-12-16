package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "page",indexes = @Index(name = "path_ind",columnList = "path"))
public class Page  implements Comparable<Page>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "site_id",nullable = false,foreignKey = @ForeignKey(name = "sites_foreign_key"))
    private Sites site;
    @Column(nullable = false)
    private Integer code;
    @Column(columnDefinition = "MEDIUMTEXT",nullable = false)
    private String content;
    @Column(columnDefinition = "VARCHAR(250)", nullable = false)
    private String path;


    @Override
    public int compareTo(Page o) {
        return getPath().compareTo(o.getPath());
    }
}
