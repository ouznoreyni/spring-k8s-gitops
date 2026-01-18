package sn.noreyni.springapi.application.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import sn.noreyni.springapi.application.dto.UserDto;
import sn.noreyni.springapi.application.usecase.user.command.AdminCreateUserCommand;
import sn.noreyni.springapi.application.usecase.user.command.DeleteUserCommand;
import sn.noreyni.springapi.application.usecase.user.command.UpdateUserCommand;
import sn.noreyni.springapi.application.usecase.user.query.GetUserByIdQuery;
import sn.noreyni.springapi.application.usecase.user.query.GetUserListQuery;
import sn.noreyni.springapi.domain.model.User;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final AdminCreateUserCommand adminCreateUserCommand;
    private final GetUserListQuery getUserListQuery;
    private final GetUserByIdQuery getUserByIdQuery;
    private final UpdateUserCommand updateUserCommand;
    private final DeleteUserCommand deleteUserCommand;

    public Mono<UserDto> createUser(User user) {
        return adminCreateUserCommand.execute(user);
    }

    public Mono<Page<UserDto>> getUsers(Pageable pageable) {
        return getUserListQuery.execute(pageable);
    }

    public Mono<UserDto> getUserById(Long id) {
        return getUserByIdQuery.execute(id);
    }

    public Mono<UserDto> updateUser(Long id, User user) {
        return updateUserCommand.execute(id, user);
    }

    public Mono<Void> deleteUser(Long id) {
        return deleteUserCommand.execute(id);
    }
}
