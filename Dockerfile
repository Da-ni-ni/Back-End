# 1단계: Gradle 빌드
FROM gradle:8.5-jdk17 AS builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle build -x test

# 2단계: 실행용 이미지
FROM openjdk:17
WORKDIR /app
# 와일드카드(*) 대신 명확한 파일명을 사용하여 JAR 복사
COPY --from=builder /home/gradle/project/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
