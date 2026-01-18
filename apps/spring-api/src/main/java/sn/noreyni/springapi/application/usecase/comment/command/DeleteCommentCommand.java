package sn.noreyni.springapi.application.usecase.comment.command;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sn.noreyni.springapi.domain.repository.CommentRepository;
import sn.noreyni.springapi.domain.repository.UserRepository;
import sn.noreyni.springapi.infrastructure.exception.BlogException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeleteCommentCommand {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public Mono<Void> execute(Long id, String userEmail) {
        return userRepository.findByEmail(userEmail)
                .switchIfEmpty(Mono.error(new BlogException(HttpStatus.UNAUTHORIZED, "User not found")))
                .flatMap(user -> commentRepository.findById(id)
                        .switchIfEmpty(Mono.error(new BlogException(HttpStatus.NOT_FOUND, "Comment not found")))
                        .flatMap(comment -> {
                            if (!comment.getAuthorId().equals(user.getId())) {
                                return Mono.error(new BlogException(HttpStatus.FORBIDDEN, "You can only delete your own comments"));
                            }
                            return commentRepository.deleteById(id);
                        }));
    }
}
