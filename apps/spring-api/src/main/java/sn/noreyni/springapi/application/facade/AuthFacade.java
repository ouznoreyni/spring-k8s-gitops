package sn.noreyni.springapi.application.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sn.noreyni.springapi.application.usecase.user.command.CreateUserCommand;
import sn.noreyni.springapi.application.usecase.user.query.LoginQuery;
import sn.noreyni.springapi.domain.model.User;
import sn.noreyni.springapi.web.request.LoginRequest;
import sn.noreyni.springapi.web.response.AuthResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthFacade {
    private final CreateUserCommand createUserCommand;
    private final LoginQuery loginQuery;

    public Mono<AuthResponse> register(User user) {
        return createUserCommand.execute(user);
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return loginQuery.execute(request);
    }
}
