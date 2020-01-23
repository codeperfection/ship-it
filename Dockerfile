FROM gradle:6.0.1-jdk11 as build
COPY . /app
RUN addgroup shipit \ 
    && adduser --system --disabled-password --no-create-home --shell /bin/false --home /app --group shipit \
    && chown -R shipit:shipit /app
USER shipit
WORKDIR /app
RUN gradle build

FROM openjdk:11-jre-slim
RUN addgroup shipit \
    && adduser --system --disabled-password --shell /bin/false --home /app --group shipit
USER shipit
COPY --from=build /app/build/libs/ship-it-*.jar /app/app.jar
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "/app/app.jar" ]
