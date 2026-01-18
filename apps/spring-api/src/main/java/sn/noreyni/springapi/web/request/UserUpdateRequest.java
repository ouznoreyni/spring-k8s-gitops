package sn.noreyni.springapi.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    private String firstName;
    private String lastName;

    @NotBlank
    @Email
    private String email;
}
