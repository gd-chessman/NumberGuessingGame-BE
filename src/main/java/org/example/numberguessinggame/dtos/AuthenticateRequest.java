package org.example.numberguessinggame.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
