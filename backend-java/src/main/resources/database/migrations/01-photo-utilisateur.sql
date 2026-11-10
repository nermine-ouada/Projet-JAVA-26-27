-- Migration (base deja creee) : ajoute la photo des utilisateurs.
-- Sans perte de donnees. Pour une base neuve, database.sql suffit.
USE gestion_salle_sport;
ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS photo MEDIUMTEXT NULL AFTER mot_de_passe;
