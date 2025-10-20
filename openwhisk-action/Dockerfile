#FROM openwhisk/java11action:1.3.0
#FROM ghcr.io/apache/openwhisk/java11action:latest
#FROM openwhisk/action-java-v11:latest
# Use official OpenJDK 11 slim image as base
#FROM openjdk:11-jre-slim
#FROM ghcr.io/apache/openwhisk/action-java-11:nightly
#FROM ghcr.io/apache/openwhisk/java11action:1.5.0
#FROM openwhisk/java11action
#FROM openwhisk/java11action:latest
#FROM ghcr.io/apache/openwhisk/java11action:1.5.0
#FROM adoptopenjdk:11-jre-slim

FROM openjdk:11-jre-slim

# copy your fat jar (built with Main-Class in the manifest)
COPY target/openwhisk-action-1.0-SNAPSHOT-jar-with-dependencies.jar /action.jar

# run the jar; the manifest's Main-Class kicks in
#ENTRYPOINT ["java","-jar","/action.jar"]
ENTRYPOINT ["java","-cp","/action.jar","com.mpmusc.openwhisk.Main"]






#ADD target/openwhisk-action-1.0-SNAPSHOT-jar-with-dependencies.jar /action/exec.jar


# Set working directory inside container
#WORKDIR /action

# Copy your fat jar (built with dependencies) into the image
#COPY target/openwhisk-action-1.0-SNAPSHOT-jar-with-dependencies.jar /action/app.jar

# Copy the OpenWhisk Java action loop jar if you have it locally (optional)
# COPY java11ActionLoop.jar /action/java11ActionLoop.jar

# Expose standard action port (if needed)
#EXPOSE 8080

# Set the entrypoint to run your Java action
# Replace com.mpmusc.openwhisk.GenderOpenWhisk with your main class that implements the Action interface
#ENTRYPOINT ["java", "-cp", "app.jar", "com.mpmusc.openwhisk.GenderOpenWhisk"]
