package com.mvc.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super(String.format("Пользователь с email '%s' уже используется", email));
    }
}
