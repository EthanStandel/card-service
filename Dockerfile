FROM openjdk:11-jdk
EXPOSE 80:8080
RUN ./gradlew
RUN ./gradlew shadowJar
CMD java -jar ./build/libs/io.standel.cards.card-service-0.0.1-all.jar