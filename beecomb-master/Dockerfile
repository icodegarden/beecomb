FROM openjdk:17-jdk
WORKDIR /geelyapp
ADD target/glros-analysis.jar app.jar
ENV JAVA_OPTS "-Xmx2048M -Xms2048M -Xss256K -XX:MaxMetaspaceSize=256M -XX:MetaspaceSize=256M -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./"
ENV JAVA_ARGS "--server.port=8080"
EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar $JAVA_ARGS