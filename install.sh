#!/bin/bash

mvn install:install-file -Dfile=jars/rabbitmq-client.jar -DgroupId=com.rabbitmq -DartifactId=rabbitmq-client -Dversion=3.1.5 -Dpackaging=jar
