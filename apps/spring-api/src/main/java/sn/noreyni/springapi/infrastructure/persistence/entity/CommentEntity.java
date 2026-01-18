package sn.noreyni.springapi.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("comments")
public class CommentEntity {
    @Id
    private Long id;
    private String content;
    private Long authorId;
    private Long articleId;
    private LocalDateTime createdAt;
}
