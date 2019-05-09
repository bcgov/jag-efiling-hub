#!/usr/bin/env bash

mvn --settings /usr/.m2/settings-docker.xml clean test-compile exec:java -Plocal
