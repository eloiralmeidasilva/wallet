package com.eloir.wallet.validation;

public interface Validator<T> {
    void validate(T input) throws IllegalArgumentException;
}

