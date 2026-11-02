package tn.enicarthage.gestionSalleDeSport.exception;

// Non connecte ou token invalide -> 401 Unauthorized
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
