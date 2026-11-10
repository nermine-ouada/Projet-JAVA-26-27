package tn.enicarthage.gestionSalleDeSport.dto;

import java.time.LocalDate;
import java.time.LocalTime;

// Corps JSON de POST /seances
// ex: {"coachId":1,"dateSeance":"2026-11-25","heure":"17:00","dureeMinutes":60,"objectif":"Perdre 3 kg"}
// adherentId : seulement quand l'admin reserve pour un adherent (l'adherent reserve pour lui-meme).
public record SeanceRequest(Integer coachId, Integer adherentId, LocalDate dateSeance, LocalTime heure,
                            Integer dureeMinutes, String objectif) {
}
