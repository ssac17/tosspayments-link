# 1단계: 빌드 환경 (OpenJDK 25)
FROM openjdk:25-ea-jdk-slim AS builder
WORKDIR /app

# 소스 전체 복사
COPY . .

# gradlew 실행 권한 부여 및 빌드 (Gradle 힙 메모리 제한으로 OOM 방지)
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon -x test -Dorg.gradle.jvmargs="-Xmx384m -XX:+UseSerialGC"

# 2단계: 런타임 환경 (OpenJDK 25)
FROM openjdk:25-ea-jdk-slim
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]