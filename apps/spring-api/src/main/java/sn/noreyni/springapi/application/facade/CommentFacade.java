package sn.noreyni.springapi.application.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import sn.noreyni.springapi.application.dto.CommentDto;
import sn.noreyni.springapi.application.usecase.comment.command.AddCommentCommand;
import sn.noreyni.springapi.application.usecase.comment.command.DeleteCommentCommand;
import sn.noreyni.springapi.application.usecase.comment.query.GetCommentsByArticleQuery;
import sn.noreyni.springapi.domain.model.Comment;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CommentFacade {
    private final AddCommentCommand addCommentCommand;
    private final DeleteCommentCommand deleteCommentCommand;
    private final GetCommentsByArticleQuery getCommentsByArticleQuery;

    public Mono<CommentDto> addComment(Comment comment, String authorEmail) {
        return addCommentCommand.execute(comment, authorEmail);
    }

    public Mono<Page<CommentDto>> getCommentsByArticle(Long articleId, Pageable pageable) {
        return getCommentsByArticleQuery.execute(articleId, pageable);
    }

    public Mono<Void> deleteComment(Long id, String userEmail) {
        return deleteCommentCommand.execute(id, userEmail);
    }
}
