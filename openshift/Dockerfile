FROM maven:3.3-jdk-8

COPY ./src /usr/src/hub
COPY ./pom.xml /usr/src/hub/pom.xml

EXPOSE 8888

CMD ["mvn clean test-compile exec:java -Plocal"]
