package tn.enicarthage.gestionSalleDeSport.dto;

// Corps JSON de PUT /seances/{id}/statut : {"statut":"CONFIRMEE"} ou {"statut":"ANNULEE"}
public record StatutRequest(String statut) {
}
