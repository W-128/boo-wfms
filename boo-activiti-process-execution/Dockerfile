FROM java:8
COPY target/*.jar /app.jar
ENTRYPOINT ["java","-Xmx2688M", "-Xms2688M" ,"-Xmn960M","-XX:MetaspaceSize=256M","-XX:MaxMetaspaceSize=256M","-jar","/app.jar"]
EXPOSE 10238