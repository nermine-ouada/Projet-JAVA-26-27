CREATE TABLE utilisateur (
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             nom VARCHAR(100) NOT NULL,
                             prenom VARCHAR(100) NOT NULL,
                             email VARCHAR(150) NOT NULL UNIQUE,
                             mot_de_passe VARCHAR(255) NOT NULL
);

CREATE TABLE adherent (
                          id INT PRIMARY KEY,
                          telephone VARCHAR(20),
                          FOREIGN KEY (id) REFERENCES utilisateur(id)
);

CREATE TABLE coach (
                       id INT PRIMARY KEY,
                       specialite VARCHAR(100),
                       telephone VARCHAR(20),
                       FOREIGN KEY (id) REFERENCES utilisateur(id)
);

CREATE TABLE administrateur (
                                id INT PRIMARY KEY,
                                FOREIGN KEY (id) REFERENCES utilisateur(id)
);

CREATE TABLE abonnement (
                            id_abonnement INT PRIMARY KEY AUTO_INCREMENT,
                            type VARCHAR(50) NOT NULL,
                            date_debut DATE NOT NULL,
                            date_fin DATE NOT NULL,
                            prix DECIMAL(10,2) NOT NULL,
                            id_adherent INT NOT NULL,
                            FOREIGN KEY (id_adherent) REFERENCES adherent(id)
);

CREATE TABLE paiement (
                          id_paiement INT PRIMARY KEY AUTO_INCREMENT,
                          montant DECIMAL(10,2) NOT NULL,
                          date_paiement DATE NOT NULL,
                          mode_paiement VARCHAR(50),
                          id_adherent INT NOT NULL,
                          FOREIGN KEY (id_adherent) REFERENCES adherent(id)
);

CREATE TABLE salle (
                       id_salle INT PRIMARY KEY AUTO_INCREMENT,
                       nom VARCHAR(100) NOT NULL,
                       capacite INT NOT NULL
);

CREATE TABLE cours (
                       id_cours INT PRIMARY KEY AUTO_INCREMENT,
                       nom_cours VARCHAR(100) NOT NULL,
                       date_cours DATE NOT NULL,
                       heure TIME NOT NULL,
                       capacite INT NOT NULL,
                       id_coach INT NOT NULL,
                       id_salle INT NOT NULL,
                       FOREIGN KEY (id_coach) REFERENCES coach(id),
                       FOREIGN KEY (id_salle) REFERENCES salle(id_salle)
);

CREATE TABLE inscription_cours (
                                   id_adherent INT,
                                   id_cours INT,
                                   PRIMARY KEY (id_adherent, id_cours),
                                   FOREIGN KEY (id_adherent) REFERENCES adherent(id),
                                   FOREIGN KEY (id_cours) REFERENCES cours(id_cours)
);