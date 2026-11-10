package tn.enicarthage.gestionSalleDeSport.service;

import tn.enicarthage.gestionSalleDeSport.dao.SessionDao;
import tn.enicarthage.gestionSalleDeSport.dao.UtilisateurDao;
import tn.enicarthage.gestionSalleDeSport.dto.AuthResponse;
import tn.enicarthage.gestionSalleDeSport.dto.LoginRequest;
import tn.enicarthage.gestionSalleDeSport.dto.RegisterRequest;
import tn.enicarthage.gestionSalleDeSport.dto.UtilisateurRequest;
import tn.enicarthage.gestionSalleDeSport.dto.UtilisateurResponse;
import tn.enicarthage.gestionSalleDeSport.exception.UnauthorizedException;
import tn.enicarthage.gestionSalleDeSport.model.Utilisateur;
import tn.enicarthage.gestionSalleDeSport.util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {

    private static final int DUREE_SESSION_JOURS = 7;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final SessionDao sessionDao = new SessionDao();
    private final UtilisateurService utilisateurService = new UtilisateurService();

    public AuthResponse login(LoginRequest request) throws SQLException {
        if (estVide(request.email()) || estVide(request.motDePasse())) {
            throw new IllegalArgumentException("Email et mot de passe obligatoires.");
        }
        Utilisateur utilisateur = utilisateurDao.getUtilisateurByEmail(request.email().trim().toLowerCase());
        // Meme message dans les deux cas : on ne revele pas si l'email existe
        if (utilisateur == null || !PasswordUtil.verifier(request.motDePasse(), utilisateur.getMotDePasse())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect.");
        }
        return ouvrirSession(utilisateur.getId());
    }

    // Inscription publique : cree toujours un ADHERENT (memes validations que le CRUD)
    public AuthResponse register(RegisterRequest r) throws SQLException {
        UtilisateurResponse cree = utilisateurService.createUtilisateur(
                new UtilisateurRequest(r.nom(), r.prenom(), r.email(), r.motDePasse(), "ADHERENT",
                        null, null, null));
        return ouvrirSession(cree.id());
    }

    // Changer la photo de l'utilisateur connecte
    public UtilisateurResponse updateMaPhoto(int utilisateurId, String photo) throws SQLException {
        return utilisateurService.updatePhoto(utilisateurId, photo);
    }

    public void logout(String token) throws SQLException {
        if (token != null) {
            sessionDao.deleteSession(token);
        }
    }

    // Utilise par le Router pour proteger les routes
    public UtilisateurResponse getMe(String token) throws SQLException {
        if (estVide(token)) {
            throw new UnauthorizedException("Connexion requise.");
        }
        Integer utilisateurId = sessionDao.getUtilisateurIdByToken(token);
        if (utilisateurId == null) {
            throw new UnauthorizedException("Session invalide ou expiree.");
        }
        Utilisateur utilisateur = utilisateurDao.getUtilisateur(utilisateurId);
        if (utilisateur == null) {
            throw new UnauthorizedException("Utilisateur introuvable.");
        }
        return UtilisateurService.versResponse(utilisateur);
    }

    private AuthResponse ouvrirSession(int utilisateurId) throws SQLException {
        String token = PasswordUtil.genererToken();
        sessionDao.createSession(utilisateurId, token, DUREE_SESSION_JOURS);
        return new AuthResponse(token, UtilisateurService.versResponse(utilisateurDao.getUtilisateur(utilisateurId)));
    }

    private boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
