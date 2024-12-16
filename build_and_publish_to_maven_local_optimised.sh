#!/bin/bash

SECONDS=0

####################################################################################################
###########################################  Clean  ################################################
####################################################################################################

./gradlew clean

####################################################################################################
##########################################  Build  #################################################
####################################################################################################


#./gradlew :libraries:profiling-adapter:build
#./gradlew :libraries:profiling-adapter:profiling-adapter-test:build

#./gradlew :libraries:profiling-analyzer:build
#./gradlew :libraries:profiling-analyzer:profiling-analyzer-test:build

./gradlew :libraries:profiling-android:build
#./gradlew :libraries:profiling-android:profiling-android-test:build

#./gradlew :libraries:profiling-anomaly:build
#./gradlew :libraries:profiling-anomaly:profiling-anomaly-test:build

#./gradlew :libraries:profiling-core:build
#./gradlew :libraries:profiling-core:profiling-core-test:build

#./gradlew :libraries:profiling-logger:build
#./gradlew :libraries:profiling-logger:profiling-logger-test:build

#./gradlew :libraries:profiling-tracer:build
#./gradlew :libraries:profiling-tracer:profiling-tracer-test:build


####################################################################################################
####################################  Publish to maven local  ######################################
####################################################################################################

./gradlew :libraries:profiling-adapter:publishToMavenLocal
#./gradlew :libraries:profiling-adapter:profiling-adapter-test:publishToMavenLocal

./gradlew :libraries:profiling-analyzer:publishToMavenLocal
#./gradlew :libraries:profiling-analyzer:profiling-analyzer-test:publishToMavenLocal

./gradlew :libraries:profiling-android:publishToMavenLocal
#./gradlew :libraries:profiling-android:profiling-android-test:publishToMavenLocal

./gradlew :libraries:profiling-anomaly:publishToMavenLocal
#./gradlew :libraries:profiling-anomaly:profiling-anomaly-test:publishToMavenLocal

./gradlew :libraries:profiling-core:publishToMavenLocal
#./gradlew :libraries:profiling-core:profiling-core-test:publishToMavenLocal

./gradlew :libraries:profiling-logger:publishToMavenLocal
#./gradlew :libraries:profiling-logger:profiling-logger-test:publishToMavenLocal

./gradlew :libraries:profiling-tracer:publishToMavenLocal
#./gradlew :libraries:profiling-tracer:profiling-tracer-test:publishToMavenLocal

echo "total time taken $SECONDS"