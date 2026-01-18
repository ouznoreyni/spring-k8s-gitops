package sn.noreyni.springapi.application.usecase.article.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.noreyni.springapi.application.dto.ArticleSummaryDto;
import sn.noreyni.springapi.application.mapper.ArticleMapper;
import sn.noreyni.springapi.application.mapper.UserMapper;
import sn.noreyni.springapi.domain.repository.ArticleRepository;
import sn.noreyni.springapi.domain.repository.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetArticleListQuery {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public Mono<Page<ArticleSummaryDto>> execute(Pageable pageable) {
        return articleRepository.findAll(pageable)
                .map(articleMapper::toSummaryDto)
                .collectList()
                .zipWith(articleRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }
}
