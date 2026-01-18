package sn.noreyni.springapi.application.usecase.comment.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.noreyni.springapi.application.dto.CommentDto;
import sn.noreyni.springapi.application.mapper.CommentMapper;
import sn.noreyni.springapi.domain.model.Comment;
import sn.noreyni.springapi.domain.repository.CommentRepository;
import sn.noreyni.springapi.domain.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AddCommentCommand {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public Mono<CommentDto> execute(Comment comment, String authorEmail) {
        return userRepository.findByEmail(authorEmail)
                .flatMap(user -> {
                    comment.setAuthorId(user.getId());
                    comment.setCreatedAt(LocalDateTime.now());
                    return commentRepository.save(comment);
                })
                .map(commentMapper::toDto);
    }
}
