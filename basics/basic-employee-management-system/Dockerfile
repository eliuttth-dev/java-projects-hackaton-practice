FROM eclipse-temurin:23-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y wget

RUN wget -O mysql-connector-j-8.0.33.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar

COPY . .

RUN javac -cp ".:mysql-connector-j-8.0.33.jar" EmployeeManagementSystem.java
CMD ["java", "-cp", ".:mysql-connector-j-8.0.33.jar", "EmployeeManagementSystem"]

