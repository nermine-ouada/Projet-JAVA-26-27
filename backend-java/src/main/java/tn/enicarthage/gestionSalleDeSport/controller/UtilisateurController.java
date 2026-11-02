package tn.enicarthage.gestionSalleDeSport.controller;

import tn.enicarthage.gestionSalleDeSport.dto.UtilisateurRequest;
import tn.enicarthage.gestionSalleDeSport.dto.UtilisateurResponse;
import tn.enicarthage.gestionSalleDeSport.service.UtilisateurService;

import java.sql.SQLException;
import java.util.List;

public class UtilisateurController {

    private final UtilisateurService utilisateurService = new UtilisateurService();

    public List<UtilisateurResponse> getUtilisateurs() throws SQLException {
        return utilisateurService.getUtilisateurs();
    }

    public UtilisateurResponse getUtilisateur(int id) throws SQLException {
        return utilisateurService.getUtilisateur(id);
    }

    public UtilisateurResponse createUtilisateur(UtilisateurRequest request) throws SQLException {
        return utilisateurService.createUtilisateur(request);
    }

    public UtilisateurResponse updateUtilisateur(int id, UtilisateurRequest request) throws SQLException {
        return utilisateurService.updateUtilisateur(id, request);
    }

    public void deleteUtilisateur(int id, int idConnecte) throws SQLException {
        utilisateurService.deleteUtilisateur(id, idConnecte);
    }
}
