CREATE TABLE Book( -- df: mult=100.0
  bid SERIAL PRIMARY KEY,
  title TEXT NOT NULL
);

CREATE TABLE Reader(
  rid SERIAL PRIMARY KEY,
  firstname TEXT NOT NULL,
  lastname TEXT NOT NULL,
  born DATE NOT NULL,
  gender BOOLEAN NOT NULL,
  phone TEXT -- nullable, maybe no phone
);

CREATE TABLE Borrow( --df: mult=1.5
  borrowed TIMESTAMP NOT NULL,
  rid INTEGER NOT NULL REFERENCES Reader,
  bid INTEGER NOT NULL REFERENCES Book,
  -- PRIMARY KEY(bid) -- a book is borrowed at most once at a time!
);
 