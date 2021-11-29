# Copyright (c) 2020 Ayman ElSherif <ayman.elsherif@outlook.com> - All Rights Reserved

FROM openjdk:17-alpine

RUN apk add --no-cache lksctp-tools tcpdump

RUN mkdir -p /app/
RUN mkdir -p /app/logs/
COPY ./intruder.yml /app/app.yml
COPY ./intruder.jar /app/app.jar
WORKDIR /app

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
