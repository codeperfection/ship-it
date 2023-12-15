FROM gradle:8.5-jdk21 as build
COPY . /app
RUN addgroup shipit \
    && adduser --system --disabled-password --no-create-home --shell /bin/false --home /app --ingroup shipit shipit \
    && chown -R shipit:shipit /app
USER shipit
WORKDIR /app
RUN gradle clean build

FROM amazoncorretto:21-alpine-jdk
RUN addgroup shipit \
    && adduser --system --disabled-password --shell /bin/false --home /app --ingroup shipit shipit
USER shipit
COPY --from=build /app/build/libs/ship-it-*.jar /app/app.jar
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "/app/app.jar"]
