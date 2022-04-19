FROM openjdk:17-jdk
WORKDIR /opt
ARG VERSION
ADD target/beecomb-master-${VERSION}.jar app.jar
ENV JAVA_OPTS "-Xmx2048M -Xms2048M -Xss256K -XX:MaxMetaspaceSize=256M -XX:MetaspaceSize=256M"
ENV JAVA_ARGS "--server.port=9898"
EXPOSE 9898
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar $JAVA_ARGS