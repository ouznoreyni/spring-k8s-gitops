package sn.noreyni.springapi.domain.repository;

import reactor.core.publisher.Flux;
import sn.noreyni.springapi.domain.model.User;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    Mono<User> findById(Long id);
    Mono<User> findByEmail(String email);
    Mono<User> save(User user);
    Mono<Boolean> existsByEmail(String email);
    Flux<User> findAll(Pageable pageable);
    Mono<Long> count();
    Mono<Void> deleteById(Long id);
}
