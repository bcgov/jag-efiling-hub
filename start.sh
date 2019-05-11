#!/usr/bin/env bash

mvn --settings /usr/.m2/settings-docker.xml clean compile exec:java -Plocal
