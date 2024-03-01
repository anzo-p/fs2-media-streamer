CREATE TABLE tracks (
  track_id VARCHAR PRIMARY KEY,
  title VARCHAR NOT NULL,
  artist VARCHAR NOT NULL,
  album VARCHAR,
  bitrate INTEGER,
  duration INTEGER NOT NULL,
  genre VARCHAR[],
  filepath VARCHAR,
  filesize INTEGER NOT NULL,
  format VARCHAR NOT NULL,
  year INTEGER
);

CREATE UNIQUE INDEX tracks_artist_idx ON tracks (artist);

CREATE UNIQUE INDEX tracks_album_genre_idx ON tracks (album, genre);
