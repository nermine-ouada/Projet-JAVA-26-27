package tn.enicarthage.gestionSalleDeSport.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Hash des mots de passe : on ne stocke JAMAIS le mot de passe en clair.
 * Format stocke en base : "sel:sha256(sel + motDePasse)" (hexadecimal).
 * Meme calcul que SHA2(CONCAT(sel, mdp), 256) en SQL (voir database.sql).
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hasher(String motDePasse) {
        byte[] selBytes = new byte[16];
        RANDOM.nextBytes(selBytes);
        String sel = HexFormat.of().formatHex(selBytes);
        return sel + ":" + sha256(sel + motDePasse);
    }

    public static boolean verifier(String motDePasse, String stocke) {
        if (motDePasse == null || stocke == null || !stocke.contains(":")) {
            return false;
        }
        String sel = stocke.substring(0, stocke.indexOf(':'));
        String attendu = stocke.substring(stocke.indexOf(':') + 1);
        // Comparaison en temps constant
        return MessageDigest.isEqual(
                attendu.getBytes(StandardCharsets.UTF_8),
                sha256(sel + motDePasse).getBytes(StandardCharsets.UTF_8));
    }

    // Token de session aleatoire (64 caracteres hexadecimaux)
    public static String genererToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String sha256(String texte) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(texte.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
