package sn.noreyni.springapi.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sn.noreyni.springapi.infrastructure.persistence.entity.ArticleEntity;

public interface R2dbcArticleRepository extends ReactiveCrudRepository<ArticleEntity, Long> {
    Flux<ArticleEntity> findAllBy(Pageable pageable);
    Flux<ArticleEntity> findAllByAuthorId(Long authorId, Pageable pageable);
    Mono<Long> countByAuthorId(Long authorId);
}
