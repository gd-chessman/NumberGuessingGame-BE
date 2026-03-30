package org.example.numberguessinggame.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuessRequest {

    @NotNull(message = "number is required")
    @Min(1)
    @Max(5)
    private Integer number;
}
