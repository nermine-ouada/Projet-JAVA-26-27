package tn.enicarthage.gestionSalleDeSport.dto;

// Utilisateur renvoye au client (sans le mot de passe)
public record UtilisateurResponse(int id, String nom, String prenom, String email, String role,
                                  String telephone, String specialite, String photo) {
}
