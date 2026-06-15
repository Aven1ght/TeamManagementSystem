package ua.rud.teammanagementsystem.exceptions;

public class ConflictRequest extends RuntimeException {
    public ConflictRequest(String message) {
        super(message);
    }
}
