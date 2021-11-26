FROM gradle:jdk11 as gradleimage
COPY . /home/gradle/source
WORKDIR /home/gradle/source
RUN gradle shadowJar

FROM openjdk:11-jre-slim
COPY --from=gradleimage /home/gradle/source/build/libs/io.standel.cards.card-service-0.0.1-all.jar /app/
WORKDIR /app
ENTRYPOINT ["java", "-jar", "io.standel.cards.card-service-0.0.1-all.jar"]