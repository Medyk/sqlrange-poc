# Simple Temporal-Database implementation with PostgreSQL

See `wikipedia` for more info on [Temporal Database](https://en.wikipedia.org/wiki/Temporal_database)

For business dates + audit dates see [Bi-Temporal Modeling](https://en.wikipedia.org/wiki/Bitemporal_modeling)

## Run DB

```bash
# setup database
# run twice because there is no check for flyway to wait for database
podman compose up
CTRL+C
podman compose up

# if you need to change initial data you need to remove volume
# 0010-init-database.sh is called only once
podman compose down -v
```

## Run DemoApp

```bash
~/.jdks/temurin-25.0.4/bin/java @argfile com.example.sqlrange.SqlrangeApplication
```

## Available paths

| METHOD | PATH               | DESCRIPTION                                   |
|--------|:-------------------|-----------------------------------------------|
| PUT    | /{uuid}            | Update book                                   | 
| GET    | /{uuid}/-/history  | Get book revisions                            |
| GET    | /{uuid}/{revision} | Get book single revision                      |
| GET    | /{uuid}            | Get book current revision (current timestamp) |
| GET    | /                  | Get all book (current revision)               |

## Get some data

```bash
# get current revision of a book
curl -s http://localhost:8080/511635fa-6c85-4233-9dff-7d8f058c4a84 | jq

# get selected revision of a book
curl -s http://localhost:8080/511635fa-6c85-4233-9dff-7d8f058c4a84/2 | jq

# get history of a book (all revisions)
curl -s http://localhost:8080/511635fa-6c85-4233-9dff-7d8f058c4a84/-/history | jq
```

## Put some data

```bash
# revision is for optimistic locking - must be current revision number or conflict will be returned
curl -s -X PUT -H "Content-Type: application/json" \
  -d '{"revision":2,"title":"New Title"}' \
  http://localhost:8080/511635fa-6c85-4233-9dff-7d8f058c4a84 | jq

# check if revision 3 was created
curl -s http://localhost:8080/511635fa-6c85-4233-9dff-7d8f058c4a84/3 | jq
```

```json
{
  "uuid": "511635fa-6c85-4233-9dff-7d8f058c4a84",
  "revision": 3,
  "revisionFrom": "2026-08-06T21:27:08.732775Z",
  "revisionTo": "2026-08-06T21:27:37.418685Z",
  "title": "New Title",
  "author": "F. Scott Fitzgerald",
  "year": 1925,
  "genre": "Classic",
  "revisionRange": "[2026-08-06 23:27:08.732775+02,2026-08-06 23:27:37.418685+02)",
  "revisionRangeCc": "[2026-08-06 23:27:08.732775+02,2026-08-06 23:27:37.418685+02]"
}
```
