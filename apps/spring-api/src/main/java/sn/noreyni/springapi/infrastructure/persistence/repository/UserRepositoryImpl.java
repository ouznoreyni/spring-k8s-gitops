package sn.noreyni.springapi.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import sn.noreyni.springapi.domain.model.User;
import sn.noreyni.springapi.domain.repository.UserRepository;
import sn.noreyni.springapi.infrastructure.persistence.entity.UserEntity;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final R2dbcUserRepository r2dbcUserRepository;

    @Override
    public Mono<User> findById(Long id) {
        return r2dbcUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return r2dbcUserRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Mono<User> save(User user) {
        return r2dbcUserRepository.save(toEntity(user)).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return r2dbcUserRepository.existsByEmail(email);
    }

    @Override
    public Flux<User> findAll(Pageable pageable) {
        return r2dbcUserRepository.findAllBy(pageable).map(this::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return r2dbcUserRepository.count();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return r2dbcUserRepository.deleteById(id);
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .role(domain.getRole())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
