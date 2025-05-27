FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY marca_etec.png marca_etec.png
COPY marca_um.png marca_um.png
COPY marca_um_65.png marca_um_65.png
COPY medio_pago_1.png medio_pago_1.png
COPY medio_pago_2.png medio_pago_2.png
ENTRYPOINT ["java","-jar","/app.jar"]
