-- Migration (base deja creee) : cours des prochaines semaines + inscriptions.
-- A executer UNE SEULE FOIS (sinon les cours sont crees en double).
USE gestion_salle_sport;
-- Cours des prochaines semaines (agenda). coach_id : 1 = Sami, 2 = Leila. salle_id : 1 Musculation, 2 Yoga, 3 Cardio
INSERT INTO cours (nom_cours, date_cours, heure, capacite, coach_id, salle_id) VALUES
    ('Yoga du soir',        '2026-11-11', '18:30:00', 12, 2, 2),
    ('CrossFit du matin',   '2026-11-12', '07:30:00', 15, 1, 1),
    ('Cardio intensif',     '2026-11-13', '19:00:00', 20, 1, 3),
    ('Pilates',             '2026-11-14', '10:00:00', 10, 2, 2),
    ('Musculation guidee',  '2026-11-18', '12:30:00', 20, 1, 1),
    ('Pilates',             '2026-11-19', '18:00:00', 10, 2, 2),
    ('CrossFit du samedi',  '2026-11-21', '10:00:00',  2, 1, 1),
    ('CrossFit du matin',   '2026-11-23', '08:00:00', 15, 1, 1),
    ('Yoga du soir',        '2026-11-23', '18:00:00', 12, 2, 2),
    ('Cardio intensif',     '2026-11-25', '19:00:00', 20, 1, 3),
    ('Pilates',             '2026-11-28', '10:00:00', 10, 2, 2);

-- Quelques inscriptions (adherent_id : 1 = Yassine, 2 = Nour, 3 = Rania).
-- Le "CrossFit du samedi" (2 places) est complet : pratique pour tester.
INSERT INTO inscription_cours (adherent_id, cours_id)
SELECT 2, id FROM cours WHERE nom_cours = 'Yoga du soir' AND date_cours = '2026-11-11'
UNION ALL SELECT 3, id FROM cours WHERE nom_cours = 'Yoga du soir' AND date_cours = '2026-11-11'
UNION ALL SELECT 1, id FROM cours WHERE nom_cours = 'CrossFit du matin' AND date_cours = '2026-11-12'
UNION ALL SELECT 2, id FROM cours WHERE nom_cours = 'CrossFit du samedi' AND date_cours = '2026-11-21'
UNION ALL SELECT 3, id FROM cours WHERE nom_cours = 'CrossFit du samedi' AND date_cours = '2026-11-21';
