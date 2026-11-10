package tn.enicarthage.gestionSalleDeSport.dto;

import java.time.LocalDate;
import java.time.LocalTime;

// Corps JSON de POST/PUT /cours
// ex: {"nomCours":"Boxe","dateCours":"2026-11-20","heure":"18:00","capacite":10,"coachId":1,"salleId":1,"semaines":4}
// semaines (creation seulement, facultatif, 1 a 12) : cree le meme cours chaque semaine pendant N semaines.
public record CoursRequest(String nomCours, LocalDate dateCours, LocalTime heure,
                           Integer capacite, Integer coachId, Integer salleId, Integer semaines) {
}
