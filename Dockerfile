# Build from the repository root: the backend also packages shared form rules.
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
COPY frontend/src/formRules.json frontend/src/formRules.json
RUN mvn -B -ntp -f backend/pom.xml -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system hms && useradd --system --gid hms --home-dir /app hms
COPY --from=build --chown=hms:hms /workspace/backend/target/hms-1.0.0.jar /app/hms.jar
COPY --chown=hms:hms deploy/start-render.sh /app/start-render.sh
COPY --chown=hms:hms deploy/aiven-ca.crt /app/aiven-ca.crt
RUN chmod 0555 /app/start-render.sh
USER hms
ENV SPRING_PROFILES_ACTIVE=render
ENV DB_CA_CERT_FILE=/app/aiven-ca.crt
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC"
EXPOSE 10000
ENTRYPOINT ["/app/start-render.sh"]
