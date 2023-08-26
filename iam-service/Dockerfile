FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar iam-service.jar
ENTRYPOINT ["java","-jar","/iam-service.jar"]