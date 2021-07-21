# Prometheus Demo

This is a demo app to feed Prometheus with some data.

It uses information about the International Space Station (ISS, acquired through the ["Where the ISS at?" REST API](https://wheretheiss.at/w/developer)) 
and about the weather on Mars (acquired through the [MAAS2 API](https://maas2.apollorion.com/), but originating from the Curiosity Rover) 
to generate Metrics which can be scraped by a Prometheus instance.

There are two (functionally near identical) implementations using different Java frameworks:
* Quarkus
* Spring Boot

## Quarkus

Execute the following from the `./quarkus` directory (using JDK 11 and Maven 3.8.1+) to build & publish the Quarkus implementation:

Build Native image (runnable application):
```
mvn -Pnative -Dquarkus.native.container-build=true package
```

Build Native app & Container image:
```
mvn -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true package
```

Build Native app & Container image and publish it to the Docker Hub:
```
mvn -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true package
```

**NOTE:** Adapt the image group name and set the corresponding credentials in the config before attempting to publish the image:
```
grep container src/main/resources/application.properties | grep -v jib
quarkus.container-image.group=<GROUP-NAME>
quarkus.container-image.name=prometheus-demo-quarkus
quarkus.container-image.username=<DOCKER-HUB-USERNAME>
quarkus.container-image.password=<DOCKER-HUB-PASSWORD>
```
