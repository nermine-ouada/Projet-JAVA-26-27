package tn.enicarthage.gestionSalleDeSport.dto;

// Reponse de login : le token a renvoyer dans "Authorization: Bearer <token>"
public record AuthResponse(String token, UtilisateurResponse utilisateur) {
}
