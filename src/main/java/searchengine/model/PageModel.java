package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "page",indexes = {
        @Index(name = "page_path_site_index", columnList = "path, site_id")})
    public class PageModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "site_id",nullable = false)
    private SitesModel site;
    @Column(nullable = false)
    private Integer code;
    @Column(columnDefinition = "MEDIUMTEXT",nullable = false)
    private String content;
    @Column(columnDefinition = "VARCHAR(250)", nullable = false)
    private String path;
}
