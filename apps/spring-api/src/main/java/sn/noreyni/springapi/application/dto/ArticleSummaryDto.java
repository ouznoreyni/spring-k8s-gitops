package sn.noreyni.springapi.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import sn.noreyni.springapi.domain.model.ArticleStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSummaryDto {
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
