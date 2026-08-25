# 1단계: 빌드 환경 (Gradle + JDK 21)
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Gradle 캐시용 파일 및 소스 전체 복사
COPY . .

# 테스트 제외하고 JAR 빌드
RUN gradle bootJar --no-daemon -x test

# 2단계: 런타임 환경 (경량화 JRE 21 Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Xms128m", "-Xmx320m", "-XX:+UseG1GC", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]ㅎ