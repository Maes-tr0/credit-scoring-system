FROM maven:3.9-eclipse-temurin-17-focal

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

CMD ["java", "-cp", "target/credit-scoring-system-0.0.1-SNAPSHOT.jar", "io.github.nikita_dev.credit_scoring_system.training.ModelTrainer"]