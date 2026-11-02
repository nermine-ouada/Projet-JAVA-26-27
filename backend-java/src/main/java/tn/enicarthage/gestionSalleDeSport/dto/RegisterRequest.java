package tn.enicarthage.gestionSalleDeSport.dto;

// Corps JSON de POST /auth/register (cree toujours un compte ADHERENT)
public record RegisterRequest(String nom, String prenom, String email, String motDePasse) {
}
