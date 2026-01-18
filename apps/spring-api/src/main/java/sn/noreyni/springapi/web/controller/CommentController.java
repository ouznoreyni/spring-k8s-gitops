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
import sn.noreyni.springapi.application.dto.CommentDto;
import sn.noreyni.springapi.application.facade.CommentFacade;
import sn.noreyni.springapi.domain.model.Comment;
import sn.noreyni.springapi.web.request.CommentRequest;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management APIs")
@Slf4j
public class CommentController {

    private final CommentFacade commentFacade;

    @PostMapping
    @Operation(summary = "Add a comment to an article")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CommentDto> addComment(@PathVariable Long articleId,
                                      @Valid @RequestBody CommentRequest request,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Adding comment to article: {}", articleId);
        Comment comment = Comment.builder()
                .content(request.getContent())
                .articleId(articleId)
                .build();
        return commentFacade.addComment(comment, userDetails.getUsername());
    }

    @GetMapping
    @Operation(summary = "Get paginated list of comments for an article")
    public Mono<Page<CommentDto>> getCommentsByArticle(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return commentFacade.getCommentsByArticle(articleId, PageRequest.of(page, size));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails) {
        return commentFacade.deleteComment(id, userDetails.getUsername());
    }
}
