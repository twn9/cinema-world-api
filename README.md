# cinema-world-api

### Overview:
API for cinema world that can be used to view showtimes and book tickets 

### How to run:
Start the postgres container: `docker-compose up`  

Use to query directly to database: `docker exec -it {container name} psql -U postgres` 


Start the server: `sbt run`
http://localhost:8080/movies

To stop the server, press Enter in the console


To run tests `sbt test`. postgres must be running for api integration test to run.  



### Endpoints:
`GET movies`: returns all showing movies


`GET movies/{}`: takes movieId to get movie info


`GET movies/{}/showtimes`: takes movieId to get showtimes for the movie


`POST reserve/{}`: takes showtimeId to book showtime seat


`PUT cancel/{}`: takes reservationId to cancel reservation and decide penaltys 