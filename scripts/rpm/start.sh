#!/bin/sh
nohup java -Dserver.port=$DATASENSE_PORT -jar /opt/datasense/lib/datasense-0.1-SNAPSHOT.war > /dev/null 2>&1  &
echo $! > /var/run/datasense/datasense.pid
