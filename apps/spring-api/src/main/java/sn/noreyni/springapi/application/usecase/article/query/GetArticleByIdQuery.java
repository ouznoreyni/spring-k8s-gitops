package sn.noreyni.springapi.application.usecase.article.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.noreyni.springapi.application.dto.ArticleDto;
import sn.noreyni.springapi.application.mapper.ArticleMapper;
import sn.noreyni.springapi.application.mapper.CommentMapper;
import sn.noreyni.springapi.application.mapper.UserMapper;
import sn.noreyni.springapi.domain.repository.ArticleRepository;
import sn.noreyni.springapi.domain.repository.CommentRepository;
import sn.noreyni.springapi.domain.repository.UserRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetArticleByIdQuery {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    public Mono<ArticleDto> execute(Long id) {
        return articleRepository.findById(id)
                .flatMap(article -> {
                    // Increment views
                    article.setViews(article.getViews() != null ? article.getViews() + 1 : 1);
                    return articleRepository.save(article);
                })
                .flatMap(article -> {
                    ArticleDto dto = articleMapper.toDto(article);
                    
                    Mono<ArticleDto> withAuthor = userRepository.findById(article.getAuthorId())
                            .map(userMapper::toDto)
                            .map(authorDto -> {
                                dto.setAuthor(authorDto);
                                return dto;
                            })
                            .defaultIfEmpty(dto);

                    return withAuthor.flatMap(articleDto -> 
                        commentRepository.findByArticleId(article.getId(), Pageable.unpaged())
                                .map(commentMapper::toDto)
                                .collectList()
                                .map(comments -> {
                                    articleDto.setComments(comments);
                                    return articleDto;
                                })
                    );
                });
    }
}
