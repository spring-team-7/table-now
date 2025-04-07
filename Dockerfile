FROM amazoncorretto:17-alpine

WORKDIR /app

COPY build/libs/*.jar /app/myapp.jar

ENV SPRING_PROFILES_ACTIVE=local

ENTRYPOINT ["java", "-jar", "/app/myapp.jar"]
