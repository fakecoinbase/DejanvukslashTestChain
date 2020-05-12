FROM openjdk:12.0.2

EXPOSE 8080

ENV TESTCHAIN_PUBLICKEY=''
ENV TESTCHAIN_PRIVATEKEY=''

ADD ./target/api-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
