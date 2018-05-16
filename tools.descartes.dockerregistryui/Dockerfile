FROM openjdk:8-jre-alpine
MAINTAINER Chair of Software Engineering <se2-it@informatik.uni-wuerzburg.de>


COPY target/dockerregistryui.jar /usr/local/dockerregistryui.jar

RUN apk add --no-cache sed
RUN sed -i 's/securerandom.source=file:\/dev.*/securerandom.source=file:\/dev\/urandom/g' /usr/lib/jvm/default-jvm/jre/lib/security/java.security

RUN mkdir -p /var/lib/dockerregistryui
VOLUME /var/lib/dockerregistryui

ENV REGISTRY_HOST docker-registry
ENV REGISTRY_PROTOCOL https

# Setting secure random number generation to urandom. This increases the startup speed.
# Environment variable no longer needed. Replaced by sed in start.sh
# ENV JAVA_OPTS $JAVA_OPTS -Dsecurerandom.source=file:/dev/urandom

EXPOSE 8080

CMD java -jar /usr/local/dockerregistryui.jar ${REGISTRY_HOST} ${REGISTRY_PROTOCOL}
