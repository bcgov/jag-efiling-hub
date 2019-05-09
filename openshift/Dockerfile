FROM maven:3.3-jdk-8

RUN mkdir /usr/src/hub
ADD pom.xml /usr/src/hub/pom.xml
ADD start.sh /usr/src/hub/start.sh
ADD src /usr/src/hub/src

EXPOSE 8888

WORKDIR /usr/src/hub

CMD ["./start.sh"]
