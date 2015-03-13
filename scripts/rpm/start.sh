#!/bin/sh
nohup java -Dserver.port=$DATASENSE_PORT -jar /opt/datasense/lib/datasense-0.1-SNAPSHOT.war > /var/log/datasense/datasense.log &
echo $! > /var/run/datasense/datasense.pid
