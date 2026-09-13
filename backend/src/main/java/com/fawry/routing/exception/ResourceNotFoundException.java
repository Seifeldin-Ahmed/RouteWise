package com.fawry.routing.exception;

/** Something addressed by id does not exist. Maps to 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
