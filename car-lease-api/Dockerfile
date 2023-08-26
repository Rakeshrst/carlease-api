FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar car-lease-api.jar
ENTRYPOINT ["java","-jar","/car-lease-api.jar"]