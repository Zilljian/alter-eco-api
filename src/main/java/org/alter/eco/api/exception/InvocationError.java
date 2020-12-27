package org.alter.eco.api.exception;

public enum InvocationError {

    POSTGRES_INVOCATION_ERROR(500, "Error invoking Postgres");

    public final int status;
    public final String description;

    InvocationError(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public InvocationErrorException exception(String body) {
        return new InvocationErrorException(this, body);
    }

    public InvocationErrorException exception(Exception e, String body) {
        return new InvocationErrorException(e, this, body);
    }

    public InvocationErrorException exception(Throwable e) {
        return new InvocationErrorException(e, this);
    }

    public static class InvocationErrorException extends HttpCodeException {

        public InvocationErrorException(InvocationError error, String args) {
            super(error.status, error.description + ": ".concat(args));
        }

        public InvocationErrorException(Exception e, InvocationError error, String args) {
            super(e, error.status, error.description + ": ".concat(args));
        }

        public InvocationErrorException(Throwable e, InvocationError error) {
            super(e, error.status, error.description);
        }
    }
}
