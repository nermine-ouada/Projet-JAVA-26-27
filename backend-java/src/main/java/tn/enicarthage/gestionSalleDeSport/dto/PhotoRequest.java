package tn.enicarthage.gestionSalleDeSport.dto;

// Corps JSON de PUT /auth/photo : {"photo":"data:image/jpeg;base64,..."} ou {"photo":""} pour la supprimer
public record PhotoRequest(String photo) {
}
