FROM openjdk:17-jdk
WORKDIR /opt
ARG VERSION
ADD target/beecomb-worker-${VERSION}.jar app.jar
ENV JAVA_OPTS "-server -Xms2048M -Xmx2048M -Xmn1024M -Xss256K -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=256M -XX:MaxDirectMemorySize=50M -Dio.netty.noPreferDirect=true -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./"
ENV JAVA_ARGS ""
ENTRYPOINT exec java --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED $JAVA_OPTS -jar app.jar $JAVA_ARGS

