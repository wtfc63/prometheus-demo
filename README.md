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
```shell
$ mvn -Pnative -Dquarkus.native.container-build=true package
```

Build Native app & Container image:
```shell
$ mvn -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true package
```

Build Native app & Container image and publish it to the Docker Hub:
```shell
$ mvn -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true package
```

**NOTE:** Adapt the image group name and set the corresponding credentials in the config before attempting to publish the image:
```shell
$ grep container src/main/resources/application.properties | grep -v jib
quarkus.container-image.group=<GROUP-NAME>
quarkus.container-image.name=prometheus-demo-quarkus
quarkus.container-image.username=<DOCKER-HUB-USERNAME>
quarkus.container-image.password=<DOCKER-HUB-PASSWORD>
```

Alternatively, the credentials can also be provided as parameters for the `mvn` command: 
`-Dquarkus.container-image.password=<DOCKER-HUB-PASSWORD>`


## Spring Boot

Execute the following from the `./springboot` directory (using JDK 11 and Maven 3.8.1+) to build & publish the Spring Boot implementation:

Build Native container image:

```shell
$ mvn spring-boot:build-image
```

To push the image to the Docker Hub, tag it with the correct Group Name and push it:

```shell
$ docker image tag prometheus-demo-springboot:latest <GROUP-NAME>/prometheus-demo-springboot:latest
$ docker image push <GROUP-NAME>/prometheus-demo-springboot:latest
```

**NOTE:** Make sure you are logged into your Docker account before attempting to push the image 
(i.e. using `docker login`). 
