package ua.rud.teammanagementsystem.Exceptions;

public class ConflictRequest extends RuntimeException {
    public ConflictRequest(String message) {
        super(message);
    }
}
