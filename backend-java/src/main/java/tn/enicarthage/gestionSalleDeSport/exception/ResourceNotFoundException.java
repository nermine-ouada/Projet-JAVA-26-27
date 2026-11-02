package tn.enicarthage.gestionSalleDeSport.exception;

// Ressource introuvable -> 404 Not Found
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
