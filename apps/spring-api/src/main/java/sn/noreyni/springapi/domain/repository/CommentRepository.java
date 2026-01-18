package sn.noreyni.springapi.domain.repository;

import org.springframework.data.domain.Pageable;
import sn.noreyni.springapi.domain.model.Comment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentRepository {
    Mono<Comment> findById(Long id);
    Flux<Comment> findByArticleId(Long articleId, Pageable pageable);
    Mono<Long> countByArticleId(Long articleId);
    Mono<Comment> save(Comment comment);
    Mono<Void> deleteById(Long id);
}
