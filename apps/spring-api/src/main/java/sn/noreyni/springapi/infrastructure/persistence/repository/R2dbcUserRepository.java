package sn.noreyni.springapi.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import sn.noreyni.springapi.infrastructure.persistence.entity.UserEntity;
import reactor.core.publisher.Mono;

public interface R2dbcUserRepository extends ReactiveCrudRepository<UserEntity, Long> {
    Mono<UserEntity> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
    Flux<UserEntity> findAllBy(Pageable pageable);
}
