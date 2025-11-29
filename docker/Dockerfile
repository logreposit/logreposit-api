FROM eclipse-temurin:25-jre

WORKDIR /opt/logreposit/logreposit-api

COPY run.sh    /opt/logreposit/logreposit-api/run.sh
COPY app.jar   /opt/logreposit/logreposit-api/app.jar
RUN  chmod 755 /opt/logreposit/logreposit-api/run.sh

ENTRYPOINT ["/opt/logreposit/logreposit-api/run.sh"]
