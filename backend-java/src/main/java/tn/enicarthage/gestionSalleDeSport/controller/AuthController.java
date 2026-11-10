package tn.enicarthage.gestionSalleDeSport.controller;

import tn.enicarthage.gestionSalleDeSport.dto.AuthResponse;
import tn.enicarthage.gestionSalleDeSport.dto.LoginRequest;
import tn.enicarthage.gestionSalleDeSport.dto.UtilisateurResponse;
import tn.enicarthage.gestionSalleDeSport.service.AuthService;

import java.sql.SQLException;

public class AuthController {

    private final AuthService authService = new AuthService();

    public AuthResponse login(LoginRequest request) throws SQLException {
        return authService.login(request);
    }

    public void logout(String token) throws SQLException {
        authService.logout(token);
    }

    public UtilisateurResponse updateMaPhoto(int utilisateurId, String photo) throws SQLException {
        return authService.updateMaPhoto(utilisateurId, photo);
    }

    public UtilisateurResponse getMe(String token) throws SQLException {
        return authService.getMe(token);
    }
}
