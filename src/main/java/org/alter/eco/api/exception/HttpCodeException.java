package org.alter.eco.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class HttpCodeException extends ResponseStatusException {

    public final HttpStatus status;
    public final String body;

    public HttpCodeException(int status, String description) {
        this(HttpStatus.valueOf(status), description);
    }

    public HttpCodeException(HttpStatus status, String body) {
        super(status, body);
        this.status = status;
        this.body = body;
    }

    public HttpCodeException(Throwable cause, int status, String body) {
        super(HttpStatus.valueOf(status), body);
        this.initCause(cause);
        this.status = HttpStatus.valueOf(status);
        this.body = body;
    }

    @Override
    public String toString() {
        return "'" + body + "'";
    }
}
