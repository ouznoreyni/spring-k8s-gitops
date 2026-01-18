package sn.noreyni.springapi.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import sn.noreyni.springapi.infrastructure.persistence.entity.CommentEntity;
import reactor.core.publisher.Flux;

public interface R2dbcCommentRepository extends ReactiveCrudRepository<CommentEntity, Long> {
    Flux<CommentEntity> findAllByArticleId(Long articleId, Pageable pageable);
    Mono<Long> countByArticleId(Long articleId);
}
