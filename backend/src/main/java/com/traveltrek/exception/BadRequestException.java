package com.traveltrek.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * BadRequestException
 *
 * Purpose: Thrown when the client sends invalid or logically incorrect data.
 * Why it exists: To return HTTP 400 with a clear error message.
 * How it works: @ResponseStatus(HttpStatus.BAD_REQUEST) maps this to a 400 response.
 *
 * Example usage: throw new BadRequestException("No slots available for this package.");
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
