FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
COPY target/currency-converter-0.0.1.jar /currency-controller.jar
CMD ["java", "-jar", "/currency-controller.jar"]
