FROM openjdk:8-jre-alpine

RUN apk update
RUN apk upgrade

RUN apk add bash

ENV JAVA_HOME /usr/lib/jvm/java-8-*/
ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

ADD target/*.yaml /app/
ADD target/pay-*-allinone.jar /app/
ADD docker-startup.sh /app/docker-startup.sh
ADD chamber.sha256sum /app/chamber.sha256sum

RUN apk add openssl && \
    mkdir -p bin && \
    wget -qO bin/chamber https://github.com/segmentio/chamber/releases/download/v1.9.0/chamber-v1.9.0-linux-amd64 && \
    sha256sum -c chamber.sha256sum && \
    chmod 755 bin/chamber && \
    apk del --purge openssl

CMD bash ./docker-startup.sh
