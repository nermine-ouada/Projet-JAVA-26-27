package tn.enicarthage.gestionSalleDeSport.dto;

// Corps JSON de POST/PUT /utilisateurs (ADMIN)
// ex: {"nom":"Ali","prenom":"Sara","email":"sara@mail.tn","motDePasse":"secret1","role":"COACH",
//      "telephone":"22000000","specialite":"Boxe","photo":"data:image/jpeg;base64,..."}
// En modification (PUT) : motDePasse vide = inchange, role ignore,
// photo absente (null) = inchangee, photo "" = supprimee.
public record UtilisateurRequest(String nom, String prenom, String email, String motDePasse, String role,
                                 String telephone, String specialite, String photo) {
}
