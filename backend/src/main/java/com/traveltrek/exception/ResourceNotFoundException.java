package com.traveltrek.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ResourceNotFoundException
 *
 * Purpose: Thrown when a requested resource (user, package, itinerary, etc.) is not found.
 * Why it exists: Instead of returning null, we throw this exception to return HTTP 404.
 * How it works: @ResponseStatus(HttpStatus.NOT_FOUND) maps this exception to a 404 response.
 *
 * Example usage: throw new ResourceNotFoundException("Package not found with id: " + id);
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
