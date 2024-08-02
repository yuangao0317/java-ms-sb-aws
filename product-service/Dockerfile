FROM eclipse-temurin:21-jdk-alpine

VOLUME /tmp

ARG DEPENDENCY=build/libs
COPY ${DEPENDENCY}/BOOT-INF/classes /app
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.gao.product_service.ProductServiceApplication"]