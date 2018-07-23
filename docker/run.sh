#!/bin/sh

echo "Starting application /app.jar ..."
java -Xmx512m -Djava.security.egd=file:/dev/./urandom -jar ./app.jar
