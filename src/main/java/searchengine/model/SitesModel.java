package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;



@Getter
@Setter
@Entity
@Table(name="site")
public class SitesModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')",nullable = false)
    private SiteStatusModel status;
    @Column(name = "status_time",nullable = false)
    private LocalDateTime statusTime;
    @Column(name = "last_error",columnDefinition = "TEXT")
    private String lastError;
    @Column(columnDefinition = "VARCHAR(255)",nullable = false)
    private String url;
    @Column(columnDefinition = "VARCHAR(255)",nullable = false)
    private String name;


}
