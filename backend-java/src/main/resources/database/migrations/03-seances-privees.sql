-- Migration (base deja creee) : table des seances de coaching prive + exemples.
-- Sans perte de donnees. Les exemples ne sont ajoutes que si la table est vide.
USE gestion_salle_sport;
-- Seances de coaching prive : un adherent reserve un coach pour une seance individuelle
CREATE TABLE IF NOT EXISTS seance_privee (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    coach_id       INT NOT NULL,
    adherent_id    INT NOT NULL,
    date_seance    DATE NOT NULL,
    heure          TIME NOT NULL,
    duree_minutes  INT NOT NULL DEFAULT 60,
    objectif       VARCHAR(255),
    statut         VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE', -- EN_ATTENTE, CONFIRMEE ou ANNULEE
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seance_coach FOREIGN KEY (coach_id)
        REFERENCES coach(id) ON DELETE CASCADE,
    CONSTRAINT fk_seance_adherent FOREIGN KEY (adherent_id)
        REFERENCES adherent(id) ON DELETE CASCADE
);

-- coach_id : 1 = Sami, 2 = Leila ; adherent_id : 1 = Yassine, 2 = Nour, 3 = Rania
INSERT INTO seance_privee (coach_id, adherent_id, date_seance, heure, duree_minutes, objectif, statut)
SELECT * FROM (
    SELECT 1 AS coach, 1 AS adherent, '2026-11-17' AS jour, '17:00:00' AS heure, 60 AS duree, 'Programme de prise de masse' AS objectif, 'CONFIRMEE' AS statut
    UNION ALL SELECT 2, 3, '2026-11-18', '09:00:00', 45, 'Souplesse et posture', 'EN_ATTENTE'
    UNION ALL SELECT 1, 2, '2026-11-20', '12:00:00', 60, 'Preparation semi-marathon', 'EN_ATTENTE'
    UNION ALL SELECT 2, 1, '2026-11-05', '18:00:00', 60, 'Bilan de forme', 'CONFIRMEE'
) AS exemples
WHERE NOT EXISTS (SELECT 1 FROM seance_privee);
