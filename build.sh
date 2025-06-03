#!/bin/bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.github.djaquels.Main"
