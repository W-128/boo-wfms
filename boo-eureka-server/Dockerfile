FROM java:8
COPY target/*.jar /app.jar
ENTRYPOINT ["java","-Xmx1344M", "-Xms1344M" ,"-Xmn448M","-XX:MetaspaceSize=192M","-XX:MaxMetaspaceSize=192M","-jar","/app.jar"]
EXPOSE 10231