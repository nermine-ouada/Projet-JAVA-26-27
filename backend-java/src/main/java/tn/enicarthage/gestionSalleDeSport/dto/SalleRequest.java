package tn.enicarthage.gestionSalleDeSport.dto;

// Corps JSON de POST/PUT /salles  ex: {"nom":"Salle Boxe","capacite":15}
public record SalleRequest(String nom, Integer capacite) {
}
