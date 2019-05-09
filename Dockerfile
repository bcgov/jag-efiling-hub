FROM maven:3.3-jdk-8

RUN mkdir /usr/src/hub
RUN chmod 777 /usr/src/hub
ADD pom.xml /usr/src/hub/pom.xml
ADD start.sh /usr/src/hub/start.sh
ADD src /usr/src/hub/src

RUN mkdir /usr/.m2
RUN chmod 777 /usr/.m2
ADD settings-docker.xml /usr/.m2/settings-docker.xml

EXPOSE 8888

WORKDIR /usr/src/hub

CMD ["./start.sh"]
