package sn.noreyni.springapi.application.usecase.comment.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.noreyni.springapi.application.dto.CommentDto;
import sn.noreyni.springapi.application.mapper.CommentMapper;
import sn.noreyni.springapi.domain.repository.CommentRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetCommentsByArticleQuery {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public Mono<Page<CommentDto>> execute(Long articleId, Pageable pageable) {
        return commentRepository.findByArticleId(articleId, pageable)
                .map(commentMapper::toDto)
                .collectList()
                .zipWith(commentRepository.countByArticleId(articleId))
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }
}
