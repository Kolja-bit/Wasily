package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Getter
@Setter
@Entity
//@Table(name = "page",indexes = @Index(name = "path_ind",columnList = "path"))
@Table(name = "page",indexes = {
        @Index(name = "page_path_site_index", columnList = "path, site_id")})
public class PageModel implements Comparable<PageModel>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = CascadeType.MERGE)
    //@ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id",nullable = false,foreignKey = @ForeignKey(name = "sites_foreign_key"))
    //@JoinColumn(name = "site_id",nullable = false)
    //посмотреть ключ его не должно быть должен быть только индекс
    private SitesModel site;
    @Column(nullable = false)
    private Integer code;
    @Column(columnDefinition = "MEDIUMTEXT",nullable = false)
    private String content;
    @Column(columnDefinition = "VARCHAR(250)", nullable = false)
    private String path;


    @Override
    public int compareTo(PageModel o) {
        return getPath().compareTo(o.getPath());
    }
}
