package tn.enicarthage.gestionSalleDeSport.dto;

// Corps JSON de POST/PUT /utilisateurs (ADMIN)
// ex: {"nom":"Ali","prenom":"Sara","email":"sara@mail.tn","motDePasse":"secret1","role":"COACH"}
// En modification (PUT) : motDePasse vide = inchange, role ignore.
public record UtilisateurRequest(String nom, String prenom, String email, String motDePasse, String role) {
}
