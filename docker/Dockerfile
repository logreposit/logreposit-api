FROM openjdk:10-jre

RUN apk update && apk upgrade

WORKDIR /opt/logreposit-api

COPY run.sh      /opt/logreposit-api/run.sh
COPY app.jar     /opt/logreposit-api/app.jar

RUN chmod 755    /opt/logreposit-api/run.sh

ENTRYPOINT ["/opt/logreposit-api/run.sh"]
