FROM azul/zulu-openjdk-centos:8-latest

COPY build/distributions/datasense-*.noarch.rpm /tmp/datasense.rpm
RUN yum install -y /tmp/datasense.rpm && rm -f /tmp/datasense.rpm && yum clean all
COPY env/docker_datasense /etc/default/datasense
ENTRYPOINT . /etc/default/datasense && java -Dserver.port=$DATASENSE_PORT -DDATASENSE_LOG_LEVEL=$DATASENSE_LOG_LEVEL -jar /opt/datasense/lib/datasense.war

