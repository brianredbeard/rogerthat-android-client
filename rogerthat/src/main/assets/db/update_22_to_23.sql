-- Add description field
ALTER TABLE friend ADD COLUMN description TEXT;

-- Add description branding field
ALTER TABLE friend ADD COLUMN description_branding TEXT;

-- Add poke description field
ALTER TABLE friend ADD COLUMN poke_description TEXT;
