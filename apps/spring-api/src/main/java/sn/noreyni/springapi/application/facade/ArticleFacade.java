package sn.noreyni.springapi.application.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import sn.noreyni.springapi.application.dto.ArticleDto;
import sn.noreyni.springapi.application.dto.ArticleSummaryDto;
import sn.noreyni.springapi.application.usecase.article.command.CreateArticleCommand;
import sn.noreyni.springapi.application.usecase.article.command.DeleteArticleCommand;
import sn.noreyni.springapi.application.usecase.article.query.GetArticleByIdQuery;
import sn.noreyni.springapi.application.usecase.article.query.GetArticleListQuery;
import sn.noreyni.springapi.application.usecase.article.query.GetArticlesByAuthorQuery;
import sn.noreyni.springapi.domain.model.Article;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ArticleFacade {
    private final CreateArticleCommand createArticleCommand;
    private final DeleteArticleCommand deleteArticleCommand;
    private final GetArticleListQuery getArticleListQuery;
    private final GetArticleByIdQuery getArticleByIdQuery;
    private final GetArticlesByAuthorQuery getArticlesByAuthorQuery;

    public Mono<ArticleDto> createArticle(Article article, String authorEmail) {
        return createArticleCommand.execute(article, authorEmail);
    }

    public Mono<Page<ArticleSummaryDto>> getAllArticles(Pageable pageable) {
        return getArticleListQuery.execute(pageable);
    }

    public Mono<Page<ArticleSummaryDto>> getArticlesByAuthor(Long authorId, Pageable pageable) {
        return getArticlesByAuthorQuery.execute(authorId, pageable);
    }

    public Mono<ArticleDto> getArticleById(Long id) {
        return getArticleByIdQuery.execute(id);
    }

    public Mono<Void> deleteArticle(Long id) {
        return deleteArticleCommand.execute(id);
    }
}
