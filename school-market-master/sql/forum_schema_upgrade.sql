ALTER TABLE forum
    CHANGE COLUMN img imgs TEXT NULL COMMENT 'forum images';

ALTER TABLE forum
    ADD COLUMN icon VARCHAR(500) NULL DEFAULT NULL COMMENT 'forum cover' AFTER pass_time;

ALTER TABLE forum
    ADD COLUMN type VARCHAR(255) NULL DEFAULT '校园日常' COMMENT 'forum type' AFTER icon;

ALTER TABLE forum
    ADD COLUMN manage VARCHAR(10) NOT NULL DEFAULT '1' COMMENT 'forum status' AFTER type;

ALTER TABLE forum
    ADD COLUMN update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time' AFTER manage;

UPDATE forum
SET icon = SUBSTRING_INDEX(imgs, ',', 1)
WHERE (icon IS NULL OR icon = '')
  AND imgs IS NOT NULL
  AND imgs <> '';

UPDATE forum
SET type = '校园日常'
WHERE type IS NULL
   OR type = '';

UPDATE forum
SET manage = '1'
WHERE manage IS NULL
   OR manage = '';
