FROM java:8
COPY target/*.jar /app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar
EXPOSE 10237