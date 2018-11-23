FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY pokemon.jar .
CMD ["java","-jar","/pokemon.jar"]