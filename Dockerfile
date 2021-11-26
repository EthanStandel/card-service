FROM openjdk:11-jdk
EXPOSE 80:8080
WORKDIR .
CMD ./gradlew
CMD ./gradlew shadowJar
ENTRYPOINT java -jar ./build/libs/io.standel.cards.card-service-0.0.1-all.jar