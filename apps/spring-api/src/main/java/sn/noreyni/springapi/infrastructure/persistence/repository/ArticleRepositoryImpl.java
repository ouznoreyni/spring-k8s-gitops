package sn.noreyni.springapi.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import sn.noreyni.springapi.domain.model.Article;
import sn.noreyni.springapi.domain.repository.ArticleRepository;
import sn.noreyni.springapi.infrastructure.persistence.entity.ArticleEntity;
import sn.noreyni.springapi.infrastructure.persistence.entity.TagEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepository {
    private final R2dbcArticleRepository r2dbcArticleRepository;
    private final R2dbcTagRepository r2dbcTagRepository;
    private final org.springframework.r2dbc.core.DatabaseClient databaseClient;

    @Override
    public Mono<Article> findById(Long id) {
        return r2dbcArticleRepository.findById(id)
                .flatMap(this::loadTags)
                .map(this::toDomain);
    }

    @Override
    public Flux<Article> findAll(Pageable pageable) {
        return r2dbcArticleRepository.findAllBy(pageable)
                .flatMap(this::loadTags)
                .map(this::toDomain);
    }

    @Override
    public Flux<Article> findAllByAuthorId(Long authorId, Pageable pageable) {
        return r2dbcArticleRepository.findAllByAuthorId(authorId, pageable)
                .flatMap(this::loadTags)
                .map(this::toDomain);
    }

    private Mono<ArticleEntityWithTags> loadTags(ArticleEntity entity) {
        return databaseClient.sql("SELECT t.* FROM tags t INNER JOIN article_tags at ON t.id = at.tag_id WHERE at.article_id = :articleId")
                .bind("articleId", entity.getId())
                .map((row, rowMetadata) -> TagEntity.builder()
                        .id(row.get("id", Long.class))
                        .name(row.get("name", String.class))
                        .build())
                .all()
                .collectList()
                .map(tags -> new ArticleEntityWithTags(entity, tags));
    }

    @Override
    public Mono<Long> count() {
        return r2dbcArticleRepository.count();
    }

    @Override
    public Mono<Long> countByAuthorId(Long authorId) {
        return r2dbcArticleRepository.countByAuthorId(authorId);
    }

    @Override
    public Mono<Article> save(Article article) {
        return r2dbcArticleRepository.save(toEntity(article))
                .flatMap(savedEntity -> {
                    if (article.getTags() == null || article.getTags().isEmpty()) {
                        // If we are updating an existing article, we might want to keep existing tags if they are not provided, 
                        // but usually save() means replace. For now, let's assume we handle tags if provided.
                        return loadTags(savedEntity);
                    }
                    return databaseClient.sql("DELETE FROM article_tags WHERE article_id = :aid")
                            .bind("aid", savedEntity.getId())
                            .then()
                            .thenMany(Flux.fromIterable(article.getTags()))
                            .flatMap(tag -> databaseClient.sql("INSERT INTO article_tags (article_id, tag_id) VALUES (:aid, :tid) ON CONFLICT DO NOTHING")
                                    .bind("aid", savedEntity.getId())
                                    .bind("tid", tag.getId())
                                    .fetch().rowsUpdated())
                            .then(loadTags(savedEntity));
                })
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return r2dbcArticleRepository.deleteById(id);
    }

    private Article toDomain(ArticleEntityWithTags entityWithTags) {
        ArticleEntity entity = entityWithTags.entity;
        return Article.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .imageUrl(entity.getImageUrl())
                .status(entity.getStatus())
                .authorId(entity.getAuthorId())
                .views(entity.getViews())
                .likes(entity.getLikes())
                .tags(entityWithTags.tags.stream()
                        .map(t -> sn.noreyni.springapi.domain.model.Tag.builder()
                                .id(t.getId())
                                .name(t.getName())
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ArticleEntity toEntity(Article domain) {
        return ArticleEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .content(domain.getContent())
                .imageUrl(domain.getImageUrl())
                .status(domain.getStatus())
                .authorId(domain.getAuthorId())
                .views(domain.getViews() != null ? domain.getViews() : 0)
                .likes(domain.getLikes() != null ? domain.getLikes() : 0)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private static class ArticleEntityWithTags {
        final ArticleEntity entity;
        final java.util.List<TagEntity> tags;

        ArticleEntityWithTags(ArticleEntity entity, java.util.List<TagEntity> tags) {
            this.entity = entity;
            this.tags = tags;
        }
    }
}
