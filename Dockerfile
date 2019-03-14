# --- Main SpotNow Server ---
FROM openjdk:8-jre-alpine as spotnow-server

ENV APPLICATION_USER ktor
RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY ./build/libs/spotnow-server.jar /app/spotnow-server.jar
WORKDIR /app

EXPOSE 8081

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap",\
"-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC",\
"-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar",\
"spotnow-server.jar", "h2Db", "-m test"]