package sn.noreyni.springapi.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import sn.noreyni.springapi.domain.model.Article;
import sn.noreyni.springapi.domain.model.ArticleStatus;
import sn.noreyni.springapi.domain.model.User;
import sn.noreyni.springapi.domain.repository.ArticleRepository;
import sn.noreyni.springapi.domain.repository.UserRepository;
import sn.noreyni.springapi.infrastructure.persistence.repository.R2dbcTagRepository;
import sn.noreyni.springapi.infrastructure.persistence.entity.TagEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final R2dbcTagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        userRepository.count()
                .flatMap(count -> {
                    if (count == 0) {
                        log.info("Initializing data...");
                        return seedData();
                    }
                    log.info("Database already contains data, skipping initialization.");
                    return Mono.empty();
                })
                .subscribe();
    }

    private Mono<Void> seedData() {
        List<String> tagNames = List.of("Java", "Spring Boot", "R2DBC", "PostgreSQL", "React", "TypeScript", "Docker", "Kubernetes");
        
        return Flux.fromIterable(tagNames)
                .map(name -> TagEntity.builder().name(name).build())
                .flatMap(tagRepository::save)
                .collectList()
                .flatMap(tags -> {
                    List<User> admins = new ArrayList<>();
                    for (int i = 1; i <= 5; i++) {
                        admins.add(createUser("admin" + i, "ROLE_ADMIN"));
                    }

                    List<User> users = new ArrayList<>();
                    for (int i = 1; i <= 10; i++) {
                        users.add(createUser("user" + i, "ROLE_USER"));
                    }

                    List<User> allUsers = new ArrayList<>();
                    allUsers.addAll(admins);
                    allUsers.addAll(users);

                    return Flux.fromIterable(allUsers)
                            .flatMap(userRepository::save)
                            .collectList()
                            .flatMap(savedUsers -> {
                                List<Article> articles = new ArrayList<>();
                                for (int i = 1; i <= 30; i++) {
                                    User randomAuthor = savedUsers.get(random.nextInt(savedUsers.size()));
                                    Article article = createArticle(i, randomAuthor.getId());
                                    
                                    // Assign 1-3 random tags
                                    List<sn.noreyni.springapi.domain.model.Tag> articleTags = new ArrayList<>();
                                    int numTags = 1 + random.nextInt(3);
                                    for (int j = 0; j < numTags; j++) {
                                        TagEntity randomTagEntity = tags.get(random.nextInt(tags.size()));
                                        articleTags.add(sn.noreyni.springapi.domain.model.Tag.builder()
                                                .id(randomTagEntity.getId())
                                                .name(randomTagEntity.getName())
                                                .build());
                                    }
                                    article.setTags(articleTags);
                                    articles.add(article);
                                }
                                return Flux.fromIterable(articles)
                                        .flatMap(articleRepository::save)
                                        .then();
                            });
                })
                .doOnSuccess(v -> log.info("Data initialization completed successfully."))
                .doOnError(e -> log.error("Error during data initialization", e));
    }

    private User createUser(String name, String role) {
        return User.builder()
                .username(name)
                .firstName(name.substring(0, 1).toUpperCase() + name.substring(1))
                .lastName("LastName")
                .email(name + "@example.com")
                .password(passwordEncoder.encode("password"))
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Article createArticle(int index, Long authorId) {
        return Article.builder()
                .title("Article Title " + index)
                .content("This is the content for article " + index + ". It contains some sample text for demonstration purposes.")
                .imageUrl("https://picsum.photos/seed/" + UUID.randomUUID() + "/800/600")
                .status(getRandomStatus())
                .authorId(authorId)
                .views(random.nextInt(1000))
                .likes(random.nextInt(100))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ArticleStatus getRandomStatus() {
        ArticleStatus[] statuses = ArticleStatus.values();
        return statuses[random.nextInt(statuses.length)];
    }
}
