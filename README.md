# HeroLab Spotnow 

Currently only SpotNow server code Parkonect API module. 
It is used for custom parking transaction tracking and charge.

## How to run:

It is regular java process:

From build:
```console
java -jar build/libs/spotnow-server.jar h2Db -m test
```

From source:
```console
./gradlew run --args="h2Db -m test"
```

From docker:
```console
docker build -t spotnow-server .
docker run --rm -p 80:8081 spotnow-server
```

## Implementation stack:

Used quick prototyping with:
- Ktor REST API, serialization and authentication layer
- Exposed db ORM layer for database storage.
- Docker image for deployment.


### Api documentation

todo: swagger documentation