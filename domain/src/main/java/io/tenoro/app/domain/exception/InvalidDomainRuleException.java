package io.tenoro.app.domain.exception;

public class InvalidDomainRuleException extends RuntimeException {
    public InvalidDomainRuleException(String message) {
        super(message);
    }
}
