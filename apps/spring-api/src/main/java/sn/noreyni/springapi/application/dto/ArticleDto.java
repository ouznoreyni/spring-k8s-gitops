package sn.noreyni.springapi.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import sn.noreyni.springapi.domain.model.ArticleStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private ArticleStatus status;
    private Long authorId;
    private UserDto author;
    private List<CommentDto> comments;
    private Integer views;
    private Integer likes;
    private List<TagDto> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
