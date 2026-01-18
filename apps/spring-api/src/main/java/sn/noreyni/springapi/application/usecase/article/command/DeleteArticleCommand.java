package sn.noreyni.springapi.application.usecase.article.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.noreyni.springapi.domain.repository.ArticleRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeleteArticleCommand {
    private final ArticleRepository articleRepository;

    public Mono<Void> execute(Long id) {
        return articleRepository.deleteById(id);
    }
}
