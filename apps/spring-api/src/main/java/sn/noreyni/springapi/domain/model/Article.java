package sn.noreyni.springapi.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private ArticleStatus status;
    private Long authorId;
    private Integer views;
    private Integer likes;
    private List<Tag> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
