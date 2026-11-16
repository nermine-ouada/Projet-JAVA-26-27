-- Migration (base deja creee) : deux salles de plus (boxe, cycling) et leurs cours.
-- Sans perte de donnees ; rien n'est ajoute si les salles existent deja.
USE gestion_salle_sport;

INSERT INTO salle (nom, capacite)
SELECT 'Salle de Boxe', 16 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM salle WHERE nom = 'Salle de Boxe');
INSERT INTO salle (nom, capacite)
SELECT 'Studio Cycling', 18 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM salle WHERE nom = 'Studio Cycling');

-- coach_id : 1 = Sami, 2 = Leila
INSERT INTO cours (nom_cours, date_cours, heure, capacite, coach_id, salle_id)
SELECT 'Boxe anglaise', '2026-11-17', '19:00:00', 14, 1, id FROM salle
WHERE nom = 'Salle de Boxe' AND NOT EXISTS (SELECT 1 FROM cours WHERE nom_cours = 'Boxe anglaise' AND date_cours = '2026-11-17');
INSERT INTO cours (nom_cours, date_cours, heure, capacite, coach_id, salle_id)
SELECT 'Spinning', '2026-11-18', '07:00:00', 18, 2, id FROM salle
WHERE nom = 'Studio Cycling' AND NOT EXISTS (SELECT 1 FROM cours WHERE nom_cours = 'Spinning' AND date_cours = '2026-11-18');
INSERT INTO cours (nom_cours, date_cours, heure, capacite, coach_id, salle_id)
SELECT 'Boxe anglaise', '2026-11-24', '19:00:00', 14, 1, id FROM salle
WHERE nom = 'Salle de Boxe' AND NOT EXISTS (SELECT 1 FROM cours WHERE nom_cours = 'Boxe anglaise' AND date_cours = '2026-11-24');
