package org.alter.eco.api.exception;

public enum ApplicationError {

    TASK_NOT_FOUND_BY_ID(400, "Task not found error"),
    ATTACHMENTS_NOT_FOUND(400, "Attachments not found error");

    public final int status;
    public final String description;

    ApplicationError(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public ApplicationErrorException exception(String body) {
        return new ApplicationErrorException(this, body);
    }

    public ApplicationErrorException exception(Exception e, String body) {
        return new ApplicationErrorException(e, this, body);
    }

    public ApplicationErrorException exception(Exception e) {
        return new ApplicationErrorException(e, this);
    }

    public ApplicationErrorException exception() {
        return new ApplicationErrorException(this);
    }

    public static class ApplicationErrorException extends HttpCodeException {

        public ApplicationErrorException(ApplicationError error, String args) {
            super(error.status, error.description + ": ".concat(args));
        }

        public ApplicationErrorException(Exception e, ApplicationError error, String args) {
            super(e, error.status, error.description + ": ".concat(args));
        }

        public ApplicationErrorException(Exception e, ApplicationError error) {
            super(e, error.status, error.description);
        }

        public ApplicationErrorException(ApplicationError error) {
            super(error.status, error.description);
        }
    }
}
