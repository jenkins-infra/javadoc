# Use nginx stable
FROM nginx:1.23.1

ARG PUBLISH_PATH=/usr/share/nginx/html

RUN apt-get update && apt-get install -y openjdk-11-jdk wget ant groovy curl sed

# Just test settings to speedup the startup
ARG LTS_RELEASES=""
ARG PLUGINS=""

WORKDIR /opt/build/javadoc
COPY resources/ /opt/build/javadoc/resources
COPY scripts/ /opt/build/javadoc/scripts
COPY src/ /opt/build/javadoc/src
RUN bash -ex scripts/generate-javadoc.sh && bash -ex scripts/generate-shortnames.sh && bash -ex scripts/default-to-latest.sh && cp -R /opt/build/javadoc/build/site/* ${PUBLISH_PATH} && rm -rf /opt/build/javadoc

# For testing of particular steps
#RUN groovy -cp src/main/groovy scripts/generate-javadoc-components.groovy
#RUN cp -R /opt/build/javadoc/build/site/* ${PUBLISH_PATH}

# TODO: Bonus points squash apt-get and build and remove unneccesary packages after the build (or use different builder and prod images)

WORKDIR ${PUBLISH_PATH}/
