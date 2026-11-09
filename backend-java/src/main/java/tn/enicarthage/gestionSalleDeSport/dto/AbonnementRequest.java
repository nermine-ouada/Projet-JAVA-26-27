package tn.enicarthage.gestionSalleDeSport.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Corps JSON de POST/PUT /abonnements
// ex: {"type":"Mensuel","dateDebut":"2026-11-01","dateFin":"2026-12-01","prix":80,"adherentId":1}
public record AbonnementRequest(String type, LocalDate dateDebut, LocalDate dateFin,
                                BigDecimal prix, Integer adherentId) {
}
