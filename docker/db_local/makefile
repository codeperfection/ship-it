POSTGRES_CONTAINER_NAME = shipit_postgres
DB_PASSWORD = CVHVuUhi9YbDbaMoWk6A7Kp9AA
DB_NAME = shipit

.PHONY: run rm restart

run:
	docker run --name $(POSTGRES_CONTAINER_NAME) -e POSTGRES_PASSWORD=$(DB_PASSWORD) -e POSTGRES_DB=$(DB_NAME) -d -p 5439:5432 postgres

rm:
	docker stop $(POSTGRES_CONTAINER_NAME)
	docker rm $(POSTGRES_CONTAINER_NAME)

restart:
	docker restart $(POSTGRES_CONTAINER_NAME)
