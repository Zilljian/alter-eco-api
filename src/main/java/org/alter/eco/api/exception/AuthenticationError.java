package org.alter.eco.api.exception;

public enum AuthenticationError {

    UNAUTHORIZED(401, "Unauthorized");

    public final int status;
    public final String description;

    AuthenticationError(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public AuthenticationErrorException exception(Exception e, String body) {
        return new AuthenticationErrorException(e, this, body);
    }

    public AuthenticationErrorException exception(String body) {
        return new AuthenticationErrorException(this, body);
    }

    public AuthenticationErrorException exception() {
        return new AuthenticationErrorException(this);
    }

    public static class AuthenticationErrorException extends HttpCodeException {

        public AuthenticationErrorException(Exception e, AuthenticationError error, String args) {
            super(e, error.status, error.description + ": ".concat(args));
        }

        public AuthenticationErrorException(AuthenticationError error) {
            super(error.status, error.description);
        }

        public AuthenticationErrorException(AuthenticationError error, String args) {
            super(error.status, error.description + ": ".concat(args));
        }
    }
}
