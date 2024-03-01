create extension hstore;
create SCHEMA movies;
create table if not exists movies."Movie" (
    "id" BIGSERIAL NOT NULL PRIMARY KEY,
    "title" VARCHAR NOT NULL
);

create table if not exists movies."ShowTime" (
    "id" BIGSERIAL NOT NULL PRIMARY KEY,
    "movieId" BIGSERIAL NOT NULL,
    FOREIGN KEY ("movieId") REFERENCES movies."Movie"("id"),
    "date" VARCHAR NOT NULL,
    "time" VARCHAR NOT NULL,
    "max" INTEGER NOT NULL, 
    "reservations" INTEGER NOT NULL
);

create table if not exists movies."Reservation" (
    "id" BIGSERIAL NOT NULL PRIMARY KEY,
    "showtimeId" BIGSERIAL NOT NULL,
    FOREIGN KEY ("showtimeId") REFERENCES movies."ShowTime"("id"),
    "penalty" FLOAT8
);