package tn.enicarthage.gestionSalleDeSport.controller;

import tn.enicarthage.gestionSalleDeSport.model.Cours;
import tn.enicarthage.gestionSalleDeSport.model.Participant;
import tn.enicarthage.gestionSalleDeSport.service.InscriptionService;

import java.sql.SQLException;
import java.util.List;

public class InscriptionController {

    private final InscriptionService inscriptionService = new InscriptionService();

    public Cours inscrire(int coursId, int utilisateurId) throws SQLException {
        return inscriptionService.inscrire(coursId, utilisateurId);
    }

    public void desinscrire(int coursId, int utilisateurId) throws SQLException {
        inscriptionService.desinscrire(coursId, utilisateurId);
    }

    public List<Participant> getParticipants(int coursId) throws SQLException {
        return inscriptionService.getParticipants(coursId);
    }
}
