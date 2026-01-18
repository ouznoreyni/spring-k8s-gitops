package sn.noreyni.springapi.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BlogException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    public BlogException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}
