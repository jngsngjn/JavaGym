ALTER TABLE trainer
ADD COLUMN t_height DECIMAL(5,1) NOT NULL,
ADD COLUMN t_weight DECIMAL(5,1) NOT NULL,
ADD COLUMN t_photo MEDIUMBLOB;