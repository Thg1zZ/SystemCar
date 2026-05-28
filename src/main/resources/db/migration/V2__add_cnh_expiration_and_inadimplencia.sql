-- Migração V2 para adicionar expiração de CNH, inadimplência e controle de quilometragem

ALTER TABLE users ADD COLUMN cnh_expiration_date DATE;
ALTER TABLE users ADD COLUMN inadimplente BOOLEAN DEFAULT FALSE;

ALTER TABLE rentals ADD COLUMN initial_mileage INT;
ALTER TABLE rentals ADD COLUMN final_mileage INT;
