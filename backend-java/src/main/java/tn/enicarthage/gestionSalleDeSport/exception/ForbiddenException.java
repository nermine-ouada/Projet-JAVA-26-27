package tn.enicarthage.gestionSalleDeSport.exception;

// Connecte mais role insuffisant -> 403 Forbidden
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
