
# Run postgresql server like this
```
docker run --ulimit memlock=-1:-1 -d --rm=true --memory-swappiness=0 \
       --name postgres-quarkus-hibernate -e POSTGRES_USER=hibernate \
       -e POSTGRES_PASSWORD=hibernate \
       -p 5432:5432 postgres:10.5
       
docker exec -ti postgres-quarkus-hibernate psql -U hibernate -c "CREATE DATABASE hibernate_db1"       
docker exec -ti postgres-quarkus-hibernate psql -U hibernate -c "CREATE DATABASE hibernate_db2"       
docker exec -ti postgres-quarkus-hibernate psql -U hibernate -c "CREATE DATABASE hibernate_db3"       

docker exec -ti postgres-quarkus-hibernate psql -U hibernate -c "CREATE TABLE Gift(id bigint primary key, name text); CREATE sequence gift_id_seq;" hibernate_db1
docker exec -ti postgres-quarkus-hibernate psql -U hibernate -c "CREATE TABLE Gift(id bigint primary key, name text); CREATE sequence gift_id_seq;" hibernate_db2
docker exec -ti postgres-quarkus-hibernate psql -U hibernate -c "CREATE TABLE Gift(id bigint primary key, name text); CREATE sequence gift_id_seq;" hibernate_db3       

```

       