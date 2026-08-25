# 1단계: 빌드 환경 (JDK 21)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 프로젝트 전체 복사
COPY . .

# gradlew 실행 권한 부여 및 빌드 실행
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon -x test

# 2단계: 런타임 환경 (경량화 JRE 21 Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Xms128m", "-Xmx320m", "-XX:+UseG1GC", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]