package sn.noreyni.springapi.application.usecase.article.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.noreyni.springapi.application.dto.ArticleDto;
import sn.noreyni.springapi.application.mapper.ArticleMapper;
import sn.noreyni.springapi.domain.model.Article;
import sn.noreyni.springapi.domain.repository.ArticleRepository;
import sn.noreyni.springapi.domain.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateArticleCommand {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;

    public Mono<ArticleDto> execute(Article article, String authorEmail) {
        return userRepository.findByEmail(authorEmail)
                .flatMap(user -> {
                    article.setAuthorId(user.getId());
                    article.setCreatedAt(LocalDateTime.now());
                    article.setUpdatedAt(LocalDateTime.now());
                    return articleRepository.save(article);
                })
                .map(articleMapper::toDto);
    }
}
