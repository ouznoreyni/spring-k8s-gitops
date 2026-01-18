package sn.noreyni.springapi.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import sn.noreyni.springapi.domain.model.ArticleStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("articles")
public class ArticleEntity {
    @Id
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private ArticleStatus status;
    private Long authorId;
    private Integer views;
    private Integer likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
