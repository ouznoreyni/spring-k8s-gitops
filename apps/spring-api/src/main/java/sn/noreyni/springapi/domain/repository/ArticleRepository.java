package sn.noreyni.springapi.domain.repository;

import org.springframework.data.domain.Pageable;
import sn.noreyni.springapi.domain.model.Article;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ArticleRepository {
    Mono<Article> findById(Long id);
    Flux<Article> findAll(Pageable pageable);
    Flux<Article> findAllByAuthorId(Long authorId, Pageable pageable);
    Mono<Long> count();
    Mono<Long> countByAuthorId(Long authorId);
    Mono<Article> save(Article article);
    Mono<Void> deleteById(Long id);
}
