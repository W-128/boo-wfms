FROM openjdk:8
COPY target/*.jar /app.jar
#ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions \
#               -XX:+UseCGroupMemoryLimitForHeap \
#               -XX:MaxRAMPercentage=75.0 \
#               -XX:+HeapDumpOnOutOfMemoryError \
#               -XX:HeapDumpPath=/data/logs/"
#ENV JAVA_TOOL_OPTIONS="-server -XX:MaxMetaspaceSize=192M  -XX:MetaspaceSize=192M -Xms1344M   -Xmx1344M   -Xmn448M "
#ENTRYPOINT exec java ${JAVA_TOOL_OPTIONS} -jar /app.jar
#ENTRYPOINT java -Xmx1024m -jar /app.jar
# last used
#ENTRYPOINT java $JAVA_OPTS -XshowSettings:vm -version -jar /app.jar
#ENTRYPOINT ["java","-jar","-Xms1024m", "-Xmx1024m","/app.jar"]
# print
#ENTRYPOINT ["java","-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0" ,"-XshowSettings:vm","-version","-jar","/app.jar"]
ENTRYPOINT ["java","-Duser.timezone=GMT+08","-Xmx1344M", "-Xms1344M" ,"-Xmn448M","-XX:MetaspaceSize=192M","-XX:MaxMetaspaceSize=192M","-jar","/app.jar"]
#ENTRYPOINT ["java","-Duser.timezone=GMT+08","-Xmx2048M", "-Xms2048M" ,"-Xmn768M","-XX:MetaspaceSize=256M","-XX:MaxMetaspaceSize=256M","-jar","/app.jar"]
EXPOSE 10237