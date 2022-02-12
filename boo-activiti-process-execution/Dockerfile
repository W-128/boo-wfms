FROM java:8
COPY target/*.jar /app.jar
ENTRYPOINT ["java","-Duser.timezone=GMT+08","-Xmx4096M", "-Xms4096M" ,"-Xmn1536M","-XX:MetaspaceSize=256M","-XX:MaxMetaspaceSize=256M","-jar","/app.jar"]
EXPOSE 10238