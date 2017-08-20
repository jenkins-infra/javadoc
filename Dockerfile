FROM nginx:1.13.3

ARG PUBLISH_PATH=/usr/share/nginx/html

RUN apt-get update
# https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=863199, man-db does not help
RUN mkdir -p /usr/share/man/man1/
RUN apt-get purge openjdk-7-jre-headless && apt-get install -y openjdk-8-jdk wget ant groovy

RUN apt-get install -y curl

# Just test settings to speedup the startup
ARG LTS_RELEASES=""
ARG PLUGINS=""

WORKDIR /opt/build/javadoc
COPY resources/ /opt/build/javadoc/resources
COPY scripts/ /opt/build/javadoc/scripts
RUN bash -ex scripts/generate-javadoc.sh && bash -ex scripts/generate-shortnames.sh && bash -ex scripts/default-to-latest.sh && cp -R /opt/build/javadoc/build/site/* ${PUBLISH_PATH} && rm -rf /opt/build/javadoc


# TODO: Bonus points squash apt-get and build and remove unneccesary packages after the build (or use different builder and prod images)

WORKDIR ${PUBLISH_PATH}/
