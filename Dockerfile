#getting base image
FROM java:8
EXPOSE 8080
VOLUME /tmp
ADD target/curious-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]