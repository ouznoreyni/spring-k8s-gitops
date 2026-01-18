package sn.noreyni.springapi.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sn.noreyni.springapi.domain.model.Comment;
import sn.noreyni.springapi.domain.repository.CommentRepository;
import sn.noreyni.springapi.infrastructure.persistence.entity.CommentEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {
    private final R2dbcCommentRepository r2dbcCommentRepository;

    @Override
    public Mono<Comment> findById(Long id) {
        return r2dbcCommentRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Flux<Comment> findByArticleId(Long articleId, org.springframework.data.domain.Pageable pageable) {
        return r2dbcCommentRepository.findAllByArticleId(articleId, pageable).map(this::toDomain);
    }

    @Override
    public Mono<Long> countByArticleId(Long articleId) {
        return r2dbcCommentRepository.countByArticleId(articleId);
    }

    @Override
    public Mono<Comment> save(Comment comment) {
        return r2dbcCommentRepository.save(toEntity(comment)).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return r2dbcCommentRepository.deleteById(id);
    }

    private Comment toDomain(CommentEntity entity) {
        return Comment.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .authorId(entity.getAuthorId())
                .articleId(entity.getArticleId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private CommentEntity toEntity(Comment domain) {
        return CommentEntity.builder()
                .id(domain.getId())
                .content(domain.getContent())
                .authorId(domain.getAuthorId())
                .articleId(domain.getArticleId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
