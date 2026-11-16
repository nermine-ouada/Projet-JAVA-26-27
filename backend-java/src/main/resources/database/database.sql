DROP DATABASE IF EXISTS gestion_salle_sport;
CREATE DATABASE gestion_salle_sport
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE gestion_salle_sport;

-- Convention (Guide) : chaque table contient id, created_at, updated_at.

-- =====================================================
-- 1. TABLES SANS DEPENDANCE
-- =====================================================
CREATE TABLE utilisateur (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(255) NOT NULL, -- format "sel:sha256(sel + mot_de_passe)"
    photo         MEDIUMTEXT NULL,       -- image "data:image/jpeg;base64,..." (facultative)
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE salle (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL,
    capacite    INT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. ROLES (heritage de utilisateur, relation 1:1)
-- =====================================================
CREATE TABLE adherent (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id  INT NOT NULL UNIQUE,
    telephone       VARCHAR(20),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_adherent_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur(id) ON DELETE CASCADE
);

CREATE TABLE coach (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id  INT NOT NULL UNIQUE,
    specialite      VARCHAR(100),
    telephone       VARCHAR(20),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_coach_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur(id) ON DELETE CASCADE
);

CREATE TABLE administrateur (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id  INT NOT NULL UNIQUE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_administrateur_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur(id) ON DELETE CASCADE
);

-- Sessions de connexion : un token par connexion (envoye dans "Authorization: Bearer <token>")
CREATE TABLE session (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id  INT NOT NULL,
    token           VARCHAR(64) NOT NULL UNIQUE,
    expire_le       DATETIME NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur(id) ON DELETE CASCADE
);

-- =====================================================
-- 3. TABLES AVEC CLES ETRANGERES
-- =====================================================
CREATE TABLE abonnement (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    type         VARCHAR(50) NOT NULL,
    date_debut   DATE NOT NULL,
    date_fin     DATE NOT NULL,
    prix         DECIMAL(10,2) NOT NULL,
    adherent_id  INT NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_abonnement_adherent FOREIGN KEY (adherent_id)
        REFERENCES adherent(id) ON DELETE CASCADE
);

CREATE TABLE paiement (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    montant        DECIMAL(10,2) NOT NULL,
    date_paiement  DATE NOT NULL,
    mode_paiement  VARCHAR(50),
    adherent_id    INT NOT NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_paiement_adherent FOREIGN KEY (adherent_id)
        REFERENCES adherent(id) ON DELETE CASCADE
);

CREATE TABLE cours (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom_cours   VARCHAR(100) NOT NULL,
    date_cours  DATE NOT NULL,
    heure       TIME NOT NULL,
    capacite    INT NOT NULL,
    coach_id    INT NOT NULL,
    salle_id    INT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cours_coach FOREIGN KEY (coach_id) REFERENCES coach(id),
    CONSTRAINT fk_cours_salle FOREIGN KEY (salle_id) REFERENCES salle(id)
);

-- =====================================================
-- 4. TABLE INTERMEDIAIRE N:N
-- =====================================================
CREATE TABLE inscription_cours (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    adherent_id  INT NOT NULL,
    cours_id     INT NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_inscription UNIQUE (adherent_id, cours_id),
    CONSTRAINT fk_inscription_adherent FOREIGN KEY (adherent_id)
        REFERENCES adherent(id) ON DELETE CASCADE,
    CONSTRAINT fk_inscription_cours FOREIGN KEY (cours_id)
        REFERENCES cours(id) ON DELETE CASCADE
);

-- Tables ajoutees par chaque branche (chacune ecrit sous sa ligne)
-- [eya] tables

-- [nermine] tables
-- Seances de coaching prive : un adherent reserve un coach pour une seance individuelle
CREATE TABLE seance_privee (
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


-- =====================================================
-- 5. DONNEES DE TEST
-- Mots de passe hashes avec SHA2 (meme algorithme que PasswordUtil.java)
--   admin@fitness.tn        / admin123
--   sami.coach@fitness.tn   / coach123
--   leila.coach@fitness.tn  / coach123
--   yassine@mail.tn, nour@mail.tn, rania@mail.tn / pass123
-- =====================================================
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe) VALUES
    ('Ben Ali',  'Ahmed',   'admin@fitness.tn',       CONCAT('s1:', SHA2(CONCAT('s1', 'admin123'), 256))),
    ('Trabelsi', 'Sami',    'sami.coach@fitness.tn',  CONCAT('s2:', SHA2(CONCAT('s2', 'coach123'), 256))),
    ('Mansouri', 'Leila',   'leila.coach@fitness.tn', CONCAT('s3:', SHA2(CONCAT('s3', 'coach123'), 256))),
    ('Karoui',   'Yassine', 'yassine@mail.tn',        CONCAT('s4:', SHA2(CONCAT('s4', 'pass123'), 256))),
    ('Gharbi',   'Nour',    'nour@mail.tn',           CONCAT('s5:', SHA2(CONCAT('s5', 'pass123'), 256))),
    ('Jebali',   'Rania',   'rania@mail.tn',          CONCAT('s6:', SHA2(CONCAT('s6', 'pass123'), 256)));

INSERT INTO administrateur (utilisateur_id) VALUES (1);

INSERT INTO coach (utilisateur_id, specialite, telephone) VALUES
    (2, 'Musculation',     '71000001'),
    (3, 'Yoga et Pilates', '71000002');

INSERT INTO adherent (utilisateur_id, telephone) VALUES
    (4, '71000003'),
    (5, '71000004'),
    (6, '71000005');

INSERT INTO salle (nom, capacite) VALUES
    ('Salle Musculation', 30),
    ('Salle Yoga',        20),
    ('Salle Cardio',      25);

INSERT INTO abonnement (type, date_debut, date_fin, prix, adherent_id) VALUES
    ('Mensuel',     '2026-01-01', '2026-02-01',  80.00, 1),
    ('Trimestriel', '2026-01-15', '2026-04-15', 210.00, 2),
    ('Annuel',      '2026-01-01', '2027-01-01', 700.00, 3);

INSERT INTO paiement (montant, date_paiement, mode_paiement, adherent_id) VALUES
    ( 80.00, '2026-01-01', 'Carte bancaire', 1),
    (210.00, '2026-01-15', 'Especes',        2),
    (700.00, '2026-01-01', 'Virement',       3);

-- coach_id 1 = Sami, 2 = Leila
INSERT INTO cours (nom_cours, date_cours, heure, capacite, coach_id, salle_id) VALUES
    ('CrossFit du matin', '2026-11-16', '08:00:00', 15, 1, 1),
    ('Yoga du soir',      '2026-11-16', '18:00:00', 12, 2, 2),
    ('Cardio intensif',   '2026-11-17', '07:30:00', 20, 1, 3);

INSERT INTO inscription_cours (adherent_id, cours_id) VALUES
    (1, 1),
    (2, 2),
    (3, 1),
    (3, 3);

-- Donnees de test ajoutees par chaque branche
-- [eya] donnees
-- Deux salles de plus : boxe et cycling (ids 4 et 5)
INSERT INTO salle (nom, capacite) VALUES
    ('Salle de Boxe',  16),
    ('Studio Cycling', 18);

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

-- Cours dans les nouvelles salles
INSERT INTO cours (nom_cours, date_cours, heure, capacite, coach_id, salle_id) VALUES
    ('Boxe anglaise',  '2026-11-17', '19:00:00', 14, 1, 4),
    ('Spinning',       '2026-11-18', '07:00:00', 18, 2, 5),
    ('Boxe anglaise',  '2026-11-24', '19:00:00', 14, 1, 4);

-- [nermine] donnees
-- Seances privees (coach_id : 1 = Sami, 2 = Leila ; adherent_id : 1 = Yassine, 2 = Nour, 3 = Rania)
INSERT INTO seance_privee (coach_id, adherent_id, date_seance, heure, duree_minutes, objectif, statut) VALUES
    (1, 1, '2026-11-17', '17:00:00', 60, 'Programme de prise de masse', 'CONFIRMEE'),
    (2, 3, '2026-11-18', '09:00:00', 45, 'Souplesse et posture',        'EN_ATTENTE'),
    (1, 2, '2026-11-20', '12:00:00', 60, 'Preparation semi-marathon',   'EN_ATTENTE'),
    (2, 1, '2026-11-05', '18:00:00', 60, 'Bilan de forme',              'CONFIRMEE');
