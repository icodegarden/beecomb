FROM openjdk:17-jdk
WORKDIR /opt
ARG VERSION
ADD target/beecomb-master-${VERSION}.jar app.jar
ENV JAVA_OPTS "-server -Xmx1024M -Xms1024M -Xmn512M -Xss256K -XX:MaxMetaspaceSize=256M -XX:MetaspaceSize=256M -XX:MaxDirectMemorySize=50M -Dio.netty.noPreferDirect=true -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./"
ENV JAVA_ARGS "--server.port=9898"
EXPOSE 9898
ENTRYPOINT exec java --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED $JAVA_OPTS -jar app.jar $JAVA_ARGS