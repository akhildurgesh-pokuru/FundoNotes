
package com.fundoonotes.exception;

public class InvalidAccountTokenException
        extends RuntimeException {

    public InvalidAccountTokenException(String message) {
        super(message);
    }
}
