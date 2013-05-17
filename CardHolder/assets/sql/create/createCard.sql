CREATE TABLE IF NOT EXISTS "card" (
	cardId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
	frontImage BLOB,
	backImage BLOB,
	cardName TEXT,
	sortNum INTEGER,
	date DATE,
	memo TEXT)