FROM centos

RUN cd /etc/yum.repos.d/
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*

RUN  yum install java-1.8.0-openjdk -y

COPY build/distributions/datasense-*.noarch.rpm /tmp/datasense.rpm
RUN yum install -y /tmp/datasense.rpm && rm -f /tmp/datasense.rpm && yum clean all
COPY env/docker_datasense /etc/default/datasense
ENTRYPOINT . /etc/default/datasense && java -Dserver.port=$DATASENSE_PORT -DDATASENSE_LOG_LEVEL=$DATASENSE_LOG_LEVEL -jar /opt/datasense/lib/datasense.war

