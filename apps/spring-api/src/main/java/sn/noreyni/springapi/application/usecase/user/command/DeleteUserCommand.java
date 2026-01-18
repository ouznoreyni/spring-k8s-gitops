package sn.noreyni.springapi.application.usecase.user.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import sn.noreyni.springapi.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class DeleteUserCommand {
    private final UserRepository userRepository;

    public Mono<Void> execute(Long id) {
        return userRepository.deleteById(id);
    }
}
