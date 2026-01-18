package sn.noreyni.springapi.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sn.noreyni.springapi.application.dto.ArticleDto;
import sn.noreyni.springapi.application.dto.ArticleSummaryDto;
import sn.noreyni.springapi.application.facade.ArticleFacade;
import sn.noreyni.springapi.domain.model.Article;
import sn.noreyni.springapi.domain.model.ArticleStatus;
import sn.noreyni.springapi.web.request.ArticleRequest;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "Articles", description = "Article management APIs")
@Slf4j
public class ArticleController {

    private final ArticleFacade articleFacade;

    @PostMapping
    @Operation(summary = "Create a new article")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ArticleDto> create(@Valid @RequestBody ArticleRequest request,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Creating article: {}", request.getTitle());
        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .status(request.getStatus() != null ? request.getStatus() : ArticleStatus.DRAFT)
                .build();
        return articleFacade.createArticle(article, userDetails.getUsername());
    }

    @GetMapping
    @Operation(summary = "Get paginated list of articles")
    public Mono<Page<ArticleSummaryDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return articleFacade.getAllArticles(PageRequest.of(page, size));
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "Get paginated list of articles by author")
    public Mono<Page<ArticleSummaryDto>> getByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return articleFacade.getArticlesByAuthor(authorId, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get article by ID")
    public Mono<ArticleDto> getById(@PathVariable Long id) {
        return articleFacade.getArticleById(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete article")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return articleFacade.deleteArticle(id);
    }
}
